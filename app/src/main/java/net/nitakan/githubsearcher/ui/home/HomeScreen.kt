package net.nitakan.githubsearcher.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import net.nitakan.githubsearcher.ui.Router


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel, navController: NavController) {
    val scrollState = rememberTopAppBarScrollState()
    val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior( scrollState ) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmallTopAppBar(
                title = {
                    Text("GitHub Repositories")
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = {
                        navController.navigate("${Router.SEARCH}?query=")
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "検索")
                    }
                }
            )
        }
    ) { padding ->
        PopularRepositoriesContent(homeViewModel, padding, onRepositoryStarClick = {
            homeViewModel.toggleStar(it)
        }) {
            navController.navigate("${Router.REPOSITORY}/${it.owner.name}/${it.name}")
        }
    }
}


