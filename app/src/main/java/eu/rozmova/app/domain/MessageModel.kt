package eu.rozmova.app.domain

import com.google.gson.annotations.SerializedName
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
    val chatModel: ChatModel,
    val messages: List<MessageModel>,
    val words: List<WordModel>,
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
    @SerialName("audio_duration")
    val audioDuration: Int,
)
