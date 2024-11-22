package eu.rozmova.app.clients.domain

enum class Owner {
    USER, ASSISTANT
}

data class ChatWithMessagesDto(
    val id: String, val title: String, val messages: List<MessageDto>
)

data class MessageDto(
    val id: String, val body: String, val link: String, val owner: Owner, val order: Int
)