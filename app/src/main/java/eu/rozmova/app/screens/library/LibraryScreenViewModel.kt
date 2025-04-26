package eu.rozmova.app.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.utils.LocaleManager
import eu.rozmova.app.utils.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class LibraryScreenState(
    val scenarios: List<ScenarioDto>? = null,
)

@HiltViewModel
class LibraryScreenViewModel @Inject constructor(
    private val scenariosRepository: ScenariosRepository,
    private val localeManager: LocaleManager,
) : ContainerHost<LibraryScreenState, Nothing>, ViewModel() {
    override val container: Container<LibraryScreenState, Nothing> = container(LibraryScreenState())

    init {
        fetchScenarios()
    }

    private fun fetchScenarios() = intent {
        scenariosRepository.getAllWithFilter().map { scenarios ->
            reduce { state.copy(scenarios = scenarios) }
        }
    }
}