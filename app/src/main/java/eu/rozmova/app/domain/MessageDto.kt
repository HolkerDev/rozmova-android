package eu.rozmova.app.domain

import eu.rozmova.app.components.conversationchat.AudioChatMessage
import kotlinx.serialization.Serializable

enum class Author {
    USER,
    BOT,
}

@Serializable
data class MessageDto(
    val id: String,
    val content: String,
    val author: Author,
    val audioId: String?,
    val audioDuration: Int?,
)

fun MessageDto.toAudioMessage(): AudioChatMessage =
    AudioChatMessage(
        id = this.id,
        isPlaying = false,
        duration = this.audioDuration ?: 0,
        body = this.content,
        link = "",
        author = this.author,
    )
