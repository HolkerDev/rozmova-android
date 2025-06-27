package eu.rozmova.app.di

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import eu.rozmova.app.BuildConfig
import javax.inject.Inject

class GoogleAuthProvider
    @Inject
    constructor(
        private val credentialManager: CredentialManager,
    ) {
        suspend fun getGoogleCredential(
            activity: Activity,
            hashedNonce: String,
        ): Result<String> =
            try {
                println(BuildConfig.GOOGLE_CLIENT_ID)
                val googleIdOption =
                    GetGoogleIdOption
                        .Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                        .setNonce(hashedNonce)
                        .build()

                val request =
                    GetCredentialRequest
                        .Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                val result = credentialManager.getCredential(activity, request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                Result.success(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                Result.failure(e)
            } catch (e: GoogleIdTokenParsingException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
    }
