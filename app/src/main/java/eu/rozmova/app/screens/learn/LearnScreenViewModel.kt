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
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.TodayScenarioSelection
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
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

private val DEFAULT_LEARNING_LANGUAGE = Language.GERMAN.code

sealed class LearnEvent {
    data class ChatCreated(
        val chatId: String,
        val scenarioType: ScenarioTypeDto,
    ) : LearnEvent()
}

data class LearnScreenState(
    val weeklyScenarios: List<ScenarioDto>? = null,
    val weeklyScenariosLoading: Boolean = false,
    val recommendedScenarios: TodayScenarioSelection? = null,
)

@HiltViewModel
class LearnScreenViewModel
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
        private val chatsRepository: ChatsRepository,
        private val localeManager: LocaleManager,
    ) : ViewModel(),
        ContainerHost<LearnScreenState, LearnEvent> {
        override val container: Container<LearnScreenState, LearnEvent> = container(LearnScreenState())

        private val _latestChat = MutableStateFlow<ViewState<ChatWithScenarioModel>>(ViewState.Loading)
        val latestChat = _latestChat.asStateFlow()

        private val _events = MutableSharedFlow<LearnEvent>()
        val events = _events.asSharedFlow()

        init {
            fetchTodayScenarios()
            fetchLatestChat()
            fetchWeeklyScenarios()
        }

        private fun fetchTodayScenarios() =
            intent {
                userPreferencesRepository
                    .fetchUserPreferences()
                    .map { it.learningLanguage }
                    .getOrElse { DEFAULT_LEARNING_LANGUAGE }
                    .let { learnLang ->
                        scenariosRepository.getTodaySelection()
                    }.map { todaySelection ->
                        reduce { state.copy(recommendedScenarios = todaySelection) }
                    }.mapLeft { Log.e("LearnScreenViewModel", "Error fetching today scenarios", it) }
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

        fun createChatFromScenario(scenarioId: String) {
            viewModelScope.launch {
                chatsRepository.createChatFromScenario(scenarioId).map { createdChat ->
                    _events.emit(LearnEvent.ChatCreated(createdChat.id, createdChat.scenario.scenarioType))
                }
            }
        }

        private fun fetchWeeklyScenarios() =
            intent {
                reduce { state.copy(weeklyScenariosLoading = true) }
                val userLang = localeManager.getCurrentLocale().language
                val learnLang =
                    userPreferencesRepository
                        .fetchUserPreferences()
                        .map { it.learningLanguage }
                        .getOrElse { DEFAULT_LEARNING_LANGUAGE }

                scenariosRepository.weeklyScenarios(userLang = userLang, scenarioLang = learnLang).map { scenarios ->
                    reduce { state.copy(weeklyScenarios = scenarios, weeklyScenariosLoading = false) }
                }
            }
    }
