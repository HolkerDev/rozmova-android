package eu.rozmova.app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Language
import eu.rozmova.app.domain.UserPreference
import eu.rozmova.app.domain.getLanguageByCode
import eu.rozmova.app.repositories.AuthRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.repositories.UserPreferencesRepository
import eu.rozmova.app.services.Feature
import eu.rozmova.app.services.FeatureService
import eu.rozmova.app.utils.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsViewState {
    data object Loading : SettingsViewState()

    data class Success(
        val interfaceLang: Language,
        val learningLang: Language,
        val isGreekEnabled: Boolean = false,
    ) : SettingsViewState()

    data class Error(
        val msg: String,
    ) : SettingsViewState()
}

@HiltViewModel
class SettingsScreenViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val featureService: FeatureService,
        private val settingsRepository: SettingsRepository,
        private val localeManager: LocaleManager,
    ) : ViewModel() {
        private val _state = MutableStateFlow<SettingsViewState>(SettingsViewState.Loading)
        val state = _state.asStateFlow()

        private val _isNewLearningLanguagesEnabled = MutableStateFlow(false)
        val isNewLearningLanguagesEnabled = _isNewLearningLanguagesEnabled.asStateFlow()

        init {
            fetchFF()
            fetchCurrentLangPreferences()
        }

        private fun fetchCurrentLangPreferences() =
            viewModelScope.launch {
                _state.value =
                    userPreferencesRepository
                        .fetchUserPreferences()
                        .getOrElse { UserPreference.DEFAULT }
                        .let { userPrefs ->
                            SettingsViewState.Success(
                                interfaceLang = getLanguageByCode(localeManager.getCurrentLocale().language),
                                learningLang = getLanguageByCode(userPrefs.learningLanguage),
                                isGreekEnabled = userPrefs.hasGreekEnabled ?: false,
                            )
                        }
            }

        private fun fetchFF() {
            featureService.isFeatureEnabled(Feature.MoreLearningLanguages).let {
                _isNewLearningLanguagesEnabled.value = it
            }
        }

        fun setLearningLanguage(
            language: Language,
            isGreekEnabled: Boolean,
        ) = viewModelScope.launch {
            userPreferencesRepository
                .updateUserPreferences(
                    UserPreference(learningLanguage = language.code, hasGreekEnabled = isGreekEnabled),
                ).also {
                    fetchCurrentLangPreferences()
                }
        }

        fun signOut() {
            viewModelScope.launch {
                settingsRepository.clearLearningLang()
                _state.value = SettingsViewState.Loading
                authRepository.signOut()
            }
        }

        fun setLocale(languageCode: String) {
            localeManager.setLocale(languageCode)
        }
    }
