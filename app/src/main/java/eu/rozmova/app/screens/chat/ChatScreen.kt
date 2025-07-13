package eu.rozmova.app.screens.chat

import androidx.compose.runtime.Composable
import eu.rozmova.app.components.conversationchat.ConversationChat
import eu.rozmova.app.components.messagechat.MessageChat
import eu.rozmova.app.domain.ChatType

@Composable
fun ChatScreen(
    chatId: String,
    chatType: ChatType,
    onBack: () -> Unit,
    onMain: () -> Unit,
    onNavigateToSubscription: () -> Unit,
) {
    when (chatType) {
        ChatType.WRITING -> {
            MessageChat(
                chatId = chatId,
                onReviewAccept = onMain,
                onBackClick = onBack,
                navigateToSubscription = onNavigateToSubscription,
            )
        }
        ChatType.SPEAKING -> {
            ConversationChat(
                chatId = chatId,
                onBackClick = onBack,
                onReviewAccept = onMain,
                onNavigateToSubscription = onNavigateToSubscription,
            )
        }
    }
}
