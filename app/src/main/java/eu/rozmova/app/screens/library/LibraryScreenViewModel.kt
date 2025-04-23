package eu.rozmova.app.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.utils.LocaleManager
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryScreenState(
    val scenarios: ViewState<List<ScenarioModel>> = ViewState.Loading,
    val selectedLevel: String? = null,
    val selectedType: ScenarioType? = null,
    val showFinishedOnly: Boolean = false,
)

@HiltViewModel
class LibraryScreenViewModel @Inject constructor(
    private val scenariosRepository: ScenariosRepository,
    private val localeManager: LocaleManager,
) : ViewModel() {
    private val _state = MutableStateFlow(LibraryScreenState())
    val state = _state.asStateFlow()

    init {
        fetchScenarios()
    }

    private fun fetchScenarios() {
        viewModelScope.launch {
            val userLearnLanguage = localeManager.getCurrentLocale().language
            val scenarios = scenariosRepository.getAll(
                learningLanguage = userLearnLanguage,
                interfaceLanguage = userLearnLanguage
            )
            _state.value = _state.value.copy(scenarios = ViewState.Success(scenarios))
        }
    }

    fun updateFilters(
        level: String? = null,
        type: ScenarioType? = null,
        showFinishedOnly: Boolean? = null
    ) {
        _state.value = _state.value.copy(
            selectedLevel = level ?: _state.value.selectedLevel,
            selectedType = type ?: _state.value.selectedType,
            showFinishedOnly = showFinishedOnly ?: _state.value.showFinishedOnly
        )
    }
} 