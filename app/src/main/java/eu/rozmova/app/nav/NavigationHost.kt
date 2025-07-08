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
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.toScenarioType
import eu.rozmova.app.modules.allscenarios.AllScenariosScreen
import eu.rozmova.app.modules.chatlist.ChatsListScreen
import eu.rozmova.app.modules.generatechat.GenerateChatScreen
import eu.rozmova.app.modules.library.LibraryNavigation
import eu.rozmova.app.modules.library.LibraryScreen
import eu.rozmova.app.modules.onboarding.OnboardingScreen
import eu.rozmova.app.modules.subscription.SubscriptionScreen
import eu.rozmova.app.screens.chat.ChatScreen
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
            ChatsListScreen(onChatSelect = { chatId, scenarioType ->
                navController.navigate("chat/$chatId/$scenarioType")
            }, onChatCreateClick = { navController.navigate(NavRoutes.GenerateScenario.route) })
        }

        composable(NavRoutes.Learn.route) {
            LearnScreen(
                startOnboarding = { navController.navigate(NavRoutes.Onboarding.route) },
                navigateToChat = { chatId, scenarioType ->
                    navController.navigate(NavRoutes.Chat.routeWith(chatId, scenarioType))
                },
            )
        }

        composable(NavRoutes.Library.route) {
            LibraryScreen(
                navigation =
                    object : LibraryNavigation {
                        override fun toAllScenarios() {
                            navController.navigate(NavRoutes.AllScenarios.route)
                        }

                        override fun toScenarioGeneration() {
                            navController.navigate(NavRoutes.GenerateScenario.route)
                        }

                        override fun toChecklist() {
                            TODO("Not yet implemented")
                        }

                        override fun toCompleteScenarios() {
                            TODO("Not yet implemented")
                        }

                        override fun toTeacherIntegration() {
                            TODO("Not yet implemented")
                        }
                    },
            )
        }

        composable(route = NavRoutes.AllScenarios.route) {
            AllScenariosScreen(
                navigateToChat = { chatId, scenarioType ->
                    navController.navigate(NavRoutes.Chat.routeWith(chatId, scenarioType.toScenarioType()))
                },
                back = { navController.navigateUp() },
            )
        }

        composable(route = NavRoutes.Main.route) {
            MainScreen()
        }
        composable(route = NavRoutes.Settings.route) {
            SettingsScreen(
                onNavigateToSubscription = { navController.navigate(NavRoutes.Subscription.route) },
            )
        }

        composable(route = NavRoutes.Subscription.route) {
            SubscriptionScreen(
                onNavigateBack = { navController.navigateUp() },
            )
        }

        composable(route = NavRoutes.Login.route) {
            LoginScreen()
        }

        composable(route = NavRoutes.Onboarding.route) {
            OnboardingScreen(onLearn = {
                navController.navigate(NavRoutes.Learn.route) {
                    popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(route = NavRoutes.GenerateScenario.route) {
            GenerateChatScreen(
                onChatReady = { chatId, scenarioType ->
                    navController.navigate(NavRoutes.Chat.routeWith(chatId, scenarioType.toScenarioType()))
                },
                onBack = {
                    navController.navigateUp()
                },
            )
        }

        // Side Screens
        composable(
            route = NavRoutes.Chat.route,
            arguments =
                listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("scenarioType") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val chatId: String = backStackEntry.arguments?.getString("chatId") ?: ""
            val scenarioType =
                backStackEntry.arguments?.getString("scenarioType")?.let { ScenarioType.valueOf(it) } ?: ScenarioType.CONVERSATION
            ChatScreen(
                chatId = chatId,
                scenarioType = scenarioType,
                onBack = {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    if (previousRoute == NavRoutes.GenerateScenario.route) {
                        navController.navigate(NavRoutes.Learn.route) {
                            popUpTo(NavRoutes.GenerateScenario.route) { inclusive = true }
                        }
                    } else {
                        navController.navigateUp()
                    }
                },
                onMain = { navController.navigate(NavRoutes.Learn.route) },
                onNavigateToSubscription = {
                    navController.navigate(NavRoutes.Subscription.route)
                },
            )
        }
    }
}
