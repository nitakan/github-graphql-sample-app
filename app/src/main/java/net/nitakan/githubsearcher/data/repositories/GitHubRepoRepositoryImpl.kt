package net.nitakan.githubsearcher.data.repositories

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.nitakan.githubsearcher.data.remote.GitHubApi
import net.nitakan.githubsearcher.model.entities.*
import net.nitakan.githubsearcher.model.repositories.GitHubRepoRepository
import javax.inject.Inject

class GitHubRepoRepositoryImpl @Inject constructor(
    private val api: GitHubApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : GitHubRepoRepository {

    private val _appliedRepositoryFlow = MutableSharedFlow<GitHubRepository>()
    override val updatedRepositories: Flow<GitHubRepository> = _appliedRepositoryFlow

    private suspend fun applyRepository(repository: GitHubRepository) {
        _appliedRepositoryFlow.emit(repository)
    }

    override fun search(
        condition: SearchRepositoriesCondition,
        coroutineScope: CoroutineScope
    ): Flow<Result<GitHubRepositoriesResult>> {
        return MutableSharedFlow<Result<GitHubRepositoriesResult>>().also { flow ->
            coroutineScope.launch(dispatcher) {
                val result = kotlin.runCatching {
                    val dao = doSearchRepositories(condition)
                    GitHubRepositoriesResult(
                        condition,
                        dao.repositories,
                        GithubPagination(dao.count, dao.hasNext) {
                            nextRepositories(
                                condition,
                                dao.lastCursor,
                                flow,
                            ) { cursor ->
                                doSearchRepositories(condition, cursor)
                            }
                        })
                }
                flow.emit(result)
            }
        }
    }

    override suspend fun get(ownerName: String?, repositoryName: String?) =
        withContext(dispatcher) {
            kotlin.runCatching {
                api.getRepository(ownerName, repositoryName).also {
                    applyRepository(it)
                }
            }
        }

    override suspend fun setStar(repository: GitHubRepository, star: Boolean): Result<Boolean> {
        val copied = repository.copy(viewerHasStarred = star)
        applyRepository(copied)

        val result = if (star) {
            api.addStar(repository.id)
        } else {
            api.removeStar(repository.id)
        }
        result.onFailure {
            // 失敗時は戻す
            applyRepository(repository)
        }
        return result
    }

    override suspend fun setSubscription(
        repository: GitHubRepository,
        subscription: GitHubViewSubscription
    ): Result<GitHubViewSubscription> {
        val copied = repository.copy(
            viewerSubscription = subscription
        )
        applyRepository(copied)
        val result = api.setSubscription(copied.id, subscription)
        result.onFailure {
            // 失敗時は戻す
            applyRepository(repository)
        }
        return result
    }

    private suspend fun nextRepositories(
        condition: SearchRepositoriesCondition,
        lastCursor: String?,
        flow: MutableSharedFlow<Result<GitHubRepositoriesResult>>,
        executeProc: suspend (cursor: String?) -> GitHubApi.GitHubRepositoriesResponse,
    ): Unit = withContext(dispatcher) {
        val result = kotlin.runCatching {
            val dao = executeProc(lastCursor)
            GitHubRepositoriesResult(
                condition,
                dao.repositories,
                GithubPagination(dao.count, dao.hasNext) {
                    if (dao.hasNext) {
                        nextRepositories(
                            condition,
                            dao.lastCursor,
                            flow,
                        ) { cursor ->
                            doSearchRepositories(condition, cursor)
                        }
                    }
                })
        }
        flow.emit(result)
    }

    private suspend fun doSearchRepositories(
        condition: SearchRepositoriesCondition,
        lastCursor: String? = null
    ): GitHubApi.GitHubRepositoriesResponse {
        return api.searchRepositories(
            condition.searchWord,
            condition.sortForSearch(),
            condition.limit,
            lastCursor
        ).also {
            it.repositories.forEach { repository ->
                applyRepository(repository)
            }
        }
    }
}

fun SearchRepositoriesCondition.sortForSearch(): String {
    return """${
        when (this.sortBy) {
            SearchRepositorySort.CreatedAt -> "created"
            SearchRepositorySort.Forks -> "forks"
            SearchRepositorySort.Stargazers -> "stars"
            SearchRepositorySort.UpdatedAt -> "updated"
        }
    }-${
        when (this.order) {
            SearchRepositoryOrder.Asc -> "asc"
            SearchRepositoryOrder.Desc -> "desc"
        }
    }"""
}