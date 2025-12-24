package ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.repository.AuthRepository
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

//Tipo a funcionalidade do UI, o JS pro HTML
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository
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
        viewModelScope.launch {
            var emailError: String? = null
            var passwordError: String? = null

            if (loginUIState.email.isBlank()) {
                emailError = "Email obrigatório"
            }
            if (loginUIState.password.isBlank()) {
                passwordError = "Password obrigatória"
            }

            // Se houver erro, atualiza o estado e não chama a API
            if (emailError != null || passwordError != null) {
                loginUIState = loginUIState.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
                return@launch
            }

            // limpa erros e segue para o login
            loginUIState = loginUIState.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null,
                passwordError = null
            )

            val result = authRepository.login(
                email = loginUIState.email,
                password = loginUIState.password
            )

            result
                .onSuccess { loginResponse ->
                    // Depois do login, buscar o utilizador + progresso pelo email
                    val userResult = authRepository.getUserByEmail(loginUIState.email)
                    userResult
                        .onSuccess { userDto ->
                            val progress = userDto.progress
                            val bmi = if (progress != null) {
                                // se tiver peso e altura, recalcula IMC
                                val h = userDto.altura
                                val w = progress.peso_atual
                                w / ((h / 100f) * (h / 100f))
                            } else 0f

                            val dailyCalories = progress?.calorias_diarias ?: 0
                            val goalWeight = progress?.peso_meta ?: progress?.peso_atual ?: 0f
                            val currentWeight = progress?.peso_atual ?: 0f

                            userLocalRepository.saveUserLocal(
                                userId = userDto.id,
                                token = loginResponse.token,
                                bmi = bmi,
                                currentWeight = currentWeight,
                                goalWeight = goalWeight,
                                dailyCalories = dailyCalories
                            )
                        }
                        .onFailure { e ->
                            // não bloqueia o login se esta call falhar
                        }

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
        }
    }
}
