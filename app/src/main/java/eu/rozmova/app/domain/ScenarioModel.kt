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
}

@Serializable
data class WordModel(
    val id: String,
    val word: String,
    val translation: String,
)

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
    @SerialName("word_ids")
    val wordIds: List<String>?,
)

data class TodayScenarioSelection(
    val easyScenario: ScenarioDto,
    val mediumScenario: ScenarioDto,
    val hardScenario: ScenarioDto,
)
