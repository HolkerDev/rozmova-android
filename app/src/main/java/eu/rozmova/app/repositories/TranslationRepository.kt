package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
import eu.rozmova.app.clients.backend.TranslationClient
import eu.rozmova.app.clients.backend.TranslationProposalReq
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
            targetLanguage: String,
            chatId: String,
        ): Either<InfraErrors, String> =
            Either
                .catch {
                    val response =
                        translationClient.fetchTranslationProposal(
                            TranslationProposalReq(
                                phrase = text,
                                targetLang = targetLanguage,
                                chatId = chatId,
                            ),
                        )
                    Log.i("", "Scenarios: $response")
                    if (response.isSuccessful) {
                        response.body()?.phrase
                            ?: throw InfraErrors.NetworkError("Empty response body from translation proposal")
                    }
                    throw InfraErrors.NetworkError(
                        "Error trying to fetch translation proposal: ${response.errorBody()}",
                    )
                }.mapLeft { error ->
                    Log.e("ScenariosRepository", "Error trying to fetch scenarios", error)
                    InfraErrors.NetworkError("Error trying to fetch scenarios: $error")
                }
    }
