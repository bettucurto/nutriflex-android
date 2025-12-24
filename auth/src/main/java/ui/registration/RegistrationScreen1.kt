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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.auth.R
import components.BackButton
import components.BirthDateField
import components.GenderButtonGroup
import components.LeftHeadingTextComponent
import components.LeftTitleText
import components.NFButton
import components.RegularTextField
import components.StepIndicators
import theme.AppTheme


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen1(navController: NavController, viewModel: RegisterViewModel = hiltViewModel()){
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
                LeftTitleText(stringResource(R.string.titleRegistration1))
                Spacer(modifier = Modifier.heightIn(20.dp))
                StepIndicators(0)
                LeftHeadingTextComponent(stringResource(R.string.SubTitleRegistration1))
                Spacer(modifier = Modifier.heightIn(30.dp))
                RegularTextField(textState = state.name, labelValue = stringResource(R.string.textField1RegisterScreen1),
                    error = state.nameError,
                    onTextSelected = {
                        viewModel.onEvent((RegisterUIEvent.RegisterNameChanged(it)))
                    }
                )
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally )
                ) {
                    GenderButtonGroup(selectedGender = state.gender,
                        onGenderSelected = { gender ->
                            viewModel.onEvent(RegisterUIEvent.RegisterGenderChanged(gender))
                        })

                }
                if (state.genderError != null) {
                    Text(
                        text = state.genderError,
                        color = colorScheme.error,

                    )
                }
                Spacer(modifier = Modifier.heightIn(24.dp))

                BirthDateField(
                    label = stringResource(R.string.textField2RegisterScreen1),
                    value = state.birthDate,
                    error = state.birthDateError,
                    onDateSelected = { date ->
                        viewModel.onEvent(RegisterUIEvent.RegisterBirthDateChanged(date))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally )
                )
                    {
                        BackButton(onButtonClicked = {navController.popBackStack()})
                        NFButton(text = stringResource(R.string.buttonRegisterScreen1), onButtonClicked = {viewModel.onEvent(RegisterUIEvent.NextClickedStep1)
                            val newState = viewModel.registerUIState
                            if (newState.isStep1Valid) {
                                navController.navigate("registrationScreen2")
                            }}
                        )
                    }
                Spacer(Modifier.weight(0.15f))
            }
        }
    }
}