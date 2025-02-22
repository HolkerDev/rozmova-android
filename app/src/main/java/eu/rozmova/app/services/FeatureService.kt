package eu.rozmova.app.services

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.customSignals
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureService
    @Inject
    constructor(
        private val remoteConfig: FirebaseRemoteConfig,
    ) {
        fun initialize(userGroups: List<String>) {
            val configSettings =
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = 3600 // 1 hour cache
                }
            remoteConfig.setConfigSettingsAsync(configSettings)

            val customSignals =
                customSignals {
                    put("user_group", userGroups.joinToString(","))
                }

            // Apply signals correctly
            remoteConfig
                .setCustomSignals(
                    customSignals,
                ).addOnCompleteListener {
                    // Ensure Remote Config gets updated AFTER custom signals are set
                    remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FeatureService", "Config params updated")
                        } else {
                            Log.e("FeatureService", "Remote Config fetch failed", task.exception)
                        }
                    }
                }
        }

        fun isFeatureEnabled(feature: Feature): Boolean = remoteConfig.getBoolean(feature.key)
    }
