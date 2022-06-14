package net.nitakan.githubsearcher.data.mock

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.nitakan.githubsearcher.model.entities.*
import net.nitakan.githubsearcher.model.exceptions.NotFoundException
import net.nitakan.githubsearcher.model.repositories.GitHubRepoRepository
import javax.inject.Inject

class MockGithubRepoRepository @Inject constructor() : GitHubRepoRepository {
    private val list = (0..201).map {
        val number = it
        GitHubRepository(
            "$number",
            name = "Repository Name $number",
            url = "https://google.com",
            description = null,
            star = (Math.random() * 300).toInt(),
            fork = (Math.random() * 50).toInt(),
            owner = GitHubRepositoryOwner(
                id = "$number",
                avatarUri = "",
                name = "Owner $number",
                profileUri = "https://google.com"
            ),
            pullRequestCount = 0,
            watchersCount = 0,
            languages = emptyList(),
            viewerHasStarred = false,
            viewerSubscription = GitHubViewSubscription.UNSUBSCRIBED,
        )
    }

    private val _applied = MutableSharedFlow<GitHubRepository>()
    override val updatedRepositories: Flow<GitHubRepository> = _applied

    override fun search(
        condition: SearchRepositoriesCondition,
        coroutineScope: CoroutineScope
    ): Flow<Result<GitHubRepositoriesResult>> {
        return MutableSharedFlow<Result<GitHubRepositoriesResult>>().also { searchResult ->
            coroutineScope.launch(Dispatchers.IO) {
                val result = kotlin.runCatching {
                    val dao = doSearchRepositories(condition, lastCursor = null)
                    GitHubRepositoriesResult(
                        condition,
                        dao.repositories,
                        GithubPagination(dao.count, dao.hasNext) {
                            next(condition, dao.lastCursor!!, searchResult) { cursor ->
                                doSearchRepositories(condition, cursor)
                            }
                        })
                }
                searchResult.emit(result)
            }
        }
    }

    override suspend fun get(ownerName: String?, repositoryName: String?): Result<GitHubRepository> {
        if (ownerName.isNullOrBlank() || repositoryName.isNullOrBlank()) {
            return Result.failure(NotFoundException("repository not found: $ownerName/$repositoryName"))
        }
        delay(1000)
        return kotlin.runCatching {
            list.firstOrNull {
                it.name == repositoryName && it.owner.name == ownerName
            } ?: throw NotFoundException("repository not found: $ownerName/$repositoryName")
        }
    }

    override suspend fun setStar(repository: GitHubRepository, star: Boolean): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun setSubscription(repository: GitHubRepository, subscription: GitHubViewSubscription): Result<GitHubViewSubscription> {
        return Result.success(GitHubViewSubscription.SUBSCRIBED)
    }

    private suspend fun next(
        condition: SearchRepositoriesCondition,
        lastCursor: String,
        searchResult: MutableSharedFlow<Result<GitHubRepositoriesResult>>,
        executeProc: suspend (cursor: String) -> GitHubRepositoriesResultDao,
    ) = withContext(Dispatchers.IO) {
        searchResult.emit(kotlin.runCatching {
            val dao = executeProc(lastCursor)
            GitHubRepositoriesResult(
                condition,
                dao.repositories,
                GithubPagination(dao.count, dao.hasNext) {
                    if (dao.hasNext) {
                        nextExecute(dao, condition, searchResult, executeProc)
                    }
                })
        })
    }

    private suspend fun nextExecute(
        dao: GitHubRepositoriesResultDao,
        condition: SearchRepositoriesCondition,
        result: MutableSharedFlow<Result<GitHubRepositoriesResult>>,
        executeProc: suspend (cursor: String) -> GitHubRepositoriesResultDao,
    ) {
        val cursor = dao.repositories.last().id
        next(condition, cursor, result, executeProc)

    }

    private suspend fun doSearchRepositories(
        condition: SearchRepositoriesCondition,
        lastCursor: String?
    ): GitHubRepositoriesResultDao {
        delay(1000)
        val repositories: Pair<List<GitHubRepository>, Boolean> = if (lastCursor == null) {
            Pair(this.list.subList(0, condition.limit), (condition.limit) < this.list.size)
        } else {
            val lastIndex = list.indexOfFirst { it.id == lastCursor }
            if (lastIndex < 0) {
                Pair(emptyList(), false)
            } else {
                val fromIndex = lastIndex + 1
                val toIndex = fromIndex + condition.limit
                val maxIndex = if (toIndex <= list.size) toIndex else list.size
                Pair(list.subList(fromIndex, maxIndex), maxIndex < list.size)
            }
        }
        return GitHubRepositoriesResultDao(
            repositories.first,
            this.list.size,
            repositories.second,
            repositories.first.lastOrNull()?.id
        )
    }
}

data class GitHubRepositoriesResultDao(
    val repositories: List<GitHubRepository>,
    val count: Int,
    val hasNext: Boolean,
    val lastCursor: String?,
)