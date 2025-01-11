package eu.rozmova.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RozmovaApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
