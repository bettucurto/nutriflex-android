package ui.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.repository.AuthRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

//Tipo a funcionalidade do UI, o JS pro HTML
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var loginUIState by mutableStateOf(LoginUIState())
        private set

    fun onEvent(event: LoginUIEvent) {
        when (event) {
            is LoginUIEvent.LoginEmailChanged -> {
                loginUIState = loginUIState.copy(email = event.email)
            }
            is LoginUIEvent.LoginPasswordChanged -> {
                loginUIState = loginUIState.copy(password = event.password)
            }
            is LoginUIEvent.LoginButtonClicked -> {
                doLogin()
            }
        }
    }

    private fun doLogin() {
        Log.d("Login", "doLogin chamado com email=${loginUIState.email}")
        viewModelScope.launch {
            loginUIState = loginUIState.copy(
                isLoading = true,
                errorMessage = null
            )
            Log.d("Login", "Antes de chamar authRepository.login")

            val result = authRepository.login(
                email = loginUIState.email,
                password = loginUIState.password
            )

            result
                .onSuccess {
                    loginUIState = loginUIState.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
                .onFailure { e ->
                    loginUIState = loginUIState.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Erro no login"
                    )
                }
            Log.d("Login", "Depois de chamar authRepository.login, result=$result")
        }
    }
}
