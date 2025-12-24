package components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.components.R
import theme.AppShapes

@Composable
fun IconTextField(labelValue: String, imageVector: ImageVector, error: String? = null,
                  onTextSelected: (String) -> Unit){

    val textValue = remember{
        mutableStateOf("")
    }


    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().clip(AppShapes.small),
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurface,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            unfocusedBorderColor = colorScheme.onSurface,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurface

        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        value = textValue.value,
        onValueChange = {
            textValue.value = it
            onTextSelected(it)
        },
        singleLine = true,
        maxLines = 1,
        leadingIcon = {
            Icon(imageVector = imageVector,
                contentDescription = ""
            )
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = colorScheme.error)
            }
        },
    )
}

@Composable
fun RegularTextField(supportText: String? = null,textIcon: String? = null,textState: String,labelValue: String, error: String? = null,
                  onTextSelected: (String) -> Unit){


    val textValue = remember{
        mutableStateOf(textState)
    }


    OutlinedTextField(modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.small), label = { Text(text = labelValue) }, colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurface,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            unfocusedBorderColor = colorScheme.onSurface,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurface

        ), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), value = textValue.value, onValueChange = {
            textValue.value = it
            onTextSelected(it)
        }, singleLine = true, maxLines = 1, trailingIcon = {
            if (textIcon != null) {
                Text(textIcon)
            }
        }, isError = error != null, supportingText = {
            if (error != null) {
                Text(text = error, color = colorScheme.error)
            }else if (supportText != null){
                Text(text = supportText, color = Color.Gray)
            }
        },)
}

@Composable
fun PasswordTextField(labelValue: String, imageVector: ImageVector,error: String? = null,
                      onTextSelected: (String) -> Unit){

    val localFocusManager = LocalFocusManager.current
    val password = remember{
        mutableStateOf("")
    }

    val passwordVisible = remember{
        mutableStateOf(false)
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().clip(AppShapes.small),
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurface,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            unfocusedBorderColor = colorScheme.onSurface,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurface,
            focusedTrailingIconColor = colorScheme.primary

        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions {
            localFocusManager.clearFocus()
        },
        value = password.value,
        onValueChange = {
            password.value = it
            onTextSelected(it)
        },
        singleLine = true,
        maxLines = 1,
        leadingIcon = {
            Icon(imageVector = imageVector,
                contentDescription = ""
            )
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = colorScheme.error)
            }
        },
        trailingIcon = {
            val iconImage = if (passwordVisible.value){
                Icons.Filled.Visibility
            } else{
                Icons.Filled.VisibilityOff
            }

            var description = if (passwordVisible.value){
                stringResource(id = R.string.hide_password)
            } else{
                stringResource(id = R.string.show_password)
            }

            IconButton(onClick = {passwordVisible.value = !passwordVisible.value}) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
        },

        visualTransformation = if(passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()
    )
}


@Composable
fun NumberTextField(modifier: Modifier = Modifier,
    enabled: Boolean = true,
    allowDecimal: Boolean? = true,
    supportText: String? = null,
    textIcon: String? = null,
    textState: String,
    labelValue: String,
    error: String? = null,
    onTextSelected: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.small),
        label = { Text(text = labelValue) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurface,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            unfocusedBorderColor = colorScheme.onSurface,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = if(allowDecimal == true){
                KeyboardType.Decimal
            }else{
                KeyboardType.Number
            },
            imeAction = ImeAction.Next
        ),
        value = textState,
        onValueChange = { newValue ->
            // Só permitir dígitos e no máximo um ponto
            val filtered = newValue
                .filter { it.isDigit() || it == '.' }
                .let { str ->
                    val firstDot = str.indexOf('.')
                    if (firstDot == -1) str
                    else str.take(firstDot + 1) +
                            str.substring(firstDot + 1).replace(".", "")
                }

            onTextSelected(filtered)
        },
        singleLine = true,
        maxLines = 1,
        trailingIcon = {
            if (textIcon != null) {
                Text(textIcon)
            }
        },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = colorScheme.error)
            } else if (supportText != null) {
                Text(text = supportText, color = Color.Gray)
            }
        },
        enabled = enabled
    )
}
