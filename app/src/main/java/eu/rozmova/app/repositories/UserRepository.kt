package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import com.google.firebase.firestore.FirebaseFirestore
import eu.rozmova.app.clients.backend.UserClient
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class UserRepositoryError {
    object FirebaseError : UserRepositoryError()

    object UserNotFound : UserRepositoryError()
}

@Singleton
class UserRepository
    @Inject
    constructor(
        private val userClient: UserClient,
        firestore: FirebaseFirestore,
    ) {
        private val usersCollection = firestore.collection("users")

        suspend fun fetchUserGroups(userId: String): Either<UserRepositoryError, List<String>> =
            Either
                .catch {
                    val userDocument =
                        usersCollection
                            .document("userId#$userId")
                            .get()
                            .await()
                            .data
                            ?: return Either.Left(UserRepositoryError.UserNotFound)

                    (userDocument["groups"] as? List<*>)
                        ?.filterIsInstance<String>()
                        .orEmpty()
                }.mapLeft {
                    Log.e("UserRepository", "Error trying to fetch user groups", it)
                    UserRepositoryError.FirebaseError
                }

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
