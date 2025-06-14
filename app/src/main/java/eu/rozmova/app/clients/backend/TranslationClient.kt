package eu.rozmova.app.clients.backend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TranslationClient {
    @POST("v1/translation/proposal")
    suspend fun fetchTranslationProposal(
        @Body body: TranslationProposalReq,
    ): Response<TranslationProposalResp>
}

data class TranslationProposalReq(
    val targetLang: String,
    val sourceLang: String,
    val phrase: String,
    val chatId: String,
)

data class TranslationProposalResp(
    val phrase: String,
    val notes: List<String>,
)
