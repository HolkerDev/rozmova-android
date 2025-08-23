package eu.rozmova.app.repositories

import android.util.Log
import eu.rozmova.app.clients.backend.UserClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository
    @Inject
    constructor(
        private val userClient: UserClient,
    ) {
        suspend fun isOnboardingComplete(): Boolean {
            val response = userClient.getUserPreferences()

            Log.i("OnboardingRepository", "isOnboardingComplete: ${response.body()}")
            Log.i("OnboardingRepository", "isOnboardingComplete: ${response.code()}")
            Log.i("OnboardingRepository", "isOnboardingComplete: ${response.isSuccessful}")
            return response.isSuccessful
        }
    }
