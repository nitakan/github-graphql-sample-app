package net.nitakan.githubsearcher.model.entities

data class SearchRepositoriesCondition(
    val searchWord: String,
    val limit: Int = 20,
    val sortBy: SearchRepositorySort = SearchRepositorySort.Stargazers,
    val order: SearchRepositoryOrder = SearchRepositoryOrder.Desc,
)

enum class SearchRepositorySort {
    CreatedAt,
    Forks,
    Stargazers,
    UpdatedAt,
}

enum class SearchRepositoryOrder {
    Asc,
    Desc,
}