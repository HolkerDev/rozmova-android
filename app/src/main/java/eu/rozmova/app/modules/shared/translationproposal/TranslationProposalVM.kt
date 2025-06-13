package eu.rozmova.app.modules.shared.translationproposal

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class TranslationProposalState(
    val translatedTexts: List<String> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class TranslationProposalVM
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<TranslationProposalState, Unit> {
        override val container: Container<TranslationProposalState, Unit> =
            container(
                TranslationProposalState(),
            )

        fun translatePhrase(phrase: String) =
            intent {
                reduce { state.copy(isLoading = true) }
                val currentTranslations = state.translatedTexts as ArrayList<String>
                currentTranslations.add("translated phrase")
                reduce { state.copy(translatedTexts = currentTranslations, isLoading = false) }
            }
    }
