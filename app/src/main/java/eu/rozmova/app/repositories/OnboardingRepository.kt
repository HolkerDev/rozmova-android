package eu.rozmova.app.repositories

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
            return response.isSuccessful
        }
    }
