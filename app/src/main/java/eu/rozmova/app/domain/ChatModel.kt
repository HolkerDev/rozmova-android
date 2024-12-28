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
data class ChatWithScenarioModel(
    val id: String,
    @SerialName("scenario_id")
    val scenarioId: String,
    val status: ChatStatus,
    @SerialName("user_id")
    val userId: String,
    val scenario: ScenarioModel,
)
