package eu.rozmova.app.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.TodayScenarioSelectionModel
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.UserPreferencesRepository
import eu.rozmova.app.utils.LocaleManager
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_LEARNING_LANGUAGE = "de"

@HiltViewModel
class LearnScreenViewModel
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val chatsRepository: ChatsRepository,
        private val localeManager: LocaleManager,
    ) : ViewModel() {
        private val _scenariosState =
            MutableStateFlow<ViewState<List<ScenarioModel>>>(ViewState.Loading)
        val scenariosState = _scenariosState.asStateFlow()

        private val _chatCreationState = MutableStateFlow<ViewState<String>>(ViewState.Empty)
        val chatCreationState = _chatCreationState.asStateFlow()

        private val _todaySelectionState =
            MutableStateFlow<ViewState<TodayScenarioSelectionModel>>(ViewState.Loading)
        val todaySelectionState = _todaySelectionState.asStateFlow()

        init {
            fetchTodayScenarios()
        }

        fun resetChatCreationState() {
            _chatCreationState.value = ViewState.Empty
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

        fun createChatFromScenario(scenario: ScenarioModel) {
            viewModelScope.launch {
                chatsRepository.createChatFromScenario(scenario).let { chatId ->
                    _chatCreationState.value = ViewState.Success(chatId)
                }
            }
        }
    }
