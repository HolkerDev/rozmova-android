package eu.rozmova.app.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ChatDetailScreen(onBackClicked: () -> Unit, chatId: String) {
    Text("Chat detail screen for chat with id $chatId")
}