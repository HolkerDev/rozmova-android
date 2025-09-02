package eu.rozmova.app.repositories

import eu.rozmova.app.clients.backend.BucketClient
import eu.rozmova.app.domain.BucketDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BucketRepository
    @Inject
    constructor(
        private val bucketClient: BucketClient,
    ) {
        suspend fun fetchBucket(): Result<BucketDto> {
            val response = bucketClient.fetchBucket()
            return if (response.isSuccessful) {
                val bucket = response.body()
                if (bucket != null) {
                    Result.success(bucket)
                } else {
                    Result.failure(Exception("Empty bucket response"))
                }
            } else {
                Result.failure(Exception("Failed to fetch bucket: ${response.code()} ${response.message()}"))
            }
        }
    }
