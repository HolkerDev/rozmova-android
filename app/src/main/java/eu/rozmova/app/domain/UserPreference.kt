package eu.rozmova.app.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPreference(
    @SerialName("learning_language")
    val learningLanguage: String,
    @SerialName("has_greek_enabled")
    val hasGreekEnabled: Boolean? = false,
) {
    companion object {
        val DEFAULT =
            UserPreference(
                learningLanguage = Language.GERMAN.code,
                hasGreekEnabled = false,
            )
    }
}
