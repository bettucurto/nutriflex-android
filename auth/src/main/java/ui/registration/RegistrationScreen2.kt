package ui.registration

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
import components.LeftHeadingTextComponent
import components.LeftTitleText
import components.NFButton
import components.StepIndicators
import components.TextButtonGroup
import theme.AppTheme


@Composable
fun RegistrationScreen2(navController: NavController, viewModel: RegisterViewModel = hiltViewModel()){
    AppTheme() {
        val focusManager = LocalFocusManager.current
        val state = viewModel.registerUIState
        val goalOptions = listOf(
            "Reach a specific weight",
            "Feel and become healthier",
            "I dont feel a need to change weight"
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
                LeftTitleText(stringResource(R.string.titleRegistration2))
                Spacer(modifier = Modifier.heightIn(20.dp))
                StepIndicators(2)
                LeftHeadingTextComponent(stringResource(R.string.SubTitleRegistration2))
                Spacer(Modifier.weight(0.3f))


                TextButtonGroup(
                    options = goalOptions,
                    selectedIndex = state.goal,
                    onOptionSelected = { index ->
                        viewModel.onEvent(RegisterUIEvent.RegisterGoalChanged(index))
                    }
                )
                if (state.goalError != null) {
                    Text(
                        text = state.goalError,
                        color = colorScheme.error,
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
                    NFButton(text = stringResource(R.string.buttonRegisterScreen1), onButtonClicked = {viewModel.onEvent(RegisterUIEvent.NextClickedStep2)
                        val newState = viewModel.registerUIState
                        if (newState.goalError == null) {
                            navController.navigate("registrationScreen3")
                        }}
                    )
                }
                Spacer(Modifier.weight(0.1f))
            }
        }
    }
}