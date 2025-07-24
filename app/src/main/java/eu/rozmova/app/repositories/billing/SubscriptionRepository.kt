package eu.rozmova.app.repositories.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "subscription")
val isSubscribedKey = stringPreferencesKey("is_subscribed")

@Singleton
class SubscriptionRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore: DataStore<Preferences> = context.dataStore

        val isSubscribedFlow = dataStore.data.map { preferences ->
            preferences[isSubscribedKey] == "true"
        }

        suspend fun getIsSubscribed(): Boolean =
            isSubscribedFlow.first()

        suspend fun setIsSubscribed(isSubscribed: Boolean) {
            dataStore.edit { preferences ->
                preferences[isSubscribedKey] = isSubscribed.toString()
            }
        }
    }
