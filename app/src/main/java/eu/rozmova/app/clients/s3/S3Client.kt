package eu.rozmova.app.clients.s3

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3Client
    @Inject
    constructor() {
        fun uploadFile(
            presignedUrl: String,
            file: File,
        ) {
            try {
                val client = OkHttpClient()

                val requestBody = file.asRequestBody("application/octet-stream".toMediaType())

                val request =
                    Request
                        .Builder()
                        .url(presignedUrl)
                        .put(requestBody)
                        .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw RuntimeException("Upload failed: ${response.code} ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("S3Client", "Failed to upload file", e)
                throw e
            }
        }
    }
