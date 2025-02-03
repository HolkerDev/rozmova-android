package eu.rozmova.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class ChatAnalysis(
    val taskCompletion: TaskCompletion,
    val topicsToReview: List<TopicToReview>,
)

@Serializable
data class TaskCompletion(
    val isCompleted: Boolean,
    val rating: Int,
    val requirements: Requirements,
)

@Serializable
data class Requirements(
    val met: List<String>,
    val missed: List<String>,
)

@Serializable
data class TopicToReview(
    val topic: String,
)
