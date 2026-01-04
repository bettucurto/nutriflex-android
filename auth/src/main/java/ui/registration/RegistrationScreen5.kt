package ui.registration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.auth.R
import components.BackButton
import components.IconTextField
import components.LeftHeadingTextComponent
import components.LeftTitleText
import components.NFButton
import components.PasswordTextField
import components.StepIndicators
import kotlinx.coroutines.launch
import theme.AppTheme


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen5(navController: NavController, viewModel: RegisterViewModel = hiltViewModel()){
    AppTheme() {
        val focusManager = LocalFocusManager.current
        val state = viewModel.registerUIState
        val difficultyOptions = listOf(
            "I've had a lot of difficulties",
            "I've had some difficulties",
            "I've never had difficulties",
            "I've never tried"
        )

        Surface(modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ){
                focusManager.clearFocus()
            })
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 19.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                LeftTitleText(stringResource(R.string.titleRegistration3))
                Spacer(modifier = Modifier.heightIn(20.dp))
                StepIndicators(4)
                LeftHeadingTextComponent(stringResource(R.string.SubTitleRegistration5))
                Spacer(Modifier.weight(0.1f))


                IconTextField(stringResource(R.string.textField1LoginScreen),
                    Icons.Outlined.Email,
                    error = state.emailError,
                    onTextSelected = {
                        viewModel.onEvent((RegisterUIEvent.RegisterEmailChanged(it)))
                    })
                PasswordTextField(stringResource(R.string.textField2LoginScreen),
                    Icons.Outlined.Lock,
                    error = state.passwordError,
                    onTextSelected = {
                        viewModel.onEvent((RegisterUIEvent.RegisterPasswordChanged(it)))
                    })

                if (state.registerError != null) {
                    Text(
                        text = state.registerError,
                        color = colorScheme.error
                    )
                }


                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally )
                )
                {
                    BackButton(onButtonClicked = {navController.popBackStack()})
                    val coroutineScope = rememberCoroutineScope()
                    NFButton(
                        text = stringResource(R.string.buttonRegisterScreen5),
                        onButtonClicked = {
                            Log.d("Register", "Create account clicked")
                            // validação simples do email/password antes
                            viewModel.onEvent(RegisterUIEvent.NextClickedStep5)
                            val newState = viewModel.registerUIState
                            val hasEmailError = newState.emailError != null || newState.email.isBlank()
                            val hasPasswordError = newState.passwordError != null || newState.password.isBlank()
                            Log.d("Register", "email='${newState.email}', hasEmailError=$hasEmailError")
                            Log.d("Register", "password='${newState.password}', hasPasswordError=$hasPasswordError")
                            if (hasEmailError || hasPasswordError) return@NFButton

                            coroutineScope.launch {
                                viewModel.finalizeAndRegister { result ->
                                    result
                                        .onSuccess {
                                            navController.navigate("homeScreen") {
                                                popUpTo("registrationScreen") { inclusive = true }
                                            }
                                        }
                                        .onFailure {
                                            // aqui podes pôr um erro geral no estado, tipo registerErrorMessage
                                        }
                                }
                            }
                        }
                    )
                }
                Spacer(Modifier.weight(0.1f))
            }
        }
    }
}