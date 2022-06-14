package net.nitakan.githubsearcher.ui.extensions

import kotlinx.coroutines.flow.MutableStateFlow
import net.nitakan.githubsearcher.model.entities.*
import org.junit.Assert.*
import org.junit.Test

class TestApplyRepositories {

    private val repository = GitHubRepository(
        id = "ID",
        name = "NAME",
        url = "https://google.com",
        description = null,
        star = 0,
        fork = 0,
        owner = GitHubRepositoryOwner(
            "OWNER-ID",
            "https://google.com",
            "NAME",
            "https://google.com"
        ),
        viewerHasStarred = false,
        viewerSubscription = GitHubViewSubscription.IGNORED,
        languages = emptyList(),
    )

    private val mocklist =
        listOf(repository.copy(id = "1"), repository.copy(id = "2"), repository.copy(id = "3"))


    @Test
    fun `Starの更新が同じIDのみ反映されること`() {
        val flow = MutableStateFlow(
            Result.success(
                GitHubRepositoriesResult(
                    repositories = mocklist,
                    searchRepositoriesCondition = SearchRepositoriesCondition(""),
                    pagination = null
                )
            )
        )

        flow.value.getOrNull()!!.repositories.map { it.viewerHasStarred }.toBooleanArray().let {
            assertArrayEquals(booleanArrayOf(false, false, false), it)
        }

        flow.applyRepository(
            repository.copy(
                id = "2",
                viewerHasStarred = true,
            )
        )

        flow.value.getOrNull()!!.repositories.map { it.viewerHasStarred }.toBooleanArray().let {
            assertArrayEquals(booleanArrayOf(false, true, false), it)
        }
    }

    @Test
    fun `Subscriptionの更新が同じIDのみ反映されること`() {
        val flow = MutableStateFlow(
            Result.success(
                GitHubRepositoriesResult(
                    repositories = mocklist,
                    searchRepositoriesCondition = SearchRepositoriesCondition(""),
                    pagination = null
                )
            )
        )

        flow.value.getOrNull()!!.repositories.map { it.viewerSubscription }
            .toTypedArray().let {
                assertArrayEquals(
                    arrayOf(
                        GitHubViewSubscription.IGNORED,
                        GitHubViewSubscription.IGNORED,
                        GitHubViewSubscription.IGNORED,
                    ), it
                )
            }

        flow.applyRepository(
            repository.copy(
                id = "2",
                viewerSubscription = GitHubViewSubscription.SUBSCRIBED,
            )
        )

        flow.value.getOrNull()!!.repositories.map { it.viewerSubscription }
            .toTypedArray().let {
                assertArrayEquals(
                    arrayOf(
                        GitHubViewSubscription.IGNORED,
                        GitHubViewSubscription.SUBSCRIBED,
                        GitHubViewSubscription.IGNORED,
                    ), it
                )
            }
    }
}

internal class TestApplyRepository {
    private val repository = GitHubRepository(
        id = "ID",
        name = "NAME",
        url = "https://google.com",
        description = null,
        star = 0,
        fork = 0,
        owner = GitHubRepositoryOwner(
            "OWNER-ID",
            "https://google.com",
            "NAME",
            "https://google.com"
        ),
        viewerHasStarred = false,
        viewerSubscription = GitHubViewSubscription.IGNORED,
        languages = emptyList(),
    )

    @Test
    fun `IDが一致する場合Starの更新が反映されること`() {
        val flow =
            MutableStateFlow<Result<GitHubRepository?>>(Result.success(repository.copy(id = "2")))

        assertFalse(flow.value.getOrNull()!!.viewerHasStarred)

        flow.applyRepository(
            repository.copy(
                id = "2",
                viewerHasStarred = true,
            )
        )

        assertTrue(flow.value.getOrNull()!!.viewerHasStarred)

    }

    @Test
    fun `IDが一致しない場合Starの更新が反映されないこと`() {
        val flow =
            MutableStateFlow<Result<GitHubRepository?>>(Result.success(repository.copy(id = "1")))

        assertFalse(flow.value.getOrNull()!!.viewerHasStarred)

        flow.applyRepository(
            repository.copy(
                id = "2",
                viewerHasStarred = true,
            )
        )

        assertFalse(flow.value.getOrNull()!!.viewerHasStarred)

    }


    @Test
    fun `IDが一致する場合Subscriptionの更新が反映されること`() {
        val flow = MutableStateFlow<Result<GitHubRepository?>>(Result.success(repository.copy(id = "2")))

        assertEquals(GitHubViewSubscription.IGNORED, flow.value.getOrNull()?.viewerSubscription)

        flow.applyRepository(
            repository.copy(
                id = "2",
                viewerSubscription = GitHubViewSubscription.SUBSCRIBED
            )
        )
        assertEquals(GitHubViewSubscription.SUBSCRIBED, flow.value.getOrNull()?.viewerSubscription)
    }

    @Test
    fun `IDが一致しない場合Subscriptionの更新が反映されないこと`() {
        val flow = MutableStateFlow<Result<GitHubRepository?>>(Result.success(repository.copy(id = "1")))

        assertEquals(GitHubViewSubscription.IGNORED, flow.value.getOrNull()?.viewerSubscription)

        flow.applyRepository(
            repository.copy(
                id = "2",
                viewerSubscription = GitHubViewSubscription.SUBSCRIBED
            )
        )
        assertEquals(GitHubViewSubscription.IGNORED, flow.value.getOrNull()?.viewerSubscription)
    }
}