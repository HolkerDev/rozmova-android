package eu.rozmova.app.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioModel(
    val id: String,
    @SerialName("created_at")
    val createdAt: Instant,
    val title: String,
    val labels: List<String>,
    @SerialName("language_level")
    val languageLevel: String,
    @SerialName("bot_instruction")
    val botInstruction: String,
    val situation: String,
    @SerialName("user_instruction")
    val userInstruction: String,
    @SerialName("target_language")
    val targetLanguage: String,
    @SerialName("user_language")
    val userLanguage: String,
)
