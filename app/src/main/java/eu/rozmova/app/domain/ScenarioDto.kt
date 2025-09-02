package eu.rozmova.app.domain

import eu.rozmova.app.components.Difficulty

data class ScenarioDto(
    val id: String,
    val createdAt: String,
    val userLang: LangDto,
    val scenarioLang: LangDto,
    val difficulty: DifficultyDto,
    val scenarioType: ScenarioTypeDto,
    val title: String,
    val situation: String,
    val labels: List<String>,
    val helperWords: List<HelperWord>,
    val userInstructions: List<UserInstruction>,
)

data class UserInstruction(
    val assessment: String,
    val task: String,
)

data class HelperWord(
    val word: String,
    val translation: String,
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

fun ScenarioTypeDto.toScenarioType(): ScenarioType =
    when (this) {
        ScenarioTypeDto.CONVERSATION -> ScenarioType.CONVERSATION
        ScenarioTypeDto.MESSAGES -> ScenarioType.MESSAGES
    }

enum class LangDto {
    EN,
    DE,
}
