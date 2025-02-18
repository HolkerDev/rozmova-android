package eu.rozmova.app.domain

import eu.rozmova.app.R

sealed class Language(
    val resId: Int,
    val code: String,
) {
    data object ENGLISH : Language(R.string.lang_en, "en")

    data object GERMAN : Language(R.string.lang_de, "de")

    data object GREEK : Language(R.string.lang_el, "el")

    data object UKRAINIAN : Language(R.string.lang_uk, "uk")

    data object RUSSIAN : Language(R.string.lang_ru, "ru")
}

fun getLanguageByCode(code: String): Language =
    when (code) {
        "en" -> Language.ENGLISH
        "de" -> Language.GERMAN
        "el" -> Language.GREEK
        "uk" -> Language.UKRAINIAN
        "ru" -> Language.RUSSIAN
        else -> Language.ENGLISH
    }

fun Language.toDatabaseName(): String =
    when (this) {
        Language.ENGLISH -> "English"
        Language.GERMAN -> "German"
        Language.GREEK -> "Greek"
        Language.UKRAINIAN -> "Ukrainian"
        Language.RUSSIAN -> "Russian"
    }

fun getLanguageByDatabaseName(displayName: String): Language =
    when (displayName) {
        "English" -> Language.ENGLISH
        "German" -> Language.GERMAN
        "Greek" -> Language.GREEK
        "Ukrainian" -> Language.UKRAINIAN
        "Russian" -> Language.RUSSIAN
        else -> Language.ENGLISH
    }

val LEARN_LANGUAGES = listOf(Language.GERMAN, Language.GREEK)

val INTERFACE_LANGUAGES =
    listOf(
        Language.ENGLISH,
        Language.UKRAINIAN,
        Language.RUSSIAN,
    )
