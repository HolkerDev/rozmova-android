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
import eu.rozmova.app.modules.allscenarios.AllScenariosScreen
import eu.rozmova.app.modules.chat.ChatScreen
import eu.rozmova.app.modules.chatlist.ChatsListScreen
import eu.rozmova.app.modules.createchat.CreateChatScreen
import eu.rozmova.app.modules.devscreen.DevScreen
import eu.rozmova.app.modules.generatechat.GenerateChatScreen
import eu.rozmova.app.modules.library.LibraryNavigation
import eu.rozmova.app.modules.library.LibraryScreen
import eu.rozmova.app.modules.onboarding.OnboardingScreen
import eu.rozmova.app.modules.review.ReviewScreen
import eu.rozmova.app.modules.reviewlist.ReviewListScreen
import eu.rozmova.app.modules.subscription.SubscriptionScreen
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
                navController.navigate(NavRoutes.Chat.routeWith(chatId))
            })
        }

        composable(NavRoutes.Learn.route) {
            LearnScreen(
                startOnboarding = { navController.navigate(NavRoutes.Onboarding.route) },
                toChat = { chatId, chatType ->
                    navController.navigate(NavRoutes.Chat.routeWith(chatId))
                },
                toCreateChat = { scenarioId ->
                    navController.navigate(NavRoutes.CreateChat.routeWith(scenarioId))
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
                            navController.navigate(NavRoutes.DevScreen.route)
                        }

                        override fun toCompleteScenarios() {
                            navController.navigate(NavRoutes.ReviewList.route)
                        }

                        override fun toTeacherIntegration() {
                            navController.navigate(NavRoutes.DevScreen.route)
                        }
                    },
            )
        }

        composable(
            NavRoutes.CreateChat.route,
            arguments = listOf(navArgument("scenarioId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val scenarioId: String = backStackEntry.arguments?.getString("scenarioId") ?: ""
            CreateChatScreen(
                scenarioId = scenarioId,
                toSubscription = {
                    navController.navigate(NavRoutes.Subscription.route)
                },
                toChat = { chatId ->
                    navController.navigate(NavRoutes.Chat.routeWith(chatId))
                },
                back = {
                    navController.navigateUp()
                },
            )
        }

        composable(route = NavRoutes.AllScenarios.route) {
            AllScenariosScreen(
                back = { navController.navigateUp() },
                toChatCreate = { scenarioId ->
                    navController.navigate(NavRoutes.CreateChat.routeWith(scenarioId))
                },
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
                onChatReady = { chatId, chatType ->
                    navController.navigate(
                        NavRoutes.Chat.routeWith(
                            chatId,
                        ),
                    )
                },
                onBack = {
                    navController.navigateUp()
                },
                toSubscription = {
                    navController.navigate(NavRoutes.Subscription.route)
                },
            )
        }

        composable(route = NavRoutes.DevScreen.route) {
            DevScreen(back = { navController.navigateUp() })
        }

        composable(
            route = NavRoutes.Review.route,
            arguments = listOf(navArgument("reviewId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val reviewId: String = backStackEntry.arguments?.getString("reviewId") ?: ""
            ReviewScreen(
                reviewId = reviewId,
                onClose = {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    if (previousRoute == NavRoutes.Chat.route) {
                        navController.navigate(NavRoutes.Learn.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                        return@ReviewScreen
                    }

                    navController.navigateUp()
                },
            )
        }

        composable(route = NavRoutes.ReviewList.route) {
            ReviewListScreen(
                back = { navController.navigateUp() },
                toReview = { reviewId ->
                    navController.navigate(NavRoutes.Review.routeWith(reviewId))
                },
            )
        }

        // Side Screens
        composable(
            route = NavRoutes.Chat.route,
            arguments =
                listOf(
                    navArgument("chatId") { type = NavType.StringType },
                ),
        ) { backStackEntry ->
            val chatId: String = backStackEntry.arguments?.getString("chatId") ?: ""

            ChatScreen(
                chatId = chatId,
                back = {
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    if (previousRoute == NavRoutes.GenerateScenario.route || previousRoute == NavRoutes.CreateChat.route) {
                        // Clear everything back to Learn and recreate bottom nav state
                        navController.navigate(NavRoutes.Learn.route) {
                            popUpTo(0) {
                                inclusive = true // Clear everything
                            }
                        }
                    } else {
                        navController.navigateUp()
                    }
                },
                toReview = { reviewId ->
                    navController.popBackStack()
                    navController.navigate(NavRoutes.Review.routeWith(reviewId))
                },
                toSubscription = {
                    navController.navigate(NavRoutes.Subscription.route)
                },
            )
        }
    }
}
