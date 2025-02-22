package eu.rozmova.app.services

sealed class Feature(
    val key: String,
) {
    data object MoreLearningLanguages : Feature(key = "more_learning_languages")
}
