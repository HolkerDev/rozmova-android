package eu.rozmova.app.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ChatStatus {
    IN_PROGRESS,
    FINISHED,
    ARCHIVED,
}

@Serializable
data class ChatModel(
    val id: String,
    @SerialName("scenario_id")
    val scenarioId: String,
    val status: ChatStatus,
    @SerialName("user_id")
    val userId: String,
)

@Serializable
data class BaseChatModel(
    val id: String,
    val status: ChatStatus,
    val title: String,
    val labels: List<String>,
    val languageLevel: String,
    val botInstruction: String,
    val situation: String,
    val userInstruction: String,
    val targetLanguage: String,
    val userLanguage: String,
)
