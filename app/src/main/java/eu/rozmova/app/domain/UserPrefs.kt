package eu.rozmova.app.domain

data class UserPrefs(
    val hobbies: List<String>,
    val pronoun: String,
    val job: String? = null,
    val level: Level,
)
