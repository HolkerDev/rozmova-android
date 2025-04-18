package eu.rozmova.app.domain

data class ReviewDto(
    val taskCompletion: TaskCompletionDto,
    val topicsToReview: List<String>,
    val wordsToLearn: List<String>,
)

data class TaskCompletionDto(
    val isCompleted: Boolean,
    val metInstructions: String,
    val missedInstructions: String,
    val mistakes: List<MistakeDto>,
    val rating: Int,
)

data class MistakeDto(
    val wrong: String,
    val correct: String,
)
