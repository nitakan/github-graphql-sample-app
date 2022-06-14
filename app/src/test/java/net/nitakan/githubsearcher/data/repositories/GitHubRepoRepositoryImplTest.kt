@file:OptIn(ExperimentalCoroutinesApi::class)

package net.nitakan.githubsearcher.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.nitakan.githubsearcher.data.remote.GitHubApi
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.model.entities.GitHubRepositoryOwner
import net.nitakan.githubsearcher.model.entities.GitHubViewSubscription
import net.nitakan.githubsearcher.model.entities.SearchRepositoriesCondition
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GitHubRepoRepositoryImplTest {
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
        viewerSubscription = GitHubViewSubscription.SUBSCRIBED,
        languages = emptyList(),
    )

    @Before
    fun setUp() {
    }

    @Test
    fun `検索結果が1ページのみの場合はhasNextがfalseになること`() = runTest {
        val api = mock<GitHubApi> {
            onBlocking { it.searchRepositories(any(), any(), any(), anyOrNull()) } doAnswer {
                GitHubApi.GitHubRepositoriesResponse(
                    listOf(repository),
                    1,
                    false,
                    "lastCursor"
                )
            }
        }

        val repo = GitHubRepoRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        val flow = repo.search(SearchRepositoriesCondition("TEST"), this)
        val item = flow.first()

        assertEquals(item.isSuccess, true)
        val result = item.getOrNull()
        assertNotNull(result)
        assertEquals(result?.pagination?.hasNext, false)
    }

    @Test
    fun `nextを実行することで次ページが取得できること`() = runTest {
        val api = mock<GitHubApi> {
            onBlocking { it.searchRepositories(any(), any(), any(), isNull()) } doAnswer {
                GitHubApi.GitHubRepositoriesResponse(
                    listOf(repository.copy(id = "first")),
                    2,
                    true,
                    "first"
                )
            }
            onBlocking {
                it.searchRepositories(any(), any(), any(), argThat { this == "first" })
            } doAnswer {
                GitHubApi.GitHubRepositoriesResponse(
                    listOf(repository.copy(id = "second")),
                    2,
                    false,
                    "second"
                )
            }
        }

        val repo = GitHubRepoRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        val flow = repo.search(SearchRepositoriesCondition("TEST", limit = 1), this)

        // １ページ目のFlow消費
        flow.first().let { item ->
            assertEquals(item.isSuccess, true)
            val result = item.getOrNull()
            assertNotNull(result)
            assertEquals("first", result!!.repositories.first().id)
            assertTrue(result.pagination?.hasNext!!)

            launch {
                // これを呼ぶことでFlowに次ページがemitされる
                result.pagination!!.next()
            }
        }

        // ２ページ目のFlow消費
        flow.first().let { item ->
            val result = item.getOrNull()
            assertNotNull(result)
            assertEquals("second", result!!.repositories.first().id)
            assertFalse(result.pagination!!.hasNext)
        }
    }

    @Test
    fun `nextを2回実行することで３ページ目が取得できること`() = runTest {
        val api = mock<GitHubApi> {
            onBlocking { it.searchRepositories(any(), any(), any(), isNull()) } doAnswer {
                GitHubApi.GitHubRepositoriesResponse(
                    listOf(repository.copy(id = "first")),
                    2,
                    true,
                    "first"
                )
            }
            onBlocking {
                it.searchRepositories(any(), any(), any(), argThat { this == "first" })
            } doAnswer {
                GitHubApi.GitHubRepositoriesResponse(
                    listOf(repository.copy(id = "second")),
                    3,
                    true,
                    "second"
                )
            }
            onBlocking {
                it.searchRepositories(any(), any(), any(), argThat { this == "second" })
            } doAnswer {
                GitHubApi.GitHubRepositoriesResponse(
                    listOf(repository.copy(id = "third")),
                    3,
                    false,
                    "third"
                )
            }
        }

        val repo = GitHubRepoRepositoryImpl(api, StandardTestDispatcher(testScheduler))
        val flow = repo.search(SearchRepositoriesCondition("TEST", limit = 1), this)

        // １ページ目のFlow消費
        flow.first().let { item ->
            assertEquals(item.isSuccess, true)
            val result = item.getOrNull()
            assertNotNull(result)
            assertEquals("first", result!!.repositories.first().id)
            assertTrue(result.pagination?.hasNext!!)

            // next()はflowの消費を待つので別Coroutineで起動する
            launch {
                // これを呼ぶことでFlowに次ページがemitされる
                result.pagination!!.next()
            }
        }

        // ２ページ目のFlow消費
        flow.first().let { item ->
            val result = item.getOrNull()
            assertNotNull(result)
            assertEquals("second", result!!.repositories.first().id)
            assertTrue(result.pagination!!.hasNext)

            launch {
                // これを呼ぶことでFlowに次ページがemitされる
                result.pagination!!.next()
            }
        }

        // ３ページ目のFlow消費
        flow.first().let { item ->
            val result = item.getOrNull()
            assertNotNull(result)
            assertEquals("third", result!!.repositories.first().id)
            assertFalse(result.pagination!!.hasNext)
        }
    }

    // Stargazer
    @Test
    fun `Starを追加すると更新されたリポジトリが流れる`() = runTest {
        val api = mock<GitHubApi> {
            onBlocking { it.getRepository(any(), any()) } doAnswer {
                repository.copy(viewerHasStarred = false)
            }
            onBlocking { it.addStar(any()) } doReturn Result.success(true)
        }

        val repo = GitHubRepoRepositoryImpl(api)
        val currentRepository = repo.get("a", "a")

        assertEquals(currentRepository.getOrNull()?.viewerHasStarred, false)

        // launchを即実行することでsetStarする前に`updateRepositories`を購読する
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            // Get時のemitを消費する
            repo.updatedRepositories.first()
            val item = repo.updatedRepositories.first()
            assertEquals(item.id, repository.id)
            assertEquals(true, repository.viewerHasStarred)
        }
        repo.setStar(currentRepository.getOrNull()!!, true).getOrThrow()
        job.cancel()
    }

    @Test
    fun `Starを削除すると更新されたリポジトリが流れる`() = runTest {
        val api = mock<GitHubApi> {
            onBlocking { it.getRepository(any(), any()) } doAnswer {
                repository.copy(viewerHasStarred = true, id = "AAAA")
            }
            onBlocking { it.removeStar(any()) } doReturn Result.success(false)
        }

        val repo = GitHubRepoRepositoryImpl(api)
        val currentRepository = repo.get("a", "a")

        assertEquals(true, currentRepository.getOrNull()?.viewerHasStarred)

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            // Get時のemitを消費する
            repo.updatedRepositories.first()
            val item = repo.updatedRepositories.first()
            assertEquals("AAAA", item.id)
            assertEquals(false, repository.viewerHasStarred)
        }
        assertEquals(false, repo.setStar(currentRepository.getOrNull()!!, false).getOrThrow())
        job.cancel()
    }


    @Test
    fun `Subscriptionを変更すると更新されたリポジトリが流れる`() = runTest {
        val api = mock<GitHubApi> {
            onBlocking { it.getRepository(any(), any()) } doAnswer {
                repository.copy(viewerSubscription = GitHubViewSubscription.IGNORED, id = "AAAA")
            }
            onBlocking { it.setSubscription(any(), any()) } doReturn Result.success(GitHubViewSubscription.SUBSCRIBED)
        }

        val repo = GitHubRepoRepositoryImpl(api)
        val currentRepository = repo.get("a", "a")

        assertEquals(
            GitHubViewSubscription.IGNORED,
            currentRepository.getOrNull()?.viewerSubscription
        )

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repo.updatedRepositories.first()
            val item = repo.updatedRepositories.first()
            assertEquals("AAAA", item.id)
            assertEquals(GitHubViewSubscription.SUBSCRIBED, item.viewerSubscription)
        }
        repo.setSubscription(currentRepository.getOrNull()!!, GitHubViewSubscription.SUBSCRIBED)
        job.cancel()
    }
}


