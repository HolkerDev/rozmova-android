package eu.rozmova.app.domain

data class BucketDto(
    val progress: Int,
    val activeWords: List<WordDto>,
)

data class WordDto(
    val word: String,
)
