package eu.rozmova.app.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Language
import eu.rozmova.app.domain.getLanguageByCode
import eu.rozmova.app.repositories.AuthRepository
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
        private val localeManager: LocaleManager,
    ) : ViewModel() {
        private val _state = MutableStateFlow<SettingsViewState>(SettingsViewState.Loading)
        val state = _state.asStateFlow()

        init {
            fetchCurrentLangPreferences()
        }

        fun fetchCurrentLangPreferences() {
            val selectedLocale = localeManager.getCurrentLocale()
            getLanguageByCode(selectedLocale.language).let { interfaceLang ->
                _state.value =
                    SettingsViewState.Success(
                        interfaceLang = interfaceLang,
                        learningLang = interfaceLang,
                    )
            }
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
