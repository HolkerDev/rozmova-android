package eu.rozmova.app

import android.app.Application
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import android.util.Log
import com.amplifyframework.AmplifyException

class RozmovaApp: Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("RozmovaApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("RozmovaApp", "Could not initialize Amplify", error)
        }
    }
}