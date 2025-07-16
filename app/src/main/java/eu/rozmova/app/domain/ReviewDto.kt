package eu.rozmova.app.domain

data class ReviewDto(
    val id: String,
    val taskCompletion: TaskCompletionDto,
    val topicsToReview: List<String>,
    val wordsToLearn: List<String>,
    val chatType: ChatType,
    val chatTitle: String,
    val difficulty: DifficultyDto,
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
