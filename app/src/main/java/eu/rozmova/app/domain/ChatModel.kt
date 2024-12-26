package eu.rozmova.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class ChatModel(
    val id: String, val title: String, val members: List<String>, val isCreated: Boolean
)
