package net.nitakan.githubsearcher.data.remote

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.exception.ApolloException
import net.nitakan.githubsearcher.*
import net.nitakan.githubsearcher.fragment.GitHubRepositoryListItemFragment
import net.nitakan.githubsearcher.model.entities.*
import net.nitakan.githubsearcher.model.exceptions.NetworkException
import net.nitakan.githubsearcher.model.exceptions.NotFoundException
import net.nitakan.githubsearcher.model.exceptions.SearchException
import net.nitakan.githubsearcher.type.SubscriptionState
import javax.inject.Inject

interface GitHubApi {
    suspend fun searchRepositories(
        keyword: String,
        sort: String,
        limit: Int,
        afterCursor: String?
    ): GitHubRepositoriesResponse

    suspend fun getRepository(
        ownerName: String?,
        repositoryName: String?,
    ): GitHubRepository

    suspend fun addStar(starrableId: String): Result<Boolean>

    suspend fun removeStar(starrableId: String): Result<Boolean>

    suspend fun setSubscription(
        id: String,
        subscription: GitHubViewSubscription
    ): Result<GitHubViewSubscription>

    data class GitHubRepositoriesResponse(
        val repositories: List<GitHubRepository>,
        val count: Int,
        val hasNext: Boolean,
        val lastCursor: String?,
    )
}

class GitHubApiImpl @Inject constructor(
    privateAccessKey: String,
    url: String,
) : GitHubApi {
    private val apolloClient = ApolloClient.Builder()
        .addHttpHeader("Authorization", "bearer $privateAccessKey")
        .serverUrl(url).build()

    override suspend fun searchRepositories(
        keyword: String,
        sort: String,
        limit: Int,
        afterCursor: String?
    ): GitHubApi.GitHubRepositoriesResponse {

        val queryText = "sort:$sort $keyword"
        val query = SearchGitHubRepositoryQuery(
            queryText,
            limit,
            Optional.presentIfNotNull(afterCursor)
        )
        val result = try {
            apolloClient.query(query).execute()
        } catch (exception: ApolloException) {
            throw NetworkException(exception.message, cause = exception)
        }

        if (result.hasErrors()) {
            val errors = result.errors ?: emptyList()
            for (error in errors) {
                throw SearchException(error.message)
            }
        }

        val data = result.data?.search
        return if (data != null) {
            val repositoryCount = data.repositoryCount
            val pagination = data.pageInfo
            val list = data.nodes?.mapNotNull {
                it?.gitHubRepositoryListItemFragment?.toEntity()
            } ?: emptyList()

            GitHubApi.GitHubRepositoriesResponse(
                list,
                repositoryCount,
                pagination.hasNextPage,
                pagination.endCursor
            )
        } else {
            GitHubApi.GitHubRepositoriesResponse(emptyList(), 0, false, null)
        }
    }

    override suspend fun getRepository(
        ownerName: String?,
        repositoryName: String?
    ): GitHubRepository {
        if (ownerName.isNullOrBlank() || repositoryName.isNullOrBlank()) {
            throw NotFoundException("repository not found: $ownerName/$repositoryName")
        }
        val query = GetGitHubRepositoryQuery(repositoryName, ownerName)
        val result = try {
            this.apolloClient.query(query).execute()
        } catch (exception: ApolloException) {
            throw NetworkException(exception.message, cause = exception)
        }

        if (result.hasErrors()) {
            val errors = result.errors ?: emptyList()
            for (error in errors) {
                throw SearchException(error.message)
            }
        }
        return result.data?.repository?.toEntity()
            ?: throw NotFoundException("repository not found: $ownerName/$repositoryName")
    }

    override suspend fun addStar(starrableId: String): Result<Boolean> {
        return try {
            val mutation = AddStarMutation(starrableId)
            val result = this.apolloClient.mutation(mutation).execute()
            result.errors.orEmpty().forEach {
                throw Exception(it.message)
            }
            Result.success(false)
        } catch (exception: ApolloException) {
            Result.failure(Exception(exception.message, exception))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun removeStar(starrableId: String): Result<Boolean> {
        return try {
            val mutation = RemoveStarMutation(starrableId)
            val result = this.apolloClient.mutation(mutation).execute()
            result.errors.orEmpty().forEach {
                throw Exception(it.message)
            }
            Result.success(false)
        } catch (exception: ApolloException) {
            Result.failure(Exception(exception.message, exception))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun setSubscription(
        id: String,
        subscription: GitHubViewSubscription
    ): Result<GitHubViewSubscription> {
        return try {
            val mutation =
                UpdateSubscriptionMutation(id, SubscriptionState.valueOf(subscription.value))
            val result = this.apolloClient.mutation(mutation).execute()
            result.errors.orEmpty().forEach {
                throw Exception(it.message)
            }
            Result.success(subscription)
        } catch (exception: ApolloException) {
            Result.failure(Exception(exception.message, exception))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}

fun GetGitHubRepositoryQuery.Repository.toEntity(): GitHubRepository {
    val repo = this.gitHubRepositoryListItemFragment
    return GitHubRepository(
        repo.id,
        repo.name,
        repo.url.toString(),
        repo.description,
        repo.stargazerCount,
        repo.forkCount,
        GitHubRepositoryOwner(
            repo.owner.id,
            repo.owner.avatarUrl.toString(),
            repo.owner.login,
            repo.owner.url.toString()
        ),
        repo.viewerHasStarred,
        repo.viewerSubscription.toEntity(),
        repo.languages?.nodes?.mapNotNull {
            it?.let { node ->
                GitHubRepositoryLanguage(node.id, node.name, node.color)
            }
        } ?: emptyList(),

        homepageUrl?.toString(),
        issues.totalCount,
        pullRequests.totalCount,
        watchers.totalCount,
        discussions.totalCount,
        releases.totalCount,
        releases.nodes?.mapNotNull {
            it?.let { node ->
                GitHubRepositoryRelease(
                    node.name ?: node.tagName,
                    node.description,
                    node.tagName,
                    node.publishedAt?.toString(),
                    node.url.toString()
                )
            }
        }?.firstOrNull(),
        licenseInfo?.let {
            GitHubLicense(it.name, it.nickname, it.url?.toString())
        },
        repositoryTopics.nodes?.mapNotNull {
            it?.let { topic ->
                GitHubTopic(
                    topic.topic.name,
                    topic.topic.viewerHasStarred,
                    topic.topic.stargazerCount,
                    topic.url.toString()
                )
            }
        } ?: emptyList()
    )
}

fun GitHubRepositoryListItemFragment.toEntity(): GitHubRepository {
    val repo = this
    return GitHubRepository(
        repo.id,
        repo.name,
        repo.url.toString(),
        repo.description,
        repo.stargazerCount,
        repo.forkCount,
        GitHubRepositoryOwner(
            repo.owner.id,
            repo.owner.avatarUrl.toString(),
            repo.owner.login,
            repo.owner.url.toString()
        ),
        repo.viewerHasStarred,
        repo.viewerSubscription.toEntity(),
        repo.languages?.nodes?.mapNotNull {
            it?.let { node ->
                GitHubRepositoryLanguage(node.id, node.name, node.color)
            }
        } ?: emptyList()
    )
}

fun SubscriptionState?.toEntity(): GitHubViewSubscription {
    return GitHubViewSubscription.find(this?.rawValue)
}