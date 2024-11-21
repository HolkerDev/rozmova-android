package eu.rozmova.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import eu.rozmova.app.components.SimpleToolBar

@Composable
fun ChatDetailScreen(onBackClicked: () -> Unit, chatId: String) {
    Column {
        SimpleToolBar(title = chatId, onBack = onBackClicked)
        Text("Chat detail screen for chat with id $chatId")
    }
}