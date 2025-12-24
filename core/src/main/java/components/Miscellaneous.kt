package components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.R
import java.time.Instant
import java.time.ZoneId

@Composable
fun DividerTextComponent(){
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically){
        HorizontalDivider(modifier = Modifier.fillMaxWidth()
            .weight(1f),
            color = colorScheme.onSurface,
            thickness = 1.dp
        )

        Text(modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.divider),
            fontSize = 18.sp,
            color = colorScheme.onSurface
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth()
            .weight(1f),
            color = colorScheme.onSurface,
            thickness = 1.dp
        )
    }
}

@Composable
fun StepIndicators(
    currentStep: Int, // 0-based ou 1-based, já explico
) {
    val totalSteps = 5
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep   // se currentStep for 0-based
            Box(
                modifier = Modifier
                    .weight(5F, true)
                    .height(20.dp)
                    .clip(RoundedCornerShape(30)) // “cilindro”
                    .background(
                        if (index <= currentStep) colorScheme.primary
                        else colorScheme.outlineVariant)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDateField(
    label: String,
    value: String?,               // "YYYY-MM-DD" ou null
    error: String? = null,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var openDialog by remember { mutableStateOf(false) }

    // Campo “só leitura” que abre o date picker
    OutlinedTextField(
        value = value ?: "",
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurface,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            unfocusedBorderColor = colorScheme.onSurface,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurface,
            focusedTrailingIconColor = colorScheme.primary

        ),
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        modifier = modifier,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = colorScheme.error)
            }
        },
        trailingIcon = {
            IconButton(onClick = { openDialog = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            }
        },
        visualTransformation = VisualTransformation.None,
        enabled = true,

    )

    if (openDialog) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            // converte para "YYYY-MM-DD"
                            val formatted = localDate.toString()
                            onDateSelected(formatted)
                        }
                        openDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

enum class Gender { MALE, FEMALE }