package eu.rozmova.app.domain

data class ChatDto(
    val id: String,
    val scenario: ScenarioDto,
    val status: ChatStatus,
    val messages: List<MessageDto>,
)
