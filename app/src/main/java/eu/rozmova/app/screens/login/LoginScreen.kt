package eu.rozmova.app.screens.login

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.components.GoogleSignInButton
import eu.rozmova.app.components.ThemedLogo
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val activity = LocalContext.current as Activity

    fun onSignInClick() {
        viewModel.login(activity)
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
            GoogleSignInButton(isLoading = state.isLoading, onClick = { onSignInClick() })
            state.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(4.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
