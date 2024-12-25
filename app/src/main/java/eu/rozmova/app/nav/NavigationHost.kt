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
import eu.rozmova.app.screens.ChatsListScreen
import eu.rozmova.app.screens.login.LoginScreen
import eu.rozmova.app.screens.SettingsScreen
import eu.rozmova.app.screens.chatdetails.ChatDetailScreen
import eu.rozmova.app.screens.main.MainScreen

@Composable
fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Main.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Main Screens
        composable(NavRoutes.Chats.route) {
            ChatsListScreen(onChatSelected = { chatId ->
                navController.navigate("chat_details/$chatId")
            })
        }
        composable(route = NavRoutes.Main.route){
            MainScreen()
        }
        composable(route = NavRoutes.Settings.route){
            SettingsScreen()
        }

        composable(route = NavRoutes.Login.route) {
            LoginScreen()
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