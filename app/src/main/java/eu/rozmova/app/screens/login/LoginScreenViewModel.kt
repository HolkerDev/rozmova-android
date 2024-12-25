package eu.rozmova.app.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.rozmova.app.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    data object Initial : LoginState()
    data object Loading : LoginState()
}

@HiltViewModel
class LoginScreenViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Initial)
    val state = _state.asStateFlow()

     fun login() {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            authRepository.signInWithGoogle()
        }
    }
}