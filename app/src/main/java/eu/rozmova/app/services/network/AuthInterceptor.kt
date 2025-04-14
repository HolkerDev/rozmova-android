package eu.rozmova.app.services.network

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val supabaseClient: SupabaseClient,
    ) : Interceptor {
        private val tag = this::class.java.simpleName

        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Add Content-Type header to all requests
            val requestWithContentType = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build()

            // If the user is not authenticated, proceed with content type header only
            if (supabaseClient.auth.currentSessionOrNull() == null) {
                Log.d(tag, "User not authenticated, skipping token")
                return chain.proceed(requestWithContentType)
            }

            // Get the latest access token
            val accessToken =
                runBlocking {
                    try {
                        supabaseClient.auth.currentSessionOrNull()?.accessToken
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to get access token", e)
                        null
                    }
                }

            // If token retrieval failed, proceed with content type header only
            if (accessToken == null) {
                Log.w(tag, "Access token is null, proceeding without token")
                return chain.proceed(requestWithContentType)
            }

            // Add the token to the request
            val authorizedRequest =
                requestWithContentType
                    .newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()

            return chain.proceed(authorizedRequest)
        }
    }
