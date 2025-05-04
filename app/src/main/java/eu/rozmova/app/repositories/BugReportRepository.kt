package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.BugReportClient
import eu.rozmova.app.clients.SendBugReportReq
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BugReportRepository
    @Inject
    constructor(
        private val bugReportClient: BugReportClient,
    ) {
        suspend fun sendBugReport(
            title: String,
            description: String,
        ): Either<InfraErrors, Unit> =
            Either
                .catch {
                    val response =
                        bugReportClient.reportBug(
                            SendBugReportReq(
                                title = title,
                                description = description,
                            ),
                        )

                    Log.d("BugReportRepository", "Response: $response")

                    if (response.isSuccessful) {
                        Unit
                    } else {
                        throw InfraErrors.NetworkError("${response.code()} ${response.message()}")
                    }
                }.mapLeft { error ->
                    Log.e("BugReportRepository", "Error trying to send bug report", error)
                    InfraErrors.NetworkError("${error.message}")
                }
    }
