package eu.rozmova.app.clients.backend

import retrofit2.Response
import retrofit2.http.POST

interface UserClient {
    @POST("v1/user/delete")
    suspend fun deleteUser(): Response<String>
}
