package eu.rozmova.app.repositories

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository
    @Inject
    constructor() {
        fun isOnboardingComplete(): Boolean = false
    }
