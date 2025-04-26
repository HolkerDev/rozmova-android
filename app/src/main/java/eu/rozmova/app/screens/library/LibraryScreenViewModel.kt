package eu.rozmova.app.screens.library

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
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
class LibraryScreenViewModel
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val chatsRepository: ChatsRepository,
    ) : ViewModel(),
        ContainerHost<LibraryScreenState, LibraryScreenEvents> {
        override val container: Container<LibraryScreenState, LibraryScreenEvents> = container(LibraryScreenState())

        fun fetchScenarios(
            scenarioType: ScenarioTypeDto,
            difficulty: DifficultyDto,
        ) = intent {
            scenariosRepository.getAllWithFilter(scenarioType, difficulty).map { scenarios ->
                reduce { state.copy(scenarios = scenarios) }
            } // TODO: Map error to state
        }

        fun createChat(scenarioId: String) =
            intent {
                chatsRepository.createChatFromScenario(scenarioId).map { chatDto ->
                    postSideEffect(LibraryScreenEvents.ChatCreated(chatDto.id, chatDto.scenario.scenarioType))
                }
            }
    }
