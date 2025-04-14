package eu.rozmova.app.domain

import eu.rozmova.app.components.Difficulty
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioDto(
    val id: String,
    val createdAt: String,
    val userLang: LangDto,
    val scenarioLang: LangDto,
    val difficulty: DifficultyDto,
    val scenarioType: ScenarioTypeDto,
    val title: String,
    val situation: String,
)

enum class DifficultyDto {
    EASY,
    MEDIUM,
    HARD,
}

fun DifficultyDto.toDifficulty(): Difficulty =
    when (this) {
        DifficultyDto.EASY -> Difficulty.BEGINNER
        DifficultyDto.MEDIUM -> Difficulty.INTERMEDIATE
        DifficultyDto.HARD -> Difficulty.ADVANCED
    }

enum class ScenarioTypeDto {
    CONVERSATION,
    MESSAGES,
}

enum class LangDto {
    EN,
    DE,
}
