package eu.rozmova.app.modules.createchat

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.UsageLimitReachedException
import eu.rozmova.app.state.AppStateRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class CreateChatState(
    val scenario: ScenarioDto? = null,
    val isLoading: Boolean = false,
    val errorChatCreation: Boolean = false,
    val errorScenarioLoad: Boolean = false,
)

sealed interface CreateChatEvent {
    data class ChatCreated(
        val chatId: String,
        val chatType: ChatType,
    ) : CreateChatEvent

    data object UsageLimitReached : CreateChatEvent
}

@HiltViewModel
class CreateChatVM
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val chatsRepository: ChatsRepository,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<CreateChatState, CreateChatEvent> {
        override val container: Container<CreateChatState, CreateChatEvent> =
            container(CreateChatState())

        fun fetchScenario(scenarioId: String) =
            intent {
                scenariosRepository
                    .getScenarioById(scenarioId)
                    .map { scenario ->
                        reduce { state.copy(scenario) }
                    }.mapLeft { error ->
                        reduce { state.copy(errorScenarioLoad = true) }
                        Log.e("CreateChatVM", "Error fetching scenario", error)
                    }
            }

        fun createChat(
            scenarioId: String,
            chatType: ChatType,
        ) = intent {
            reduce { state.copy(errorChatCreation = false, isLoading = true) }
            chatsRepository
                .createChat(scenarioId, chatType)
                .onSuccess { chatId ->
                    Log.d("CreateChatVM", "Chat created with ID: $chatId")
                    appStateRepository.triggerRefetch()
                    postSideEffect(CreateChatEvent.ChatCreated(chatId, chatType))
                }.onFailure { error ->
                    if (error is UsageLimitReachedException) {
                        Log.w("CreateChatVM", "Usage limit reached", error)
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(CreateChatEvent.UsageLimitReached)
                        return@onFailure
                    }
                    Log.e("CreateChatVM", "Error creating chat", error)
                    reduce { state.copy(errorChatCreation = true, isLoading = false) }
                }
        }
    }
