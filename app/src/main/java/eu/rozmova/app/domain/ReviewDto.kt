package eu.rozmova.app.domain

data class ReviewDto(
    val id: String,
    val chat: ChatDto,
    val taskCompletion: TaskCompletionDto,
    val topicsToReview: List<String>,
    val wordsToLearn: List<String>,
)

data class TaskCompletionDto(
    val isCompleted: Boolean,
    val metInstructions: List<String>,
    val missedInstructions: List<String>,
    val mistakes: List<MistakeDto>,
    val rating: Int,
)

data class MistakeDto(
    val wrong: String,
    val correct: String,
)
