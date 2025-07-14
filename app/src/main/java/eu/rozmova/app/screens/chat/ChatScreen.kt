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
    toReview: (reviewId: String) -> Unit,
    onNavigateToSubscription: () -> Unit,
) {
    when (chatType) {
        ChatType.WRITING -> {
            MessageChat(
                chatId = chatId,
                onReviewAccept = onMain,
                onBackClick = onBack,
                navigateToSubscription = onNavigateToSubscription,
                toReview = toReview,
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
