package eu.rozmova.app.utils

import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ChatStatus
import eu.rozmova.app.domain.ChatType
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.LangDto
import eu.rozmova.app.domain.ReviewDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto

object MockData {
    fun mockScenarioDto(): ScenarioDto =
        ScenarioDto(
            id = "1",
            title = "Sample Conversation",
            situation = "A casual conversation with a friend about weekend plans",
            difficulty = DifficultyDto.EASY,
            scenarioType = ScenarioTypeDto.CONVERSATION,
            createdAt = "",
            userLang = LangDto.EN,
            scenarioLang = LangDto.EN,
            labels = emptyList(),
            helperWords = emptyList(),
            userInstructions = emptyList(),
        )

    fun mockChatDto(scenarioDto: ScenarioDto = mockScenarioDto()): ChatDto =
        ChatDto(
            id = "chat_1",
            scenario = scenarioDto,
            status = ChatStatus.FINISHED,
            chatType = ChatType.SPEAKING,
            messages = listOf(),
        )

    fun mockReviewDto(chatDto: ChatDto = mockChatDto()) =
        ReviewDto(
            id = "preview_review_1",
            taskCompletion =
                eu.rozmova.app.domain.TaskCompletionDto(
                    isCompleted = true,
                    metInstructions = listOf("Used polite language"),
                    missedInstructions = listOf(),
                    mistakes = listOf(),
                    rating = 3,
                ),
            topicsToReview = listOf("Past tense", "Question formation"),
            wordsToLearn = listOf("appointment", "schedule"),
            chat = chatDto,
        )
}
