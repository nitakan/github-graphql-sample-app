package net.nitakan.githubsearcher.model.entities

data class GitHubRepository(
    val id: String,
    val name: String,
    val url: String,
    val description: String?,
    val star: Int,
    val fork: Int,
    val owner: GitHubRepositoryOwner,
    val viewerHasStarred: Boolean,
    val viewerSubscription: GitHubViewSubscription,
    val languages: List<GitHubRepositoryLanguage>,
    // 以降詳細画面用
    val homePageUrl: String? = null,
    val issuesCount: Int? = null,
    val pullRequestCount: Int? = null,
    val watchersCount: Int? = null,
    val discussionCount: Int? = null,
    val releaseCount: Int? = null,
    val latestRelease: GitHubRepositoryRelease? = null,
    val license: GitHubLicense? = null,
    val topics: List<GitHubTopic> = emptyList()
) {
    val hasSubscription: Boolean = this.viewerSubscription == GitHubViewSubscription.SUBSCRIBED
}

enum class GitHubViewSubscription(val value: String) {
    NONE(""),
    IGNORED("IGNORED"),
    SUBSCRIBED("SUBSCRIBED"),
    UNSUBSCRIBED("UNSUBSCRIBED");

    companion object {
        fun find(value: String?): GitHubViewSubscription {
            return values().firstOrNull { it.value == value } ?: NONE
        }
    }
}

data class GitHubRepositoryRelease(
    val name: String,
    val description: String?,
    val tagName: String,
    val publishedAt: String?,
    val url: String,
)

data class GitHubLicense(val name: String, val nickname: String?, val url: String?)

data class GitHubTopic(
    val name: String,
    val isStared: Boolean,
    val starCount: Int,
    val url: String,
)

data class GitHubRepositoryOwner(
    val id: String,
    val avatarUri: String,
    val name: String,
    val profileUri: String,
)

data class GitHubRepositoryLanguage(
    val id: String,
    val name: String,
    val color: String?,
)

data class GitHubRepositoriesResult(
    val searchRepositoriesCondition: SearchRepositoriesCondition,
    val repositories: List<GitHubRepository>,
    val pagination: GithubPagination?
) {
    companion object {
        val empty = GitHubRepositoriesResult(
            searchRepositoriesCondition = SearchRepositoriesCondition(""),
            repositories = emptyList(),
            pagination = null,
        )
    }
}

data class GithubPagination(
    val count: Int,
    val hasNext: Boolean,
    val next: suspend () -> Unit,
)