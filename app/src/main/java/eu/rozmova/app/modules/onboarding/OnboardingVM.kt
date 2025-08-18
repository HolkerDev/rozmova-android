package eu.rozmova.app.modules.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Level
import eu.rozmova.app.services.UserInitData
import eu.rozmova.app.services.UserInitState
import eu.rozmova.app.services.UserService
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

sealed interface OnboardingEvent

data class OnboardingScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class OnboardingVM
    @Inject
    constructor(
        private val userService: UserService,
    ) : ViewModel(),
        ContainerHost<OnboardingScreenState, Unit> {
        override val container = container<OnboardingScreenState, Unit>(OnboardingScreenState())

        private fun subscribeToUserInitProgress() =
            intent {
                userService.userInitProgress.collect {
                    when (it) {
                        UserInitState.CreatingBucket -> {
                        }
                        UserInitState.Error -> TODO()
                        UserInitState.Finished -> TODO()
                        UserInitState.Idle -> TODO()
                        UserInitState.SavingData -> TODO()
                    }
                }
            }

        fun initUser(
            job: String?,
            hobbies: List<String>,
            pronoun: String,
            level: Level,
        ) = intent {
            userService.startUserInit(UserInitData(job, hobbies, pronoun, level))
        }
    }
