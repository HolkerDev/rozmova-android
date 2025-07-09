package eu.rozmova.app.modules.createchat

import android.util.Log
import androidx.lifecycle.ViewModel
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.repositories.ScenariosRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class CreateChatState(
    val scenario: ScenarioDto? = null,
)

class CreateChatVM
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
    ) : ViewModel(),
        ContainerHost<CreateChatState, Unit> {
        override val container: Container<CreateChatState, Unit> = container(CreateChatState())

        fun fetchScenario(scenarioId: String) =
            intent {
                scenariosRepository
                    .getScenarioById(scenarioId)
                    .map { scenario ->
                        reduce { state.copy(scenario) }
                    }.mapLeft { error ->
                        Log.e("CreateChatVM", "Error fetching scenario", error)
                    }
            }
    }
