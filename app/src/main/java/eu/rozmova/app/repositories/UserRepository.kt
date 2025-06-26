package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.backend.UserClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository
    @Inject
    constructor(
        private val userClient: UserClient,
    ) {
        suspend fun deleteUser(): Either<InfraErrors, Unit> =
            Either
                .catch {
                    val response = userClient.deleteUser()
                    if (!response.isSuccessful) {
                        throw InfraErrors.NetworkError(
                            "Error deleting user: ${response.body()}",
                        )
                    }
                }.mapLeft { error ->
                    Log.e("UserRepository", "Error deleting user", error)
                    InfraErrors.NetworkError("Error deleting user: ${error.message}")
                }
    }
