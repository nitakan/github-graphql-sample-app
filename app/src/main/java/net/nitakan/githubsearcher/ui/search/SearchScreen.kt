@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package net.nitakan.githubsearcher.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Search
import net.nitakan.githubsearcher.model.entities.GitHubRepository
import net.nitakan.githubsearcher.ui.Router
import net.nitakan.githubsearcher.ui.widgets.GitHubRepositoryList
import net.nitakan.githubsearcher.ui.widgets.RetryableCard

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavController,
) {
    val scrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior( scrollState ) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val query = viewModel.currentQuery.collectAsState()
    val isQueryBlank = query.value.isBlank()

    fun search() {
        focusManager.clearFocus()
        viewModel.doSearch()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmallTopAppBar(
                title = {
                    BasicTextField(
                        query.value,
                        onValueChange = {
                            viewModel.updateQuery(it)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Search,
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.focusRequester(focusRequester),
                        decorationBox = { innerTextField ->
                            Box(
                                Modifier
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .fillMaxWidth()
                            ) {

                                if (isQueryBlank) {
                                    Text(
                                        "Search...",
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                // <-- Add this
                                innerTextField()
                            }
                        },

                        keyboardActions = KeyboardActions {
                            if (!isQueryBlank) {
                                search()
                            }
                        }
                    )

                    LaunchedEffect(true) {
                        if (isQueryBlank) {
                            focusRequester.requestFocus()
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    if (!isQueryBlank) {
                        IconButton(onClick = {
                            viewModel.updateQuery("")
                            focusRequester.requestFocus()
                        }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (query.value.isBlank()) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Icon(
                    FontAwesomeIcons.Solid.Search,
                    null,
                    Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    MaterialTheme.colorScheme.primary
                )
            }
            return@Scaffold
        }
        SearchResultContent(viewModel, padding, onClickStar = viewModel::toggleStar) {
            navController.navigate("${Router.REPOSITORY}/${it.owner.name}/${it.name}")
        }
    }
}

@Composable
fun SearchResultContent(
    viewModel: SearchViewModel,
    padding: PaddingValues,
    onClickStar: (repository: GitHubRepository) -> Unit,
    onClickItem: (repository: GitHubRepository) -> Unit
) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val result by viewModel.result.collectAsState()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = {
            viewModel.retry()
        }
    ) {
        result
            .onSuccess { items ->
                Box(modifier = Modifier.padding(padding)) {
                    GitHubRepositoryList(
                        items,
                        viewModel,
                        "Search Result",
                        onClickStar,
                        onClickItem
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
                            viewModel.retry()
                        },
                    )
                }
            }
    }

}