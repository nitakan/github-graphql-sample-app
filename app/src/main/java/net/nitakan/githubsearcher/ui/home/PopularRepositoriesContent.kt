package net.nitakan.githubsearcher.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.nitakan.githubsearcher.ui.widgets.GitHubRepositoryList
import net.nitakan.githubsearcher.ui.widgets.OnRepositoryItemClick
import net.nitakan.githubsearcher.ui.widgets.RetryableCard

@Composable
fun PopularRepositoriesContent(
    homeViewModel: HomeViewModel,
    padding: PaddingValues,
    onRepositoryStarClick: OnRepositoryItemClick,
    onRepositoryItemClick: OnRepositoryItemClick
) {

    val context = LocalContext.current
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val result by homeViewModel.result.collectAsState()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = {
            homeViewModel.retry()
        }
    ) {
        result
            .onSuccess { items ->
                Box(modifier = Modifier.padding(padding)) {
                    GitHubRepositoryList(
                        items,
                        homeViewModel,
                        "Popular Android Repositories",
                        onRepositoryStarClick,
                        onRepositoryItemClick
                    )
                }
            }.onFailure {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    RetryableCard(
                        modifier = Modifier
                            .align(Alignment.Center),
                        onClick = {
                            homeViewModel.retry()
                        },
                    )
                }
            }
    }

}