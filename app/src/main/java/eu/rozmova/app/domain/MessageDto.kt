package eu.rozmova.app.domain

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
