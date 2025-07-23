package eu.rozmova.app.repositories

import android.util.Log
import eu.rozmova.app.clients.backend.TranslationClient
import eu.rozmova.app.clients.backend.TranslationProposalReq
import eu.rozmova.app.domain.TranslationProposal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepository
    @Inject
    constructor(
        private val translationClient: TranslationClient,
    ) {
        private val tag = "TranslationRepository"

        suspend fun genProposal(
            text: String,
            learnLanguage: String,
            userLanguage: String,
            chatId: String,
        ): Result<TranslationProposal> =
            try {
                val response =
                    translationClient.fetchTranslationProposal(
                        TranslationProposalReq(
                            phrase = text,
                            targetLang = learnLanguage,
                            sourceLang = userLanguage,
                            chatId = chatId,
                        ),
                    )
                when {
                    response.code() == 429 -> {
                        Result.failure(UsageLimitReachedException("Usage limit reached"))
                    }
                    !response.isSuccessful -> {
                        Result.failure(InfraErrors.NetworkError("Error trying to fetch translation proposal: ${response.errorBody()}"))
                    }
                    else -> {
                        val responseBody =
                            response.body()
                                ?: return Result.failure(InfraErrors.NetworkError("Empty response body from translation proposal"))
                        Result.success(
                            TranslationProposal(
                                translation = responseBody.phrase,
                                notes = responseBody.notes,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error trying to fetch scenarios", e)
                Result.failure(InfraErrors.NetworkError("Error trying to fetch scenarios: $e"))
            }
    }
