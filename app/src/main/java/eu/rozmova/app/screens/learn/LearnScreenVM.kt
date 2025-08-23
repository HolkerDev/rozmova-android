package eu.rozmova.app.screens.learn

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.ChatDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.TodayScenarioSelection
import eu.rozmova.app.repositories.ChatsRepository
import eu.rozmova.app.repositories.ScenariosRepository
import eu.rozmova.app.repositories.SettingsRepository
import eu.rozmova.app.state.AppStateRepository
import eu.rozmova.app.utils.LocaleManager
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

sealed class LearnEvent {
    data object StartOnboarding : LearnEvent()
}

data class LearnScreenState(
    val weeklyScenarios: List<ScenarioDto>? = null,
    val weeklyScenariosLoading: Boolean = false,
    val recommendedScenarios: TodayScenarioSelection? = null,
    val latestChat: ChatDto? = null,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class LearnScreenVM
    @Inject
    constructor(
        private val scenariosRepository: ScenariosRepository,
        private val settingsRepository: SettingsRepository,
        private val chatsRepository: ChatsRepository,
        private val localeManager: LocaleManager,
        private val appStateRepository: AppStateRepository,
    ) : ViewModel(),
        ContainerHost<LearnScreenState, LearnEvent> {
        override val container: Container<LearnScreenState, LearnEvent> = container(LearnScreenState())

        init {
            Log.i("LearnScreenViewModel", "ViewModel created - hashCode: ${this.hashCode()}")
            initialize()
        }

        private fun initialize() =
            intent {
                Log.i("LearnScreenViewModel", "Initializing LearnScreenViewModel - hashCode: ${this@LearnScreenVM.hashCode()}")
                fetchTodayScenarios()
                fetchLatestChat()
                fetchWeeklyScenarios()
                observeLanguageChanges()
            }

        private fun observeLanguageChanges() =
            intent {
                appStateRepository.refetch.collect {
                    fetchTodayScenarios()
                    fetchLatestChat()
                    fetchWeeklyScenarios()
                }
            }

        fun refresh() =
            intent {
                reduce { state.copy(isRefreshing = true) }
                try {
                    val learnLang = settingsRepository.getLearningLangOrDefault()
                    val userLang = localeManager.getCurrentLocale().language

                    // Fetch all data concurrently and wait for completion
                    val todayScenarios = scenariosRepository.getTodaySelection(userLang, learnLang)
                    val latestChat = chatsRepository.fetchLatest(learnLang, userLang)
                    val weeklyScenarios = scenariosRepository.weeklyScenarios(userLang = userLang, scenarioLang = learnLang)

                    todayScenarios.map { recScenarios ->
                        reduce { state.copy(recommendedScenarios = recScenarios) }
                    }

                    latestChat.map { chat ->
                        reduce { state.copy(latestChat = chat) }
                    }

                    weeklyScenarios.map { scenarios ->
                        reduce { state.copy(weeklyScenarios = scenarios) }
                    }
                } finally {
                    reduce { state.copy(isRefreshing = false) }
                }
            }

        private fun fetchTodayScenarios() =
            intent {
                val learnLang = settingsRepository.getLearningLangOrDefault()
                val userLang = localeManager.getCurrentLocale().language
                scenariosRepository
                    .getTodaySelection(userLang, learnLang)
                    .onSuccess { recScenarios ->
                        reduce { state.copy(recommendedScenarios = recScenarios) }
                    }.onFailure { error ->
                        Log.e("LearnScreenVM", "Error fetching today's scenarios", error)
                    }
            }

        private fun fetchLatestChat() =
            intent {
                val learnLang = settingsRepository.getLearningLangOrDefault()
                val userLang = localeManager.getCurrentLocale().language
                chatsRepository.fetchLatest(learnLang, userLang).map {
                    reduce { state.copy(latestChat = it) }
                }
            }

        private fun fetchWeeklyScenarios() =
            intent {
                reduce { state.copy(weeklyScenariosLoading = true) }
                val userLang = localeManager.getCurrentLocale().language
                val learnLang = settingsRepository.getLearningLangOrDefault()

                scenariosRepository.getAllWithFilter(userLang = userLang, scenarioLang = learnLang, null, null).map { scenarios ->
                    reduce { state.copy(weeklyScenarios = scenarios, weeklyScenariosLoading = false) }
                }
            }
    }
