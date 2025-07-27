package eu.rozmova.app.modules.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.state.AppStateRepository
import eu.rozmova.app.utils.LocaleManager
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class OnboardingScreenState(
    val selectedLanguage: String = "en",
)

@HiltViewModel
class OnboardingVM
    @Inject
    constructor(
        private val localeManager: LocaleManager,
        private val settingsRepository: SettingsRepository,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<OnboardingScreenState, Unit> {
        override val container = container<OnboardingScreenState, Unit>(OnboardingScreenState())

        fun selectLanguage(languageCode: String) {
            intent {
                localeManager.setLocale(languageCode)
                reduce { state.copy(selectedLanguage = languageCode) }
            }
        }

        fun completeOnboarding() =
            intent {
                settingsRepository.setOnboardingComplete()
                appStateRepository.triggerRefetch()
            }

        fun getCurrentLanguage(): String = localeManager.getCurrentLocale().language

        fun savePronoun(salutationCode: String) =
            intent {
                settingsRepository.setPronounCode(salutationCode)
            }

        fun saveLearningLanguage(learningLang: String) =
            intent {
                settingsRepository.setLearningLang(learningLang)
            }
    }
