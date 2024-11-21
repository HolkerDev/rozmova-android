package eu.rozmova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amplifyframework.ui.authenticator.ui.Authenticator
import dagger.hilt.android.AndroidEntryPoint
import eu.rozmova.app.screens.ChatDetailScreen
import eu.rozmova.app.screens.ChatsListScreen
import eu.rozmova.app.screens.SettingsScreen
import eu.rozmova.app.ui.theme.RozmovaTheme

sealed class MainScreensNav(val route: String, val icon: ImageVector, val label: String) {
    object Home : MainScreensNav("chats", Icons.Default.ChatBubble, "Chats")
    object Settings : MainScreensNav("settings", Icons.Default.Settings, "Settings")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val bottomNavScreens = listOf(MainScreensNav.Home, MainScreensNav.Settings)

            RozmovaTheme {
                Authenticator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.systemBars.asPaddingValues())
                ) {
                    Scaffold(bottomBar = {
                        val currentRoute = getCurrentRoute(navController) ?: ""
                        if (currentRoute in bottomNavScreens.map { it.route }) {
                            BottomNavBar(bottomNavScreens, currentRoute, navController)
                        }
                    }) { innerPadding ->
                        NavigationHost(navController, innerPadding)
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomNavBar(
        screens: List<MainScreensNav>, currentRoute: String, navController: NavHostController
    ) {
        NavigationBar {
            screens.forEach { screen ->
                NavigationBarItem(icon = {
                    Icon(
                        screen.icon, contentDescription = screen.label
                    )
                },
                    label = { Text(screen.label) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
            }
        }
    }

    @Composable
    private fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
        NavHost(
            navController = navController,
            startDestination = "chats",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main Screens
            composable(route = "chats") {
                ChatsListScreen(onChatSelected = { chatId ->
                    navController.navigate("chat_details/$chatId")
                })
            }
            composable(route = "settings") {
                SettingsScreen()
            }

            // Side Screens
            composable(
                route = "chat_details/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId: String = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatDetailScreen(onBackClicked = { navController.navigateUp() }, chatId)
            }
        }
    }

    @Composable
    private fun getCurrentRoute(navController: NavHostController): String? {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        return navBackStackEntry?.destination?.route
    }
}
