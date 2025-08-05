package eu.rozmova.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.billingclient.api.Purchase
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.clients.backend.VerificationClient
import eu.rozmova.app.clients.backend.VerifySubscriptionReq
import eu.rozmova.app.nav.NavRoutes
import eu.rozmova.app.nav.NavigationHost
import eu.rozmova.app.nav.bottomNavigationItems
import eu.rozmova.app.repositories.AuthRepository
import eu.rozmova.app.repositories.AuthState
import eu.rozmova.app.repositories.OnboardingRepository
import eu.rozmova.app.repositories.billing.SubscriptionRepository
import eu.rozmova.app.services.billing.BillingEvents
import eu.rozmova.app.services.billing.BillingService
import eu.rozmova.app.ui.theme.RozmovaTheme
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.compose.collectSideEffect
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

sealed class AppState {
    data object Loading : AppState()

    data object Authenticated : AppState()

    data object Unauthenticated : AppState()
}

sealed interface AppEvent {
    data object NavigateToOnboarding : AppEvent

    data object NavigateToLearn : AppEvent
}

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val onboardingRepository: OnboardingRepository,
        private val billingService: BillingService,
        private val subscriptionRepository: SubscriptionRepository,
        private val verificationClient: VerificationClient,
    ) : ViewModel(),
        ContainerHost<Unit, AppEvent> {
        override val container: Container<Unit, AppEvent> = container(Unit)

        init {
            viewModelScope.launch {
                authRepository.observeAuthState()
            }

            viewModelScope.launch {
                billingService.billingState.collect { billingEvent ->
                    when (billingEvent) {
                        BillingEvents.Loading -> {
                            Log.d("AppViewModel", "Billing service is loading")
                        }

                        is BillingEvents.PurchaseFound -> {
                            Log.d("AppViewModel", "Purchase found: ${billingEvent.purchase}")
                            handlePurchaseFound(billingEvent.purchase)
                        }

                        BillingEvents.NoPurchaseFound -> {
                            Log.d("AppViewModel", "No purchase found")
                            val isVIPResp = verificationClient.verifyVIP()
                            if (!isVIPResp.isSuccessful) {
                                Log.e(
                                    "AppViewModel",
                                    "Failed to verify VIP status: ${isVIPResp.body()}",
                                )
                                subscriptionRepository.setIsSubscribed(false)
                                return@collect
                            }
                            if (isVIPResp.body()?.isVIP == true) {
                                Log.i("AppViewModel", "User is VIP")
                                subscriptionRepository.setIsSubscribed(true)
                            } else {
                                Log.i("AppViewModel", "User is not VIP")
                                subscriptionRepository.setIsSubscribed(false)
                            }
                        }
                    }
                }
            }
        }

        fun checkOnboarding() =
            intent {
                val isComplete = onboardingRepository.isOnboardingComplete()
                if (isComplete) {
                    postSideEffect(AppEvent.NavigateToOnboarding)
                    return@intent
                }

                postSideEffect(AppEvent.NavigateToLearn)
            }

        private suspend fun handlePurchaseFound(purchase: Purchase) {
            val response = verificationClient.verifyToken(VerifySubscriptionReq(purchase.purchaseToken))
            if (!response.isSuccessful) {
                Log.e("AppViewModel", "Failed to verify subscription: ${response.errorBody()?.string()}")
                subscriptionRepository.setIsSubscribed(false)
                return
            }
            val isSubscribed = response.body()?.isSubscribed == true
            if (isSubscribed) {
                if (purchase.isAcknowledged.not()) {
                    Log.i("AppViewModel", "Acknowledging purchase: ${purchase.orderId}")
                    billingService.acknowledgePurchase(purchase)
                }
                subscriptionRepository.setIsSubscribed(true)
                Log.i("AppViewModel", "User is subscribed")
            } else {
                subscriptionRepository.setIsSubscribed(false)
                Log.i("AppViewModel", "User is not subscribed")
            }
        }

        val appState =
            authRepository.authState
                .map { authState ->
                    when (authState) {
                        is AuthState.Loading -> AppState.Loading
                        is AuthState.Authenticated -> {
                            billingService.initialize()
                            AppState.Authenticated
                        }

                        is AuthState.Unauthenticated -> AppState.Unauthenticated
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = AppState.Loading,
                )
    }

@Composable
private fun App(viewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val bottomNavScreens =
        listOf(NavRoutes.Learn, NavRoutes.Chats, NavRoutes.Library, NavRoutes.Settings)
    val appState by viewModel.appState.collectAsState()

    viewModel.collectSideEffect { event ->
        when (event) {
            AppEvent.NavigateToOnboarding -> {
                Log.i("MainActivity", "Navigating to Onboarding")
                navController.navigate(NavRoutes.Onboarding.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            AppEvent.NavigateToLearn -> {
                navController.navigate(NavRoutes.Learn.route) {
                    popUpTo(NavRoutes.Main.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    RozmovaTheme {
        Scaffold(
            bottomBar = {
                val route = getCurrentRoute(navController) ?: ""
                if (route in bottomNavScreens.map { it.route }) {
                    BottomNavBar(route, navController)
                }
            },
            contentWindowInsets = WindowInsets.navigationBars,
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            NavigationHost(navController, innerPadding)

            LaunchedEffect(appState) {
                val currentDestination = navController.currentDestination?.route
                when (appState) {
                    AppState.Loading -> {
                    }

                    AppState.Authenticated -> {
                        if (currentDestination == NavRoutes.Main.route ||
                            currentDestination == NavRoutes.Login.route
                        ) {
                            viewModel.checkOnboarding()
                        }
                    }

                    AppState.Unauthenticated -> {
                        if (currentDestination == NavRoutes.Login.route) {
                            return@LaunchedEffect
                        }
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String,
    navController: NavHostController,
) {
    NavigationBar {
        bottomNavigationItems().forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon!!,
                        contentDescription = stringResource(screen.labelResourceId!!),
                    )
                },
                label = { Text(stringResource(screen.labelResourceId!!)) },
                selected = currentRoute == screen.route,
                onClick = {
                    Log.i("MainActivity", "Navigating to ${screen.route} from $currentRoute")
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(0) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun getCurrentRoute(navController: NavHostController): String? {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    return navBackStackEntry?.destination?.route
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!BuildConfig.DEBUG) {
            SentryAndroid.init(this) { options ->
                options.isAttachScreenshot = true
                options.dsn = "https://2f208e0f457154130d4569b7c1326ef4@o4509570570649600.ingest.de.sentry.io/4509570577072208"
                options.tracesSampleRate = 1.0
            }
        }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App()
        }
    }
}
