package ui.registration

import android.os.Build
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.auth.R
import components.BackButton
import components.LeftHeadingTextComponent
import components.LeftTitleText
import components.NFButton
import components.NumberTextField
import components.StepIndicators
import theme.AppTheme


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen4(navController: NavController, viewModel: RegisterViewModel = hiltViewModel()){
    AppTheme() {
        val focusManager = LocalFocusManager.current
        val state = viewModel.registerUIState
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
                LeftTitleText(stringResource(R.string.titleRegistration2))
                Spacer(modifier = Modifier.heightIn(20.dp))
                StepIndicators(3)
                LeftHeadingTextComponent(stringResource(R.string.SubTitleRegistration1))
                Spacer(modifier = Modifier.heightIn(30.dp))
                NumberTextField(allowDecimal = false,textIcon = "CM",textState = state.height?.toString() ?: "", labelValue = stringResource(R.string.textField1RegisterScreen4),
                    error = state.heightError,
                    onTextSelected = {
                        viewModel.onEvent((RegisterUIEvent.RegisterHeightChanged(it.toInt())))
                    }
                )

                NumberTextField(supportText = stringResource(R.string.textField2RegisterScreen4SupportText),textIcon = "KG",textState = state.weight?.toString() ?: "", labelValue = stringResource(R.string.textField2RegisterScreen4),
                    error = state.weightError,
                    onTextSelected = {
                        viewModel.onEvent((RegisterUIEvent.RegisterWeightChanged(it.toFloat())))
                    }
                )
                Spacer(modifier = Modifier.weight(0.1f))

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top) {
                    NumberTextField(enabled = !state.autoWeightGoal, supportText = stringResource(R.string.textField3RegisterScreen4SupportText),textIcon = "KG",textState = state.weightGoal?.toString() ?: "", labelValue = stringResource(R.string.textField3RegisterScreen4),
                        error = state.weightGoalError,
                        onTextSelected = {
                            viewModel.onEvent((RegisterUIEvent.RegisterWeightGoalChanged(it.toFloat())))
                        },modifier = Modifier.weight(1f)
                    )
                    FilterChip(modifier = Modifier
                        .weight(0.45f)
                        .heightIn(70.dp),
                        selected = state.autoWeightGoal,
                        onClick = {
                            viewModel.onEvent(
                                RegisterUIEvent.ToggleAutoWeightGoal(!state.autoWeightGoal)
                            )
                        },
                        label = { Text("Choose for me",  textAlign = TextAlign.Center) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colorScheme.surfaceVariant,
                            labelColor = colorScheme.onSurfaceVariant,
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary
                        )
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
                    NFButton(text = stringResource(R.string.buttonRegisterScreen1), onButtonClicked = {viewModel.onEvent(RegisterUIEvent.NextClickedStep4)
                        val newState = viewModel.registerUIState
                        if (newState.isStep4Valid) {
                            navController.navigate("registrationScreen5")
                        }}
                    )
                }
                Spacer(Modifier.weight(0.15f))
            }
        }
    }
}