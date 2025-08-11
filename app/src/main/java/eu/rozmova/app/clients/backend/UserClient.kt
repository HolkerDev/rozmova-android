package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.UserPrefs
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface UserClient {
    @POST("v1/user/delete")
    suspend fun deleteUser(): Response<String>

    @GET("v1/user/prefs")
    suspend fun getUserPreferences(): Response<UserPrefsResp>
}

data class UserPrefsResp(
    val userPrefs: UserPrefs,
)
