package ui.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.auth.R
import components.ClickableTextComponent
import components.DividerTextComponent
import components.HeadingTextComponent
import components.IconTextField
import components.NFButton
import components.PasswordTextField
import components.TitleText
import theme.AppTheme


@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()){
    AppTheme(){
        val focusManager = LocalFocusManager.current
        val state = viewModel.loginUIState

        // Navegar quando o login tiver sucesso
        LaunchedEffect(state.isSuccess) {
            if (state.isSuccess) {
                navController.navigate("homeScreen") {
                    popUpTo("loginScreen") { inclusive = true }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorScheme.surface)
                .padding(vertical = 60.dp, horizontal = 35.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ){
                    focusManager.clearFocus()
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                HeadingTextComponent(stringResource(R.string.headingTitleLoginScreen))
                TitleText(stringResource(R.string.titleLoginScreen))
                Spacer(Modifier.height(30.dp))

                IconTextField(stringResource(R.string.textField1LoginScreen),
                    Icons.Outlined.Email,
                    error = state.emailError,
                    onTextSelected = {
                        viewModel.onEvent((LoginUIEvent.LoginEmailChanged(it)))
                })
                PasswordTextField(stringResource(R.string.textField2LoginScreen),
                    Icons.Outlined.Lock,
                    error = state.passwordError,
                    onTextSelected = {
                        viewModel.onEvent((LoginUIEvent.LoginPasswordChanged(it)))
                    })

                if (state.errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = state.errorMessage,
                        color = colorScheme.error
                    )
                }
                Spacer(Modifier.weight(1f))

                NFButton(stringResource(id= R.string.btnLoginScreen), onButtonClicked = {viewModel.onEvent(
                    LoginUIEvent.LoginButtonClicked)
                            Log.d("Login", "Botão login clicado")})
                DividerTextComponent()
                ClickableTextComponent(onClick = {navController.navigate("registrationScreen1")})
                Spacer(Modifier.weight(0.1f))
            }
        }
    }
}


//@Preview
//@Composable
//fun LoginPreview(){
//    LoginScreen()
//}