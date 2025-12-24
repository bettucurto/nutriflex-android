@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Female
import androidx.compose.material.icons.outlined.Male
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
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
        ),
        contentPadding = PaddingValues(0.dp)

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
fun BackButton(onButtonClicked: () -> Unit){
    Button(
        onClick = {
            onButtonClicked.invoke()
        },
        modifier = Modifier
            .wrapContentWidth()
            .heightIn(58.dp),
    ){
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Back"
        )
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
        ),
        contentPadding = PaddingValues(0.dp)
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

@Composable
fun GenderButtonGroup(selectedGender: Gender?,
                      onGenderSelected: (Gender) -> Unit,
                      modifier: Modifier = Modifier){
    ButtonGroup(
        overflowIndicator = {
            Text("lol")
        },
        modifier = Modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ){
        // Male
        val maleChecked = selectedGender == Gender.MALE
        toggleableItem(
            checked = maleChecked,
            label = "Male",
            weight = 1f,
            onCheckedChange = { onGenderSelected(Gender.MALE) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Male,
                    contentDescription = null
                )
            }
        )

        // Female
        val femaleChecked = selectedGender == Gender.FEMALE
        toggleableItem(
            checked = femaleChecked,
            label = "Female",
            weight = 1f,
            onCheckedChange = { onGenderSelected(Gender.FEMALE) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Female,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
fun TextButtonGroup(
    options: List<String>,
    selectedIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index

            OutlinedButton(
                onClick = { onOptionSelected(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(66.dp),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(2.dp, colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) colorScheme.primary else Color.Transparent,
                    contentColor = colorScheme.onSurface
                )
            ) {
                Text(
                    text = text,
                    color = if (isSelected) colorScheme.surface else colorScheme.onSurface,
                    fontSize = 17.sp
                )
            }
        }
    }
}

