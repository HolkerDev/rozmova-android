package eu.rozmova.app.screens.login

import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.GoogleSignInButton

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = hiltViewModel()) {
    val onSignInClick = {
        viewModel.login()
    }

    Column(
        horizontalAlignment = CenterHorizontally, verticalArrangement = Center
    ) {
        GoogleSignInButton(onClick = onSignInClick)
    }
}
