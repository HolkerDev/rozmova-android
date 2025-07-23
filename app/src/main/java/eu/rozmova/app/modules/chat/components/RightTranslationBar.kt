package eu.rozmova.app.modules.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import eu.rozmova.app.modules.shared.translationproposal.TranslationProposalModal

@Composable
fun RightTranslationBar(
    chatId: String,
    show: Boolean,
    onClose: () -> Unit,
    toSubscription: () -> Unit,
) {
    AnimatedVisibility(
        visible = show,
        enter =
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300),
            ),
        exit =
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300),
            ),
    ) {
        TranslationProposalModal(
            chatId = chatId,
            onDismiss = onClose,
            toSubscription = toSubscription,
        )
    }
}
