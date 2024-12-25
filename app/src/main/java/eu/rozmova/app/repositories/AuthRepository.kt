package eu.rozmova.app.repositories

import android.util.Log
import eu.rozmova.app.di.GoogleAuthProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    data object Loading : AuthState()
    data class Authenticated(val userSession: UserSession) : AuthState()
    data object Unauthenticated : AuthState()
}

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val googleAuthProvider: GoogleAuthProvider
) {
    private val tag = this::class.java.simpleName

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    suspend fun observeAuthState() {
        try {
            val session = supabaseClient.auth.currentSessionOrNull()
            _authState.value = session?.let { AuthState.Authenticated(it) }
                ?: AuthState.Unauthenticated

            supabaseClient.auth.sessionStatus
                .collect { sessionStatus ->
                    Log.i(tag, "Session status: $sessionStatus")
                    _authState.value = when (sessionStatus) {
                        is SessionStatus.Authenticated ->
                            AuthState.Authenticated(sessionStatus.session)

                        SessionStatus.Initializing ->
                            AuthState.Loading

                        is SessionStatus.NotAuthenticated,
                        is SessionStatus.RefreshFailure ->
                            AuthState.Unauthenticated
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to observe auth state", e)
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun signOut() {
        try {
            supabaseClient.auth.signOut()
        } catch (e: Exception) {
            Log.e(tag, "Sign out failed", e)
            throw e
        }
    }

    suspend fun signInWithGoogle() {
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val hashedNonce = createNonce(rawNonce)
            val googleCredential = googleAuthProvider.getGoogleCredential(hashedNonce).getOrThrow()

            supabaseClient.auth.signInWith(IDToken) {
                idToken = googleCredential
                provider = Google
                nonce = rawNonce
            }
        } catch (e: Exception) {
            Log.e(tag, "Sign in with Google failed", e)
            _authState.value = AuthState.Unauthenticated
        }
    }
}

fun createNonce(rawNonce: String): String {
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}