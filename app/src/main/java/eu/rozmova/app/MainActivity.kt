package eu.rozmova.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import arrow.core.getOrElse
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.nav.NavRoutes
import eu.rozmova.app.nav.NavigationHost
import eu.rozmova.app.nav.bottomNavigationItems
import eu.rozmova.app.repositories.AuthRepository
import eu.rozmova.app.repositories.AuthState
import eu.rozmova.app.repositories.UserRepository
import eu.rozmova.app.services.FeatureService
import eu.rozmova.app.ui.theme.RozmovaTheme
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppState {
    data object Loading : AppState()

    data object Authenticated : AppState()

    data object Unauthenticated : AppState()
}

@HiltViewModel
class AppViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
        private val featureService: FeatureService,
    ) : ViewModel() {
        init {
            viewModelScope.launch {
                authRepository.observeAuthState()
            }
        }

        fun initializeFeatureService(userSession: UserSession) =
            viewModelScope.launch {
                val userId = userSession.user?.id ?: throw IllegalStateException("User not found")
                userRepository
                    .fetchUserGroups(userId)
                    .also { Log.i("AppViewModel", "userGroups: $it") }
                    .getOrElse { emptyList() }
                    .let { userGroups ->
                        featureService.initialize(userGroups)
                    }
            }

        val appState =
            authRepository.authState
                .map { authState ->
                    Log.i("AppViewModel", "authState: $authState")
                    when (authState) {
                        is AuthState.Loading -> AppState.Loading
                        is AuthState.Authenticated -> {
                            initializeFeatureService(authState.userSession)
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
    val bottomNavScreens = listOf(NavRoutes.Chats, NavRoutes.Settings)
    val appState by viewModel.appState.collectAsState()

    RozmovaTheme {
        Scaffold(
            bottomBar = {
                val currentRoute = getCurrentRoute(navController) ?: ""
                if (currentRoute in bottomNavScreens.map { it.route }) {
                    BottomNavBar(currentRoute, navController)
                }
            },
            contentWindowInsets = WindowInsets.statusBars,
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            NavigationHost(navController, innerPadding)

            LaunchedEffect(appState) {
                when (appState) {
                    AppState.Loading -> {
                    }

                    AppState.Authenticated -> {
                        navController.navigate(NavRoutes.Chats.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    AppState.Unauthenticated -> {
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
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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

        setContent {
            App()
        }
    }
}
