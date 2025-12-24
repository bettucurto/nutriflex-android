package ui.login
//Montar o state pro viewModel
data class LoginUIState (
    var email: String = "",
    var password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)