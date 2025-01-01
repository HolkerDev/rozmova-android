package eu.rozmova.app.clients.domain

import com.google.gson.annotations.SerializedName
import eu.rozmova.app.domain.ScenarioModel
import kotlinx.serialization.Serializable

enum class Owner {
    @SerializedName("user")
    USER,

    @SerializedName("assistant")
    ASSISTANT,
}

@Serializable
data class ChatWithMessagesDto(
    val id: String,
    val scenario: ScenarioModel,
    val messages: List<MessageDto> = emptyList(),
)

@Serializable
data class MessageDto(
    val id: String,
    val body: String,
    val link: String,
    val owner: Owner,
    val order: Int,
)
