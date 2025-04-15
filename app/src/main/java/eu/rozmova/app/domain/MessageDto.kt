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

@Serializable
data class MessageDto(
    val id: String,
    val content: String,
    val author: Author,
    @SerialName("audio_id")
    val audioId: String?,
    @SerialName("audio_duration")
    val audioDuration: Int?,
    @SerialName("created_at")
    val createdAt: Instant,
)
