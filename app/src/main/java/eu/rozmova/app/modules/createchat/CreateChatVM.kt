package eu.rozmova.app.modules.createchat

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.state.AppStateRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class CreateChatState(
    val scenario: ScenarioDto? = null,
)

sealed interface CreateChatEvent {
    data class ChatCreated(
        val chatId: String,
        val chatType: ChatType,
    ) : CreateChatEvent
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
                        Log.e("CreateChatVM", "Error fetching scenario", error)
                    }
            }

        fun createChat(
            scenarioId: String,
            chatType: ChatType,
        ) = intent {
            chatsRepository
                .createChat(scenarioId, chatType)
                .map { chatId ->
                    Log.d("CreateChatVM", "Chat created with ID: $chatId")
                    appStateRepository.triggerFetchChats()
                    postSideEffect(CreateChatEvent.ChatCreated(chatId, chatType))
                }.mapLeft { error ->
                    Log.e("CreateChatVM", "Error creating chat", error)
                }
        }
    }
