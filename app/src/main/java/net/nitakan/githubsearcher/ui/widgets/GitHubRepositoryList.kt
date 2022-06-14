package net.nitakan.githubsearcher.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.nitakan.githubsearcher.extensions.toStringWithSiUnitSuffix
import net.nitakan.githubsearcher.model.entities.GitHubRepositoriesResult
import net.nitakan.githubsearcher.model.entities.GithubPagination

interface GitHubInfiniteScrollable {
    fun next(pagination: GithubPagination)
    fun retry()
}


@Composable
fun GitHubRepositoryList(
    items: GitHubRepositoriesResult,
    gitHubInfiniteScrollable: GitHubInfiniteScrollable,
    title: String? = null,
    onItemStarClick: OnRepositoryItemClick = {},
    onItemClicked: OnRepositoryItemClick
) {

    val list = items.repositories
    val pagination = items.pagination
    if (pagination == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    } else if (pagination.count == 0) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("検索結果はありません", modifier = Modifier.align(Alignment.Center))
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Column {
                if (title != null) {
                    Text(title, fontSize = 24.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(items.pagination.count.toStringWithSiUnitSuffix())
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "repository results",
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        list.forEach { repository ->
            item {
                GitHubRepositoryListItemCard(
                    repository,
                    onItemClicked = onItemClicked,
                    onItemStarClicked = onItemStarClick
                )
            }
        }

        if (pagination.hasNext) {
            item {
                Box(contentAlignment = Alignment.Center) {
                    LoadingIndicator()

                    // Composableが入場した（リストの下部までスクロールした）際に次ページ読み込み処理を発火させる
                    // LaunchedEffectは、Composableの退場によってCoroutineがキャンセルされてしまうので使用しない。
                    // https://developer.android.com/jetpack/compose/side-effects?hl=ja#launchedeffect
                    SideEffect {
                        gitHubInfiniteScrollable.next(pagination)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    CircularProgressIndicator(
        strokeWidth = 2.dp,
        modifier = Modifier
            .padding(16.dp)
            .width(24.dp)
    )
}
