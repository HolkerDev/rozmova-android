package eu.rozmova.app.clients.domain

import com.google.gson.annotations.SerializedName
import eu.rozmova.app.domain.ScenarioModel
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Author {
    @SerializedName("USER")
    USER,

    @SerializedName("BOT")
    BOT,
}

data class ChatWithMessagesDto(
    val id: String,
    val scenario: ScenarioModel,
    val messages: List<MessageModel>,
)

@Serializable
data class MessageModel(
    val id: String,
    val transcription: String,
    val author: Author,
    @SerialName("audio_reference")
    val audioReference: String,
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("created_at")
    val createdAt: Instant,
)
