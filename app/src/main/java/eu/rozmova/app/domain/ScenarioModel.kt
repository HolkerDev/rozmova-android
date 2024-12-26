package eu.rozmova.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class ScenarioModel(val id: String, val title: String, val labels: List<String>)