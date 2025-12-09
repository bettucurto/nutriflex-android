package ui.login

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.auth.R
import components.ClickableTextComponent
import components.DividerTextComponent
import components.HeadingTextComponent
import components.NFButton
import components.PasswordTextField
import components.RegularTextField
import components.TitleText
import theme.AppTheme


@Composable
fun LoginScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()){
    AppTheme(){
        val focusManager = LocalFocusManager.current

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

                RegularTextField(stringResource(R.string.textField1LoginScreen),
                    Icons.Outlined.Email,
                    onTextSelected = {
                        loginViewModel.onEvent((LoginUIEvent.LoginEmailChanged(it)))
                })
                PasswordTextField(stringResource(R.string.textField2LoginScreen),
                    Icons.Outlined.Lock,
                    onTextSelected = {
                        loginViewModel.onEvent((LoginUIEvent.LoginPasswordChanged(it)))
                    })

                Spacer(Modifier.height(250.dp))

                NFButton(stringResource(id= R.string.btnLoginScreen), onButtonClicked = {loginViewModel.onEvent(
                    LoginUIEvent.LoginButtonClicked)})
                DividerTextComponent()
                ClickableTextComponent(onClick = {navController.navigate("welcomeScreen")})
            }
        }
    }
}


//@Preview
//@Composable
//fun LoginPreview(){
//    LoginScreen()
//}