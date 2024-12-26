package eu.rozmova.app.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.GoogleSignInButton
import eu.rozmova.app.components.ThemedLogo

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val onSignInClick = {
        viewModel.login()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ThemedLogo()
            GoogleSignInButton(isLoading = state == LoginState.Loading, onClick = onSignInClick)
        }
    }
}
