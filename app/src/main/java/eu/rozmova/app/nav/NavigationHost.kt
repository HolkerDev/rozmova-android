package eu.rozmova.app.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import eu.rozmova.app.screens.chatdetails.ChatDetailScreen
import eu.rozmova.app.screens.chats.ChatsListScreen
import eu.rozmova.app.screens.createchat.CreateChatScreen
import eu.rozmova.app.screens.learn.LearnScreen
import eu.rozmova.app.screens.login.LoginScreen
import eu.rozmova.app.screens.main.MainScreen
import eu.rozmova.app.screens.settings.SettingsScreen

@Composable
fun NavigationHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Main.route,
        modifier = modifier.padding(innerPadding),
    ) {
        // Main Screens
        composable(NavRoutes.Chats.route) {
            ChatsListScreen(onChatSelect = { chatId ->
                navController.navigate("chat_details/$chatId")
            }, onChatCreateClick = { navController.navigate(NavRoutes.CreateChat.route) })
        }

        composable(NavRoutes.Learn.route) {
            LearnScreen(
                navigateToChat = { id ->
                    navController.navigate(NavRoutes.ChatDetails.routeWith(id))
                },
            )
        }

        composable(route = NavRoutes.Main.route) {
            MainScreen()
        }
        composable(route = NavRoutes.Settings.route) {
            SettingsScreen()
        }

        composable(route = NavRoutes.Login.route) {
            LoginScreen()
        }

        composable(route = NavRoutes.CreateChat.route) {
            CreateChatScreen(onChatReady = { chatId ->
                navController.navigate(NavRoutes.ChatDetails.routeWith(chatId)) {
                    popUpTo(NavRoutes.Chats.route)
                }
            }, onBack = { navController.navigateUp() })
        }

        // Side Screens
        composable(
            route = NavRoutes.ChatDetails.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val chatId: String = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatDetailScreen(
                onBackClick = { navController.navigateUp() },
                onChatArchive = {
                    navController.navigate(NavRoutes.Learn.route) {
                        popUpTo(NavRoutes.Learn.route)
                    }
                },
                chatId,
            )
        }
    }
}
