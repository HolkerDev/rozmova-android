package eu.rozmova.app.services.network

import eu.rozmova.app.domain.ScenarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("scenarios/weekly")
    suspend fun fetchWeeklyScenarios(): Response<List<ScenarioDto>>

    @GET("scenarios/weekly")
    suspend fun getUserById(
        @Path("userId") userId: String,
    ): Response<UserResponse>

    @POST("users/update")
    suspend fun updateUser(
        @Body userRequest: UserRequest,
    ): Response<UserResponse>
}

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val createdAt: String,
)

data class UserRequest(
    val name: String,
)
