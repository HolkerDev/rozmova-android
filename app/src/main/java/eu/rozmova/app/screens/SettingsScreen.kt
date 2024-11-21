package eu.rozmova.app.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amplifyframework.core.Amplify

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings Screen")
        Button(
            onClick = {
                Amplify.Auth.signOut { Log.d("AuthQuickstart", "Signed out") }
            }
        ) {
            Text("Log out")
        }
    }
}