package eu.rozmova.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioDto(
    val id: String,
    val createdAt: String,
    val userLang: LangDto,
    val scenarioLang: LangDto,
    val scenarioType: ScenarioTypeDto,
    val title: String,
    val situation: String,
)

enum class ScenarioTypeDto {
    EASY,
    MEDIUM,
    HARD,
}

enum class LangDto {
    EN,
    DE,
}
