package eu.rozmova.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import eu.rozmova.app.services.billing.BillingService
import eu.rozmova.app.services.billing.BillingServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {

    @Binds
    @Singleton
    abstract fun bindBillingService(
        billingServiceImpl: BillingServiceImpl
    ): BillingService
}