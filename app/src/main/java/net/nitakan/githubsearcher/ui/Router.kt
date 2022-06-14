package net.nitakan.githubsearcher.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.nitakan.githubsearcher.ui.detail.RepositoryDetailScreen
import net.nitakan.githubsearcher.ui.detail.subscription.RepositorySubscriptionScreen
import net.nitakan.githubsearcher.ui.home.HomeScreen
import net.nitakan.githubsearcher.ui.search.SearchScreen

object Router {
    const val HOME = "home"
    const val REPOSITORY = "repository"
    const val SEARCH = "search"
    const val REPOSITORY_SUBSCRIPTION = "repository/subscription"

    @Composable
    fun Routes(navController: NavHostController, initialRoute: String) {
        NavHost(navController = navController, startDestination = initialRoute) {
            // アプリトップ画面
            composable(HOME) { HomeScreen(hiltViewModel(), navController) }

            // GitHubリポジトリ詳細画面
            composable(
                "$REPOSITORY/{owner}/{name}",
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType })
            ) {
                RepositoryDetailScreen(hiltViewModel(), navController)
            }

            composable(
                "$SEARCH?query={query}",
                arguments = listOf(
                    navArgument("query") { type = NavType.StringType }
                )
            ) {
                SearchScreen(hiltViewModel(), navController)
            }

            composable(
                "${REPOSITORY_SUBSCRIPTION}/{owner}/{name}",
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType })
                ) {
                RepositorySubscriptionScreen(hiltViewModel(), navController)
            }
        }
    }
}