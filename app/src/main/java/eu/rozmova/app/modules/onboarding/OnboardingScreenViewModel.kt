package eu.rozmova.app.modules.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.utils.LocaleManager
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class OnboardingScreenState(
    val selectedLanguage: String = "en",
)

@HiltViewModel
class OnboardingScreenViewModel
    @Inject
    constructor(
        private val localeManager: LocaleManager,
    ) : ViewModel(),
        ContainerHost<OnboardingScreenState, Unit> {
        override val container = container<OnboardingScreenState, Unit>(OnboardingScreenState())

        fun selectLanguage(languageCode: String) {
            intent {
                localeManager.setLocale(languageCode)
                reduce { state.copy(selectedLanguage = languageCode) }
            }
        }

        fun getCurrentLanguage(): String = localeManager.getCurrentLocale().language

        fun completeOnboarding() {
            intent {
                // Logic to mark onboarding as complete
            }
        }
    }
