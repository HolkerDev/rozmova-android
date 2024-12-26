package eu.rozmova.app.screens.createchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.repositories.ScenariosRepository
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
}

@HiltViewModel
class CreateChatViewModel
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow<CreateChatState>(CreateChatState.Loading)
        val state = _state.asStateFlow()

        private fun fetchLevelGroups() =
            viewModelScope.launch {
                _state.value = CreateChatState.Loading
                val scenarios = scenariosRepository.getAll()
                val levelGroups = scenariosToLevelGroups(scenarios)
                _state.value = CreateChatState.Success(levelGroups)
            }

        init {
            fetchLevelGroups()
        }
    }
