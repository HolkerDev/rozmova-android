package eu.rozmova.app.screens.chat

import android.util.Log
import androidx.compose.runtime.Composable
import eu.rozmova.app.components.conversationchat.ConversationChat
import eu.rozmova.app.components.messagechat.MessageChat
import eu.rozmova.app.domain.ScenarioType

@Composable
fun ChatScreen(
    chatId: String,
    scenarioType: ScenarioType,
    onBack: () -> Unit,
    onMain: () -> Unit,
) {
    Log.i("ChatScreen", "ChatScreen: $chatId, $scenarioType")
    when (scenarioType) {
        ScenarioType.MESSAGES -> {
            MessageChat(
                chatId = chatId,
                onReviewAccept = onMain,
                onBackClick = onBack,
            )
        }
        ScenarioType.CONVERSATION -> {
            ConversationChat(
                chatId = chatId,
                onBackClick = onBack,
                onChatArchive = onMain,
            )
        }
        else -> {
            Log.e("ChatScreen", "Unhandled scenario type: $scenarioType")
        }
    }
}
