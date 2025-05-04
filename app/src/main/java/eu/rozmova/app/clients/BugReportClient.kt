package eu.rozmova.app.clients

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BugReportClient {
    @POST("report/bug")
    suspend fun reportBug(
        @Body body: SendBugReportReq,
    ): Response<Unit>
}

data class SendBugReportReq(
    val title: String,
    val description: String,
)
