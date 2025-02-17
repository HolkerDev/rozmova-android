package eu.rozmova.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserPreference(
    val learningLanguage: String,
    val hasGreekEnabled: Boolean? = false,
) {
    companion object {
        val DEFAULT =
            UserPreference(
                learningLanguage = "German",
                hasGreekEnabled = false,
            )
    }
}
