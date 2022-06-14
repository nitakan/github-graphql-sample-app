package net.nitakan.githubsearcher.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.nitakan.githubsearcher.ui.theme.AppTheme

@Composable
fun App() {
    val systemUiController = rememberSystemUiController()
    val navController = rememberNavController()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
        )
    }
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Router.Routes(navController, initialRoute = Router.HOME)
        }
    }
}
