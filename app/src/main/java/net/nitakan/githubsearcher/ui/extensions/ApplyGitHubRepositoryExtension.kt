package net.nitakan.githubsearcher.ui.extensions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import net.nitakan.githubsearcher.model.entities.GitHubRepositoriesResult
import net.nitakan.githubsearcher.model.entities.GitHubRepository

@JvmName("applyRepositories")
fun MutableStateFlow<Result<GitHubRepositoriesResult>>.applyRepository(repository: GitHubRepository) {
    this.getAndUpdate { r ->
        r.map { repositories ->
            repositories.copy(repositories = repositories.repositories.map {
                if (it.id == repository.id) {
                    it.copy(
                        viewerHasStarred = repository.viewerHasStarred,
                        viewerSubscription = repository.viewerSubscription,
                    )
                } else {
                    it
                }
            })
        }

    }
}

@JvmName("applyRepository")
fun MutableStateFlow<Result<GitHubRepository?>>.applyRepository(repository: GitHubRepository) {
    this.getAndUpdate { r ->
        r.map {
            if (it?.id == repository.id) {
                it.copy(
                    viewerHasStarred = repository.viewerHasStarred,
                    viewerSubscription = repository.viewerSubscription,
                )
            } else {
                it
            }
        }
    }
}