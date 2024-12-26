package eu.rozmova.app.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoutes(
    val route: String,
    val icon: ImageVector? = null,
    val label: String? = route,
) {
    data object Main : NavRoutes(route = "main")

    data object Chats : NavRoutes(route = "chats", icon = Icons.Default.ChatBubble, label = "Chats")

    data object ChatDetails : NavRoutes(route = "chat_details/{chatId}")

    data object Settings :
        NavRoutes(route = "settings", icon = Icons.Default.Settings, label = "Settings")

    data object CreateChat : NavRoutes(route = "create_chat")

    data object Login : NavRoutes(route = "login")
}

fun bottomNavigationItems() =
    listOf(
        NavRoutes.Chats,
        NavRoutes.Settings,
    )
