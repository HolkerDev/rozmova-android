package eu.rozmova.app.nav

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import eu.rozmova.app.R

sealed class NavRoutes(
    val route: String,
    val icon: ImageVector? = null,
    val labelResourceId: Int? = null,
) {
    data object Main : NavRoutes(route = "main")

    data object Learn : NavRoutes(route = "learn", icon = Icons.Default.Book, labelResourceId = R.string.bottom_nav_learn)

    data object Chats : NavRoutes(
        route = "chats",
        icon = Icons.Default.ChatBubble,
        labelResourceId = R.string.bottom_nav_chats,
    )

    data object ChatDetails : NavRoutes(route = "chat_details/{chatId}") {
        fun routeWith(chatId: String) = "chat_details/$chatId"
    }

    data object Settings : NavRoutes(
        route = "settings",
        icon = Icons.Default.Settings,
        labelResourceId = R.string.bottom_nav_settings,
    )

    data object CreateChat : NavRoutes(route = "create_chat")

    data object Login : NavRoutes(route = "login")

    fun getLabel(context: Context): String? = labelResourceId?.let { context.getString(it) }
}

fun bottomNavigationItems() =
    listOf(
        NavRoutes.Learn,
        NavRoutes.Chats,
        NavRoutes.Settings,
    )
