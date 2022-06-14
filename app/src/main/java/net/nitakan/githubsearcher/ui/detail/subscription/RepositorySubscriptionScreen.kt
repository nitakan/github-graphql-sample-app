@file:OptIn(ExperimentalMaterial3Api::class)

package net.nitakan.githubsearcher.ui.detail.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.nitakan.githubsearcher.model.entities.GitHubViewSubscription
import net.nitakan.githubsearcher.model.exceptions.NotFoundException
import net.nitakan.githubsearcher.ui.widgets.NotFoundRepositoryCard
import net.nitakan.githubsearcher.ui.widgets.RetryableCard

@Composable
fun RepositorySubscriptionScreen(
    viewModel: RepositorySubscriptionViewModel,
    navController: NavController,
) {
    val scrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior( scrollState ) }

    val state by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val isUpdating by viewModel.isUpdating.collectAsState()
    val subscription by viewModel.selectedSubscription.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                title = {
                    Text("Notification")
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            state.onFailure { error ->
                when (error) {
                    is NotFoundException -> {
                        NotFoundRepositoryCard(modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        RetryableCard(modifier = Modifier.align(Alignment.Center)) {
                            viewModel.retry()
                        }
                    }
                }
            }.onSuccess { repository ->
                if (isLoading || repository == null) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                    return@onSuccess
                }

                Column {
                    // UNSUBSCRIBE
                    SubscriptionSelect(
                        subscription = GitHubViewSubscription.UNSUBSCRIBED,
                        current = subscription,
                        title = "参加と@メンション",
                        body = "参加時または@メンションされた場合にのみ、このリポジトリからの通知を受け取ります。",
                        onClick = viewModel::selectSubscription
                    )
                    // SUBSCRIBE
                    SubscriptionSelect(
                        subscription = GitHubViewSubscription.SUBSCRIBED,
                        current = subscription,
                        title = "すべてのアクティビティ",
                        body = "このリポジトリのすべての通知を受け取ります。",
                        onClick = viewModel::selectSubscription
                    )

                    // IGNORE
                    SubscriptionSelect(
                        subscription = GitHubViewSubscription.IGNORED,
                        current = subscription,
                        title = "無視",
                        body = "通知されることはありません。",
                        onClick = viewModel::selectSubscription
                    )

                    Box(Modifier.weight(1.0f))

                    Box(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Button(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            enabled = !isUpdating,
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.updateSubscription()
                                    navController.popBackStack()
                                }
                            },
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionSelect(
    subscription: GitHubViewSubscription,
    current: GitHubViewSubscription,
    title: String,
    body: String,
    onClick: (subscription: GitHubViewSubscription) -> Unit,
) {
    Row(
        Modifier
            .clickable {
                onClick(subscription)
            }
            .padding(16.dp)) {
        Column(Modifier.weight(1.0f)) {
            Text(title)
            Text(body)
        }
        RadioButton(selected = subscription == current, onClick = { onClick(subscription) })
    }
}