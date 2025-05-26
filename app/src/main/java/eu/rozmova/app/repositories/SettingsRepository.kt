package eu.rozmova.app.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.rozmova.app.domain.Language
import eu.rozmova.app.utils.LocaleManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val learnLangKey = stringPreferencesKey("learn_lang")

@Singleton
class SettingsRepository
    @Inject
    constructor(
        private val localeManager: LocaleManager,
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore: DataStore<Preferences> = context.dataStore

        fun getInterfaceLang(): String = localeManager.getCurrentLocale().language

        suspend fun getLearningLang(): String? =
            dataStore.data
                .map { preferences ->
                    preferences[learnLangKey]
                }.first()

        suspend fun getLearningLangOrDefault(): String = getLearningLang() ?: Language.GERMAN.code

        suspend fun setLearningLang(lang: String) {
            dataStore.edit { preferences ->
                preferences[learnLangKey] = lang
            }
        }

        suspend fun clearLearningLang() {
            dataStore.edit { preferences ->
                preferences.remove(learnLangKey)
            }
        }
    }
