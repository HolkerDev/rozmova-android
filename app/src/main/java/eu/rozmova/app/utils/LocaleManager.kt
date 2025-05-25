package eu.rozmova.app.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun setLocale(languageCode: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                    LocaleList(Locale(languageCode))
            } else {
                updateResources(context, languageCode)
            }
        }

        fun getCurrentLocale(): Locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)?.getApplicationLocales()?.get(0)
                    ?: Locale.getDefault()
            } else {
                context.resources.configuration.locales
                    .get(0)
            }

        @Suppress("DEPRECATION")
        private fun updateResources(
            context: Context,
            language: String,
        ) {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = resources.configuration

            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)

            val displayMetrics = resources.displayMetrics
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, displayMetrics)

//            // Recreate the activity to apply changes
//            if (context is Activity) {
//                context.recreate()
//            }
        }
    }
