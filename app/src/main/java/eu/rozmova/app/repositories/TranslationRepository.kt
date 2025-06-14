package eu.rozmova.app.repositories

import android.util.Log
import arrow.core.Either
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
        ): Either<InfraErrors, TranslationProposal> =
            Either
                .catch {
                    val response =
                        translationClient.fetchTranslationProposal(
                            TranslationProposalReq(
                                phrase = text,
                                targetLang = learnLanguage,
                                sourceLang = userLanguage,
                                chatId = chatId,
                            ),
                        )

                    if (!response.isSuccessful) {
                        throw InfraErrors.NetworkError(
                            "Error trying to fetch translation proposal: ${response.errorBody()}",
                        )
                    }

                    val responseBody =
                        response.body()
                            ?: throw InfraErrors.NetworkError("Empty response body from translation proposal")
                    TranslationProposal(
                        translation = responseBody.phrase,
                        notes = responseBody.notes,
                    )
                }.mapLeft { error ->
                    Log.e(tag, "Error trying to fetch scenarios", error)
                    InfraErrors.NetworkError("Error trying to fetch scenarios: $error")
                }
    }
