package ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    var loginUIState = mutableStateOf(LoginUIState())

    fun onEvent(event: LoginUIEvent){
        when(event){
            is LoginUIEvent.LoginEmailChanged -> {
                loginUIState.value = loginUIState.value.copy(
                    email = event.email
                )
            }
            is LoginUIEvent.LoginPasswordChanged -> {
                loginUIState.value = loginUIState.value.copy(
                    password = event.password
                )
            }
            is LoginUIEvent.LoginButtonClicked -> {

            }
        }
    }

}