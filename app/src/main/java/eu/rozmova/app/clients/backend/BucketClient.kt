package eu.rozmova.app.clients.backend

import eu.rozmova.app.domain.BucketDto
import retrofit2.Response
import retrofit2.http.GET

interface BucketClient {
    @GET("v1/bucket")
    suspend fun fetchBucket(): Response<BucketDto>
}
