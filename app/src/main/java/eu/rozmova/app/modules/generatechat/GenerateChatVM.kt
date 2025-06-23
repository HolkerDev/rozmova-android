package eu.rozmova.app.modules.generatechat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.SettingsRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class GenerateChatState(
    val isLoading: Boolean = false,
)

sealed interface GenerateChatEvents {
    data class ChatCreated(
        val chatId: String,
        val scenarioType: ScenarioTypeDto,
    ) : GenerateChatEvents
}

@HiltViewModel
class GenerateChatVM
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val scenariosRepository: ScenariosRepository,
    ) : ViewModel(),
        ContainerHost<GenerateChatState, GenerateChatEvents> {
        private val tag = this::class.simpleName
        override val container: Container<GenerateChatState, GenerateChatEvents> = container(GenerateChatState())

        fun generateScenario(
            difficultyDto: DifficultyDto,
            scenarioType: ScenarioTypeDto,
            description: String,
        ) = intent {
            reduce { state.copy(isLoading = true) }
            val userLang = settingsRepository.getInterfaceLang()
            val scenarioLang = settingsRepository.getLearningLangOrDefault()
            scenariosRepository
                .generateScenario(userLang, scenarioLang, scenarioType, difficultyDto, description)
                .map { response ->
                    postSideEffect(
                        GenerateChatEvents.ChatCreated(
                            response.chatId,
                            response.scenarioType,
                        ),
                    )
                    reduce { state.copy(isLoading = false) }
                }
        }
    }
