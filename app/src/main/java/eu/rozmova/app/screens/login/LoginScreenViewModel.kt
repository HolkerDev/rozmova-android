package eu.rozmova.app.screens.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.repositories.AuthRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class LoginScreenViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel(),
        ContainerHost<LoginUiState, Unit> {
        override val container: Container<LoginUiState, Unit> = container(LoginUiState())

        fun login() =
            intent {
                reduce {
                    state.copy(isLoading = true, errorMessage = null)
                }

                authRepository
                    .signInWithGoogle()
                    .mapLeft { errorMessage ->
                        reduce {
                            state.copy(isLoading = false, errorMessage = errorMessage)
                        }
                    }.map {
                        reduce {
                            state.copy(isLoading = false, errorMessage = null)
                        }
                    }
            }
    }
