package eu.rozmova.app.modules.allscenarios

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.state.AppStateRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class LibraryScreenState(
    val scenarios: List<ScenarioDto>? = null,
)

sealed interface LibraryScreenEvents {
    data class ChatCreated(
        val chatId: String,
        val scenarioType: ScenarioTypeDto,
    ) : LibraryScreenEvents
}

@HiltViewModel
class AllScenariosVM
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val settingsRepository: SettingsRepository,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<LibraryScreenState, LibraryScreenEvents> {
        override val container: Container<LibraryScreenState, LibraryScreenEvents> = container(LibraryScreenState())

        init {
            fetchScenarios(null, null)

            intent {
                appStateRepository.refetch.collect {
                    fetchScenarios(null, null)
                }
            }
        }

        fun fetchScenarios(
            scenarioType: ScenarioTypeDto?,
            difficulty: DifficultyDto?,
        ) = intent {
            val scenarioLang = settingsRepository.getLearningLangOrDefault()
            val userLang = settingsRepository.getInterfaceLang()
            scenariosRepository.getAllWithFilter(userLang, scenarioLang, scenarioType, difficulty).map { scenarios ->
                reduce { state.copy(scenarios = scenarios) }
            } // TODO: Map error to state
        }
    }
