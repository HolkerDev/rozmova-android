package eu.rozmova.app.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Language
import eu.rozmova.app.domain.getLanguageByCode
import eu.rozmova.app.repositories.AuthRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.repositories.UserRepository
import eu.rozmova.app.repositories.billing.SubscriptionRepository
import eu.rozmova.app.state.AppStateRepository
import eu.rozmova.app.utils.LocaleManager
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
    val isSubscribed: Boolean = false,
)

@HiltViewModel
class SettingsScreenViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository,
        private val localeManager: LocaleManager,
        private val subscriptionRepository: SubscriptionRepository,
        private val userRepository: UserRepository,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<SettingsState, Unit> {
        override val container: Container<SettingsState, Unit> = container(SettingsState())

        init {
            fetchCurrentLangPreferences()
            fetchSubscription()
        }

        private fun fetchSubscription() =
            intent {
                val isSubscribed = subscriptionRepository.getIsSubscribed()
                reduce { state.copy(isSubscribed = isSubscribed) }
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

        fun setLearningLanguage(language: Language) =
            intent {
                Log.i("SettingsScreenViewModel", "Setting learning language to: ${language.code}")
                settingsRepository.setLearningLang(language.code)
                reduce {
                    state.copy(
                        langSettings = state.langSettings?.copy(learningLang = language),
                    )
                }
                appStateRepository.triggerRefetch()
            }

        fun signOut() =
            intent {
                settingsRepository.clearLearningLang()
                settingsRepository.clearSalutation()
                authRepository.signOut()
            }

        fun setLocale(languageCode: String) =
            intent {
                localeManager.setLocale(languageCode)
                reduce {
                    state.copy(
                        langSettings = state.langSettings?.copy(interfaceLang = getLanguageByCode(languageCode)),
                    )
                }
                appStateRepository.triggerRefetch()
            }

        fun deleteUserData() =
            intent {
                try {
                    // Clear all user settings
                    settingsRepository.clearLearningLang()
                    settingsRepository.clearSalutation()
                    userRepository.deleteUser()
                    // Sign out the user which should clear authentication data
                    authRepository.signOut()

                    Log.i("SettingsScreenViewModel", "User data deleted successfully")
                } catch (e: Exception) {
                    Log.e("SettingsScreenViewModel", "Error deleting user data", e)
                }
            }
    }
