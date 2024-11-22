package eu.rozmova.app.clients.domain

enum class ChatState {
    CREATED, COMPLETED
}

data class ChatDto(val id: String, val title: String, val description: String, val state: ChatState)