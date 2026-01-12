package com.example.nutriflex2.home.account

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import components.BirthDateField

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showDeleteWeightsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Toast para erros
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    // Toast para sucessos
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // (já não mostramos textos de erro/sucesso aqui)

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = { viewModel.onCurrentPasswordChange(it) },
                label = { Text("Current password (required)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.newPassword,
                onValueChange = { viewModel.onNewPasswordChange(it) },
                label = { Text("New password (optional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.height,
                onValueChange = { viewModel.onHeightChange(it) },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            BirthDateField(
                label = "Date of birth (YYYY-MM-DD)",
                value = state.dateOfBirth,
                error = null,
                onDateSelected = { date ->
                    viewModel.onDateOfBirthChange(date)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.saveChanges() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }

            Spacer(Modifier.height(32.dp))

            Divider()

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showDeleteWeightsDialog = true },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete all weight records")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDeleteAccountDialog = true },
                enabled = !state.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete account")
            }

            Spacer(Modifier.height(24.dp))

            TextButton(
                onClick = onLogout,
                enabled = !state.isLoading
            ) {
                Text("Log out")
            }
        }

        if (showDeleteWeightsDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteWeightsDialog = false },
                title = { Text("Delete all weight records") },
                text = { Text("Are you sure you want to delete all saved weights? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteWeightsDialog = false
                        viewModel.deleteAllWeights(onDone = {})
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteWeightsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = { Text("Delete account") },
                text = { Text("This will permanently delete your account and data. Proceed?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount(onDone = onLogout)
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
