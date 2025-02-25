package eu.rozmova.app.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.TodayScenarioSelectionModel
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.UserPreferencesRepository
import eu.rozmova.app.utils.LocaleManager
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_LEARNING_LANGUAGE = "de"

sealed class TodaySelectionState {
    data object Loading : TodaySelectionState()

    data class Success(
        val todaySelection: TodayScenarioSelectionModel,
    ) : TodaySelectionState()

    data object Error : TodaySelectionState()
}

@HiltViewModel
class LearnScreenViewModel
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val localeManager: LocaleManager,
    ) : ViewModel() {
        private val _scenariosState =
            MutableStateFlow<ViewState<List<ScenarioModel>>>(ViewState.Loading)
        val scenariosState = _scenariosState.asStateFlow()

        private val _todaySelectionState =
            MutableStateFlow<ViewState<TodayScenarioSelectionModel>>(ViewState.Loading)
        val todaySelectionState = _todaySelectionState.asStateFlow()

        init {
            fetchTodayScenarios()
        }

        private fun fetchTodayScenarios() =
            viewModelScope.launch {
                userPreferencesRepository
                    .fetchUserPreferences()
                    .map { it.learningLanguage }
                    .getOrElse { DEFAULT_LEARNING_LANGUAGE }
                    .let { learnLang ->
                        scenariosRepository.getTodaySelection(
                            learnLang,
                            localeManager.getCurrentLocale().language,
                        )
                    }.map { todaySelection ->
                        _todaySelectionState.value = ViewState.Success(todaySelection)
                    }.mapLeft { _todaySelectionState.value = ViewState.Error(it) }
            }
    }
