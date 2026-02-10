package com.example.nutriflex2.home.account

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import local.UserLocalRepository
import remote.UserRepository
import javax.inject.Inject

data class AccountUiState(
    val name: String = "",
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val height: String = "",
    val dateOfBirth: String = "",
    val activityLevel: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    var uiState = mutableStateOf(AccountUiState())
        private set

    private var userId: Int? = null

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(isLoading = true, error = null)

            val local = userLocalRepository.getUserLocal().firstOrNull()
            if (local == null) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "No local user found"
                )
                return@launch
            }
            userId = local.userId

            try {
                val result = userRepository.getUser(local.userId)
                result.onSuccess { user ->
                    uiState.value = uiState.value.copy(
                        name = user.nome,
                        email = user.email,
                        height = user.altura.toString(),
                        activityLevel = user.nivel_atividade.toString(),
                        dateOfBirth = normalizeDate(user.data_nascenca),
                        isLoading = false
                    )
                }.onFailure {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load account data"
                    )
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load account data"
                )
            }
        }
    }

    private fun normalizeDate(raw: String): String =
        if (raw.length >= 10) raw.take(10) else raw

    fun onNameChange(value: String) {
        uiState.value = uiState.value.copy(name = value)
    }

    fun onEmailChange(value: String) {
        uiState.value = uiState.value.copy(email = value)
    }

    fun onCurrentPasswordChange(value: String) {
        uiState.value = uiState.value.copy(currentPassword = value)
    }

    fun onNewPasswordChange(value: String) {
        uiState.value = uiState.value.copy(newPassword = value)
    }

    fun onHeightChange(value: String) {
        uiState.value = uiState.value.copy(height = value)
    }

    fun onActivityLevelChange(value: String) {
        uiState.value = uiState.value.copy(activityLevel = value)
    }

    fun onDateOfBirthChange(value: String) {
        uiState.value = uiState.value.copy(dateOfBirth = value)
    }

    fun saveChanges() {
        val id = userId ?: return

        if (uiState.value.currentPassword.isBlank()) {
            uiState.value = uiState.value.copy(
                error = "Current password is required to update your account"
            )
            return
        }

        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )
            try {
                val result = userRepository.updateUser(
                    id = id,
                    currentPassword = uiState.value.currentPassword,
                    nome = uiState.value.name,
                    altura = uiState.value.height.toFloatOrNull(),
                    genero = null,
                    dataNascenca = uiState.value.dateOfBirth,
                    nivel_atividade = uiState.value.activityLevel.toInt(),
                    newPassword = uiState.value.newPassword.takeIf { it.isNotBlank() }
                )

                result.onSuccess {
                    val local = userLocalRepository.getUserLocal().firstOrNull()
                    if (local != null) {
                        userLocalRepository.saveUserLocal(
                            userId = local.userId,
                            token = local.token,
                            bmi = local.bmi,
                            currentWeight = local.currentWeight,
                            initialWeight = local.initialWeight,
                            goalWeight = local.goalWeight,
                            heightCm = uiState.value.height.toIntOrNull() ?: local.heightCm,
                            dailyCalories = local.dailyCalories,
                            gender = local.gender,
                            activityLevel = uiState.value.activityLevel.toIntOrNull() ?: local.activityLevel,
                            birthDate = normalizeDate(uiState.value.dateOfBirth),
                        )
                    }

                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        successMessage = "Account updated successfully"
                    )
                }.onFailure {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update account"
                    )
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update account"
                )
            }
        }
    }

    fun deleteAllWeights(onDone: () -> Unit) {
        val id = userId ?: return
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )
            try {
                val result = userRepository.deleteAllProgress(id)
                result.onSuccess {
                    // apaga histórico local
                    userLocalRepository.clearWeightHistoryForUser(id)

                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        successMessage = "All weight records deleted"
                    )
                    onDone()
                }.onFailure {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete weight records"
                    )
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete weight records"
                )
            }
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        val id = userId ?: return
        viewModelScope.launch {
            uiState.value = uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )
            try {
                val result = userRepository.deleteUser(id)
                result.onSuccess {
                    // limpa todos os dados locais
                    userLocalRepository.clear()
                    userLocalRepository.clearWeightHistoryForUser(id)

                    uiState.value = uiState.value.copy(isLoading = false)
                    onDone()
                }.onFailure {
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete account"
                    )
                }
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete account"
                )
            }
        }
    }

    fun clearMessages() {
        uiState.value = uiState.value.copy(error = null, successMessage = null)
    }
}
