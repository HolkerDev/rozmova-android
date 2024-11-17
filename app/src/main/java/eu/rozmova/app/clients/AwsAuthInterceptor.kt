package eu.rozmova.app.clients

import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AwsAuthInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response {
        // We have to use runBlocking since intercept is synchronous
        val token = runBlocking {
            suspendCoroutine { continuation ->
                Amplify.Auth.fetchAuthSession(
                    { authSession ->
                        val session = authSession as AWSCognitoAuthSession
                        val idToken = session.userPoolTokensResult.value?.idToken
                        if (idToken != null) {
                            continuation.resume(idToken)
                        } else {
                            continuation.resumeWithException(Exception("No token found"))
                        }
                    },
                    { error ->
                        continuation.resumeWithException(error)
                    }
                )
            }
        }

        val request = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()

        return chain.proceed(request)
    }
}