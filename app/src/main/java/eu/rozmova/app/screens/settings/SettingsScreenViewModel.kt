package eu.rozmova.app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Language
import eu.rozmova.app.domain.UserPreference
import eu.rozmova.app.domain.getLanguageByCode
import eu.rozmova.app.domain.getLanguageByDatabaseName
import eu.rozmova.app.domain.toDatabaseName
import eu.rozmova.app.repositories.AuthRepository
import eu.rozmova.app.repositories.UserPreferencesRepository
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
        private val localeManager: LocaleManager,
    ) : ViewModel() {
        private val _state = MutableStateFlow<SettingsViewState>(SettingsViewState.Loading)
        val state = _state.asStateFlow()

        init {
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
                                learningLang = getLanguageByDatabaseName(userPrefs.learningLanguage),
                                isGreekEnabled = userPrefs.hasGreekEnabled ?: false,
                            )
                        }
            }

        fun setLearningLanguage(
            language: Language,
            isGreekEnabled: Boolean,
        ) = viewModelScope.launch {
            userPreferencesRepository.updateUserPreferences(
                UserPreference(learningLanguage = language.toDatabaseName(), hasGreekEnabled = isGreekEnabled),
            )
            fetchCurrentLangPreferences()
        }

        fun signOut() {
            viewModelScope.launch {
                _state.value = SettingsViewState.Loading
                authRepository.signOut()
            }
        }

        fun setLocale(languageCode: String) {
            localeManager.setLocale(languageCode)
        }
    }
