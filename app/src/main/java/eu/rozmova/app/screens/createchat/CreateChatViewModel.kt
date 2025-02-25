package eu.rozmova.app.screens.createchat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.UserPreference
import eu.rozmova.app.domain.getLanguageByCode
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.UserPreferencesRepository
import eu.rozmova.app.utils.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LevelGroup(
    val level: String,
    val groupName: String,
    val scenarios: List<ScenarioModel>,
)

fun mapLevelToGroupName(level: String): String =
    when (level) {
        "A1" -> "Beginner"
        "A2" -> "Elementary"
        "B1" -> "Intermediate"
        "B2" -> "Upper Intermediate"
        "C1" -> "Advanced"
        "C2" -> "Proficiency"
        else -> "Unknown"
    }

fun scenariosToLevelGroups(scenarios: List<ScenarioModel>): List<LevelGroup> =
    scenarios
        .groupBy { scenario -> scenario.languageLevel }
        .map { entry ->
            LevelGroup(
                level = entry.key,
                groupName = "${mapLevelToGroupName(entry.key)} (${entry.key})",
                scenarios = entry.value,
            )
        }

sealed class CreateChatState {
    data object Loading : CreateChatState()

    data class Success(
        val levelGroups: List<LevelGroup>,
    ) : CreateChatState()

    data class ChatCreated(
        val chatId: String,
    ) : CreateChatState()
}

@HiltViewModel
class CreateChatViewModel
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val localeManager: LocaleManager,
        private val chatsRepository: ChatsRepository,
        private val userPreferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val tag = this::class.simpleName

        private val _state = MutableStateFlow<CreateChatState>(CreateChatState.Loading)
        val state = _state.asStateFlow()

        private fun fetchLevelGroups() =
            viewModelScope.launch {
                _state.value = CreateChatState.Loading
                val userLearnLanguage =
                    userPreferencesRepository
                        .fetchUserPreferences()
                        .getOrElse { UserPreference.DEFAULT }
                        .learningLanguage
                val interfaceLanguage = getLanguageByCode(localeManager.getCurrentLocale().language)
                Log.i(
                    "CreateChatViewModel",
                    "User learning language: $userLearnLanguage, interface language: $interfaceLanguage",
                )
                val scenarios =
                    scenariosRepository.getAll(
                        userLearnLanguage,
                        interfaceLanguage.code,
                    )
                val levelGroups = scenariosToLevelGroups(scenarios)
                if (levelGroups.isEmpty()) {
                    Log.e(tag, "No scenarios found")
                    _state.value = CreateChatState.Loading
                    return@launch
                }
                _state.value = CreateChatState.Success(levelGroups)
            }

        fun createChatFromScenario(scenario: ScenarioModel) {
            viewModelScope.launch {
                _state.value = CreateChatState.Loading
                val chatId = chatsRepository.createChatFromScenario(scenario)
                _state.value = CreateChatState.ChatCreated(chatId)
            }
        }

        init {
            fetchLevelGroups()
        }
    }
