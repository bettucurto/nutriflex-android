package ui.login

sealed class LoginUIEvent {

    data class LoginEmailChanged(val email:String) : LoginUIEvent()
    data class LoginPasswordChanged(val password:String) : LoginUIEvent()

    object LoginButtonClicked: LoginUIEvent()

}