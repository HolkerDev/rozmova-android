package eu.rozmova.app.nav

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LocalLibrary
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

    data object Review : NavRoutes(route = "review/{reviewId}") {
        fun routeWith(reviewId: String) = "review/$reviewId"
    }

    data object ReviewList : NavRoutes(route = "review_list")

    data object Chat : NavRoutes(route = "chat/{chatId}") {
        fun routeWith(chatId: String) = "chat/$chatId"
    }

    data object Library : NavRoutes(route = "library", icon = Icons.Default.LocalLibrary, labelResourceId = R.string.library_screen)

    data object AllScenarios : NavRoutes(route = "all_scenarios")

    data object CreateChat : NavRoutes(route = "create_chat/{scenarioId}") {
        fun routeWith(scenarioId: String) = "create_chat/$scenarioId"
    }

    data object UserInit : NavRoutes(route = "user_init?hobbies={hobbies}&job={job}&pronoun={pronoun}&level={level}") {
        fun routeWith(
            hobbies: List<String>,
            job: String?,
            pronoun: String,
            level: String,
        ) = "user_init?hobbies=${hobbies.joinToString(",")}&job=$job&pronoun=$pronoun&level=$level"
    }

    data object Settings : NavRoutes(
        route = "settings",
        icon = Icons.Default.Settings,
        labelResourceId = R.string.bottom_nav_settings,
    )

    data object Login : NavRoutes(route = "login")

    data object Onboarding : NavRoutes(route = "onboarding")

    data object Subscription : NavRoutes(route = "subscription")

    data object GenerateScenario : NavRoutes(route = "generate_scenario")

    data object DevScreen : NavRoutes(route = "dev_screen")

    fun getLabel(context: Context): String? = labelResourceId?.let { context.getString(it) }
}

fun bottomNavigationItems() =
    listOf(
        NavRoutes.Learn,
        NavRoutes.Library,
        NavRoutes.Chats,
        NavRoutes.Settings,
    )
