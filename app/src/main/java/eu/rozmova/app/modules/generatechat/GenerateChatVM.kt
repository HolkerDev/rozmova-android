package eu.rozmova.app.modules.generatechat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.repositories.UsageLimitReachedException
import eu.rozmova.app.state.AppStateRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class GenerateChatState(
    val isLoading: Boolean = false,
    val error: Boolean = false,
)

sealed interface GenerateChatEvents {
    data class ChatCreated(
        val chatId: String,
        val chatType: ChatType,
    ) : GenerateChatEvents

    data object UsageLimitReached : GenerateChatEvents
}

@HiltViewModel
class GenerateChatVM
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val scenariosRepository: ScenariosRepository,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<GenerateChatState, GenerateChatEvents> {
        override val container: Container<GenerateChatState, GenerateChatEvents> =
            container(GenerateChatState())

        fun generateScenario(
            difficultyDto: DifficultyDto,
            chatType: ChatType,
            description: String,
        ) = intent {
            reduce { state.copy(isLoading = true, error = false) }
            val userLang = settingsRepository.getInterfaceLang()
            val scenarioLang = settingsRepository.getLearningLangOrDefault()
            scenariosRepository
                .generateScenario(userLang, scenarioLang, chatType, difficultyDto, description)
                .onSuccess { chatId ->
                    appStateRepository.triggerRefetch()
                    postSideEffect(
                        GenerateChatEvents.ChatCreated(
                            chatId,
                            chatType,
                        ),
                    )
                    reduce { state.copy(isLoading = false) }
                }.onFailure { error ->
                    if (error is UsageLimitReachedException) {
                        postSideEffect(GenerateChatEvents.UsageLimitReached)
                        reduce { state.copy(isLoading = false) }
                        return@onFailure
                    }
                    reduce { state.copy(isLoading = false, error = true) }
                }
        }
    }
