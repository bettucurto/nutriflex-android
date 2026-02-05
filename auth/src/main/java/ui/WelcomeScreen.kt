package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.auth.R
import components.NFButton
import components.NFOutlinedButton
import components.TitleText
import theme.AppTheme


@Composable
fun WelcomeScreen(navController: NavController) {

    AppTheme() {
        Surface(
            modifier = Modifier.fillMaxSize()

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 19.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {


                TitleText(value = stringResource(id = R.string.titleWelcomeScreen))

                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(id = R.drawable.nutrilogo),
                    contentDescription = "Logo Nutriflex",
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 19.dp)
                ) {
                    NFButton(
                        text = stringResource(id = R.string.btn1WelcomeScreen),
                        onButtonClicked = {navController.navigate("registrationScreen1")}
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 19.dp)
                ) {
                    NFOutlinedButton(
                        text = stringResource(id = R.string.btn2WelcomeScreen),
                        onButtonClicked = { navController.navigate("loginScreen") }
                    )
                }
            }
        }
    }
}


