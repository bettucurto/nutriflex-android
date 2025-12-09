package components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NFButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onButtonClicked: () -> Unit
) {

    Button(
        onClick = {
            onButtonClicked.invoke()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(58.dp),
        colors = ButtonDefaults.buttonColors(
            Color.Transparent
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(58.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(colorScheme.secondary, colorScheme.primary)),
                shape = RoundedCornerShape(50.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun NFOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onButtonClicked: () -> Unit
) {

//    OutlinedButton(
//        onClick = onClick,
//        enabled = enabled,
//        modifier = modifier
//            .fillMaxWidth()
//            .widthIn(200.dp)
//            .heightIn(58.dp),
//        border = ButtonDefaults.outlinedButtonBorder(enabled)
//    ) {
//        Text(
//            text = text,
//            fontSize = 18.sp,
//            color = colorScheme.secondary
//        )
//    }

    Button(
        onClick = {
            onButtonClicked.invoke()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(58.dp),
        colors = ButtonDefaults.buttonColors(
            Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(58.dp)
                .background(
                    color = Color.Gray,
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
