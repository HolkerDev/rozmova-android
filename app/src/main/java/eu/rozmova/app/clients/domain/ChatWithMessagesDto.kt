package eu.rozmova.app.clients.domain

import com.google.gson.annotations.SerializedName

enum class Owner {
    @SerializedName("user")
    USER,

    @SerializedName("assistant")
    ASSISTANT
}

data class ChatWithMessagesDto(
    val id: String,
    val title: String,
    val messages: List<MessageDto>,
    val description: String,
    @SerializedName(value = "user_instructions") val userInstructions: String
)

data class MessageDto(
    val id: String, val body: String, val link: String, val owner: Owner, val order: Int
)