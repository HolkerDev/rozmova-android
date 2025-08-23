package eu.rozmova.app.modules.userinit

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.domain.Level
import eu.rozmova.app.services.UserInitData
import eu.rozmova.app.services.UserInitState.CreatingBucket
import eu.rozmova.app.services.UserInitState.Error
import eu.rozmova.app.services.UserInitState.Finished
import eu.rozmova.app.services.UserInitState.Idle
import eu.rozmova.app.services.UserInitState.SavingData
import eu.rozmova.app.services.UserService
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class UserInitState(
    val isReady: Boolean = false,
    val error: Boolean = false,
)

@HiltViewModel
class UserInitVM
    @Inject
    constructor(
        private val userService: UserService,
    ) : ViewModel(),
        ContainerHost<UserInitState, Unit> {
        override val container: Container<UserInitState, Unit> = container(UserInitState())

        init {
            subscribeToUserInitProgress()
        }

        private fun subscribeToUserInitProgress() =
            intent {
                userService.userInitProgress.collect { events ->
                    when (events) {
                        CreatingBucket -> {
                            reduce { state.copy(isReady = false) }
                        }
                        Error -> {
                            reduce { state.copy(error = true) }
                        }
                        Finished -> {
                            reduce { state.copy(isReady = true) }
                        }
                        Idle -> {
                            reduce { state.copy(isReady = false) }
                        }
                        SavingData -> {
                            reduce { state.copy(isReady = false) }
                        }
                    }
                }
            }

        fun initUser(
            hobbies: Set<String>,
            job: String?,
            pronoun: String,
            level: Level,
        ) = intent {
            Log.i("UserInitVM", "initUser: $hobbies, $job, $pronoun, $level")
            userService.startUserInit(
                UserInitData(
                    job = job,
                    hobbies = hobbies.toList(),
                    pronoun = pronoun,
                    level = level,
                ),
            )
        }
    }
