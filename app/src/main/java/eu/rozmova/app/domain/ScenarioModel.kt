package eu.rozmova.app.domain

import eu.rozmova.app.components.Difficulty
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ScenarioDifficulty {
    EASY,
    MEDIUM,
    HARD,
}

fun ScenarioDifficulty.toDifficulty(): Difficulty =
    when (this) {
        ScenarioDifficulty.EASY -> Difficulty.BEGINNER
        ScenarioDifficulty.MEDIUM -> Difficulty.INTERMEDIATE
        ScenarioDifficulty.HARD -> Difficulty.ADVANCED
    }

enum class ScenarioType {
    CONVERSATION,
    MESSAGES,
    EMAIL,
}

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
    @SerialName("scenario_type")
    val scenarioType: ScenarioType,
    val difficulty: ScenarioDifficulty,
)

@Serializable
data class TodayScenarioSelectionModel(
    @SerialName("user_language")
    val userLanguage: String,
    @SerialName("scenario_language")
    val scenarioLanguage: String,
    @SerialName("easy_scenario")
    val easyScenario: ScenarioModel,
    @SerialName("medium_scenario")
    val mediumScenario: ScenarioModel,
    @SerialName("hard_scenario")
    val hardScenario: ScenarioModel,
)
