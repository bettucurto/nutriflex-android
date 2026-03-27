package com.example.nutriflex2.home.settings

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import local.UserLocalRepository
import remote.UserRepository
import utils.calculateDailyCaloriesForWeightChange
import utils.calculateMaintenanceCalories
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

    @RequiresApi(Build.VERSION_CODES.O)
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
                val newHeight = uiState.value.height.toFloatOrNull()
                val newActivityLevel = uiState.value.activityLevel.toIntOrNull() ?: 0
                val newBirthDate = normalizeDate(uiState.value.dateOfBirth)

                val result = userRepository.updateUser(
                    id = id,
                    currentPassword = uiState.value.currentPassword,
                    nome = uiState.value.name,
                    altura = newHeight,
                    genero = null,
                    dataNascenca = newBirthDate,
                    nivel_atividade = newActivityLevel,
                    newPassword = uiState.value.newPassword.takeIf { it.isNotBlank() }
                )

                result.onSuccess {
                    val local = userLocalRepository.getUserLocal().firstOrNull()
                    if (local != null) {
                        val finalHeight = newHeight?.toInt() ?: local.heightCm
                        val finalActivityLevel = newActivityLevel
                        val finalBirthDate = newBirthDate

                        // --- RECALCULAR BMI ---
                        val heightM = finalHeight / 100f
                        val newBmi = if (heightM > 0f) local.currentWeight / (heightM * heightM) else 0f

                        // --- RECALCULAR CALORIAS ---
                        val isMale = local.gender == "M"
                        val age = try {
                            val (y, m, d) = finalBirthDate.split("-").map { it.toInt() }
                            val dob = java.time.LocalDate.of(y, m, d)
                            val today = java.time.LocalDate.now()
                            java.time.Period.between(dob, today).years
                        } catch (e: Exception) {
                            30
                        }

                        val maintenance = calculateMaintenanceCalories(
                            isMale = isMale,
                            weightKg = local.currentWeight,
                            heightCm = finalHeight,
                            age = age,
                            activityLevel = finalActivityLevel
                        )

                        val newCalories = calculateDailyCaloriesForWeightChange(
                            maintenanceCalories = maintenance,
                            currentWeightKg = local.currentWeight,
                            goalWeightKg = local.goalWeight
                        )

                        // Atualizar local usando o novo método que não faz reset ao dia
                        userLocalRepository.updateAccountData(
                            userId = local.userId,
                            height = finalHeight,
                            activityLevel = finalActivityLevel,
                            birthDate = finalBirthDate,
                            dailyCalories = newCalories,
                            bmi = newBmi
                        )

                        // Sincronizar tmb o progresso no servidor com as novas calorias
                        try {
                            userRepository.createProgress(
                                idUser = local.userId,
                                pesoAtual = local.currentWeight,
                                pesoMeta = local.goalWeight,
                                caloriasDiarias = newCalories
                            )
                        } catch (e: Exception) {
                            Log.e("AccountViewModel", "Error creating progress sync", e)
                        }
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
