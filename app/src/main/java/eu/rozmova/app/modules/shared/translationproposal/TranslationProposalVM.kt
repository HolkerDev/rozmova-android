package eu.rozmova.app.modules.shared.translationproposal

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.TranslationProposal
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.repositories.TranslationRepository
import eu.rozmova.app.repositories.UsageLimitReachedException
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class TranslationProposalState(
    val translatedTexts: List<TranslationProposal> = emptyList(),
    val isLoading: Boolean = false,
)

sealed class TranslationEvents {
    data object ClearInput : TranslationEvents()

    data object ShowUsageLimitReached : TranslationEvents()
}

@HiltViewModel
class TranslationProposalVM
    @Inject
    constructor(
        private val translationRepository: TranslationRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel(),
        ContainerHost<TranslationProposalState, TranslationEvents> {
        override val container: Container<TranslationProposalState, TranslationEvents> =
            container(
                TranslationProposalState(),
            )
        private val tag = "TranslationProposalVM"

        fun translatePhrase(
            phrase: String,
            chatId: String,
        ) = intent {
            reduce { state.copy(isLoading = true) }
            val learnLanguage = settingsRepository.getLearningLangOrDefault()
            val userLang = settingsRepository.getInterfaceLang()
            translationRepository
                .genProposal(phrase, learnLanguage, userLang, chatId)
                .onSuccess { translationProposal ->
                    val currentTranslations = state.translatedTexts.toMutableList()
                    currentTranslations.add(0, translationProposal)
                    reduce { state.copy(translatedTexts = currentTranslations, isLoading = false) }
                    postSideEffect(TranslationEvents.ClearInput)
                }.onFailure { error ->
                    if (error is UsageLimitReachedException) {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(TranslationEvents.ShowUsageLimitReached)
                        return@onFailure
                    }
                    Log.e(tag, "Error generating translation proposal: $error")
                    reduce { state.copy(isLoading = false) }
                }
        }
    }
