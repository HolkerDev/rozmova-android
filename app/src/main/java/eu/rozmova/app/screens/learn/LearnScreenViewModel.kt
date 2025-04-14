package eu.rozmova.app.screens.learn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatWithScenarioModel
import eu.rozmova.app.domain.Language
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.TodayScenarioSelectionModel
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.UserPreferencesRepository
import eu.rozmova.app.utils.LocaleManager
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val DEFAULT_LEARNING_LANGUAGE = Language.GERMAN.code

sealed class LearnEvent {
    data class ChatCreated(
        val chatId: String,
        val scenarioType: ScenarioType,
    ) : LearnEvent()
}

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

        private val _weeklyScenarios = MutableStateFlow<ViewState<List<ScenarioDto>>>(ViewState.Loading)
        val weeklyScenarios = _weeklyScenarios.asStateFlow()

        private val _events = MutableSharedFlow<LearnEvent>()
        val events = _events.asSharedFlow()

        private val _chatCreationState = MutableStateFlow<ViewState<String>>(ViewState.Empty)
        val chatCreationState = _chatCreationState.asStateFlow()

        private val _todaySelectionState =
            MutableStateFlow<ViewState<TodayScenarioSelectionModel>>(ViewState.Loading)
        val todaySelectionState = _todaySelectionState.asStateFlow()

        private val _latestChat = MutableStateFlow<ViewState<ChatWithScenarioModel>>(ViewState.Loading)
        val latestChat = _latestChat.asStateFlow()

        init {
            fetchTodayScenarios()
            fetchLatestChat()
            fetchScenarios()
            fetchWeeklyScenarios()
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

        private fun fetchLatestChat() =
            viewModelScope.launch {
                chatsRepository
                    .fetchChats()
                    .map { chats ->
                        chats
                            .filter { it.status == ChatStatus.IN_PROGRESS }
                            .takeIf { it.isNotEmpty() }
                            ?.last()
                            ?.let {
                                _latestChat.value = ViewState.Success(it)
                            } ?: run { _latestChat.value = ViewState.Empty }
                    }.mapLeft { _latestChat.value = ViewState.Error(it) }
            }

        fun createChatFromScenario(scenario: ScenarioModel) {
            viewModelScope.launch {
                chatsRepository.createChatFromScenario(scenario).let { chatId ->
                    _chatCreationState.value = ViewState.Success(chatId)
                    _events.emit(LearnEvent.ChatCreated(chatId, scenario.scenarioType))
                }
            }
        }

        private fun fetchWeeklyScenarios() =
            viewModelScope.launch {
                val userLang = localeManager.getCurrentLocale().language
                userPreferencesRepository
                    .fetchUserPreferences()
                    .map { it.learningLanguage }
                    .getOrElse { DEFAULT_LEARNING_LANGUAGE }
                    .let { learnLang ->
                        scenariosRepository.weeklyScenarios(userLang = userLang, scenarioLang = learnLang)
                    }.map {
                        _weeklyScenarios.value = ViewState.Success(it)
                    }.mapLeft {
                        Log.e("LearnScreenViewModel", "Error trying to fetch weekly scenarios", it)
                        _weeklyScenarios.value = ViewState.Error(it)
                    }
            }

        private fun fetchScenarios() =
            viewModelScope.launch {
                userPreferencesRepository
                    .fetchUserPreferences()
                    .map { it.learningLanguage }
                    .getOrElse { DEFAULT_LEARNING_LANGUAGE }
                    .let { learnLang ->
                        scenariosRepository.getAll(
                            learnLang,
                            localeManager.getCurrentLocale().language,
                        )
                    }.takeIf { it.isNotEmpty() }
                    ?.let { todaySelection ->
                        _scenariosState.value = ViewState.Success(todaySelection)
                    } ?: run { _scenariosState.value = ViewState.Empty }
            }
    }
