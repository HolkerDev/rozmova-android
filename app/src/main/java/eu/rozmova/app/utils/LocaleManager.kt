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
            }
        }

        fun getCurrentLocale(): Locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)?.applicationLocales?.get(0)
                    ?: Locale.getDefault()
            } else {
                Locale.getDefault()
            }
    }
