package eu.rozmova.app.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Language
import eu.rozmova.app.domain.getLanguageByCode
import eu.rozmova.app.repositories.AuthRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.utils.LocaleManager
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class LangSettings(
    val interfaceLang: Language = Language.ENGLISH,
    val learningLang: Language = Language.GERMAN,
)

data class SettingsState(
    val isLoading: Boolean = true,
    val langSettings: LangSettings? = null,
)

@HiltViewModel
class SettingsScreenViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository,
        private val localeManager: LocaleManager,
    ) : ViewModel(),
        ContainerHost<SettingsState, Unit> {
        override val container: Container<SettingsState, Unit> = container(SettingsState())

        init {
            fetchCurrentLangPreferences()
        }

        private fun fetchCurrentLangPreferences() =
            intent {
                val learnLang = settingsRepository.getLearningLang() ?: Language.GERMAN.code
                val interfaceLang = localeManager.getCurrentLocale().language
                Log.i("SettingsScreenViewModel", "Current interface language: $interfaceLang")
                reduce {
                    state.copy(
                        isLoading = false,
                        langSettings =
                            LangSettings(
                                learningLang = getLanguageByCode(learnLang),
                                interfaceLang = getLanguageByCode(interfaceLang),
                            ),
                    )
                }
            }

//        private fun fetchFF() {
//            featureService.isFeatureEnabled(Feature.MoreLearningLanguages).let {
//                _isNewLearningLanguagesEnabled.value = it
//            }
//        }

        fun setLearningLanguage(language: Language) =
            intent {
                settingsRepository.setLearningLang(language.code)
                reduce {
                    state.copy(
                        langSettings = state.langSettings?.copy(learningLang = language),
                    )
                }
            }

        fun signOut() {
            viewModelScope.launch {
                settingsRepository.clearLearningLang()
                settingsRepository.clearSalutation()
                authRepository.signOut()
            }
        }

        fun setLocale(languageCode: String) =
            intent {
                localeManager.setLocale(languageCode)
                reduce {
                    state.copy(
                        langSettings = state.langSettings?.copy(interfaceLang = getLanguageByCode(languageCode)),
                    )
                }
            }
    }
