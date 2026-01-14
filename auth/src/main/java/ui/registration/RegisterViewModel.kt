package ui.registration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import components.Gender
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.RegisterRequest
import data.repository.AuthRepository
import kotlinx.coroutines.launch
import local.UserLocalRepository
import utils.calculateDailyCaloriesForWeightChange
import utils.calculateMaintenanceCalories
import utils.targetKgPerWeek
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    var registerUIState by mutableStateOf(RegisterUIState())
        private set

    fun onEvent(event: RegisterUIEvent) {
        when (event) {
            is RegisterUIEvent.RegisterNameChanged -> {
                registerUIState = registerUIState.copy(name = event.name)
            }
            is RegisterUIEvent.RegisterGenderChanged -> {
                registerUIState = registerUIState.copy(
                    gender = event.gender,
                    genderError = null
                )
            }
            is RegisterUIEvent.RegisterBirthDateChanged -> {
                registerUIState = registerUIState.copy(
                    birthDate = event.date,
                    birthDateError = null
                )
            }
            is RegisterUIEvent.RegisterGoalChanged -> {
                registerUIState = registerUIState.copy(
                    goal = event.goal,
                    goalError = null
                )
            }
            is RegisterUIEvent.RegisterActivityChanged -> {
                registerUIState = registerUIState.copy(
                    activityLevel = event.activityLevel,
                    activityLevelError = null
                )
            }
            is RegisterUIEvent.RegisterDifficultyChanged -> {
                registerUIState = registerUIState.copy(
                    difficulty = event.difficulty,
                    difficultyError = null
                )
            }
            is RegisterUIEvent.RegisterHeightChanged -> {
                registerUIState = registerUIState.copy(
                    height = event.height,
                    heightError = null
                )
            }
            is RegisterUIEvent.RegisterWeightChanged -> {
                registerUIState = registerUIState.copy(
                    weight = event.weight,
                    weightError = null
                )
            }
            is RegisterUIEvent.RegisterWeightGoalChanged -> {
                registerUIState = registerUIState.copy(
                    weightGoal = event.weightGoal,
                    weightGoalError = null
                )
            }
            is RegisterUIEvent.ToggleAutoWeightGoal -> {
                registerUIState = if (event.enabled) {
                    registerUIState.copy(
                        autoWeightGoal = true,
                        weightGoal = 0f,
                        weightGoalError = null
                    )
                } else {
                    registerUIState.copy(autoWeightGoal = false)
                }
            }
            is RegisterUIEvent.RegisterEmailChanged -> {
                registerUIState = registerUIState.copy(email = event.email)
            }
            is RegisterUIEvent.RegisterPasswordChanged -> {
                registerUIState = registerUIState.copy(password = event.password)
            }

            RegisterUIEvent.NextClickedStep1 -> validateStep1()
            RegisterUIEvent.NextClickedStep2 -> validateStep2()
            RegisterUIEvent.NextClickedStep3 -> validateStep3()
            RegisterUIEvent.NextClickedStep4 -> validateStep4()
            RegisterUIEvent.NextClickedStep5 -> validateStep4()
            RegisterUIEvent.NextClickedStep6 -> validateStep6()
        }
    }

    private fun validateStep1() {
        var nameError: String? = null
        var genderError: String? = null
        var birthDateError: String? = null

        if (registerUIState.name.isBlank()) nameError = "Name is mandatory"
        if (registerUIState.gender == null) genderError = "Gender is mandatory"
        if (registerUIState.birthDate.isBlank()) birthDateError = "Birth Date is mandatory"

        val hasError = nameError != null || genderError != null || birthDateError != null

        registerUIState = registerUIState.copy(
            nameError = nameError,
            genderError = genderError,
            birthDateError = birthDateError,
            isStep1Valid = !hasError
        )
    }

    private fun validateStep2() {
        var goalError: String? = null
        if (registerUIState.goal == null) {
            goalError = "Select an option"
        }
        registerUIState = registerUIState.copy(goalError = goalError)
    }

    private fun validateStep3() {
        var difficultyError: String? = null
        if (registerUIState.difficulty == null) {
            difficultyError = "Select an option"
        }
        registerUIState = registerUIState.copy(difficultyError = difficultyError)
    }

    private fun validateStep4() {
        var heightError: String? = null
        var weightError: String? = null
        var weightGoalError: String? = null

        if (registerUIState.height == null) heightError = "Height is mandatory"
        if (registerUIState.weight == null) weightError = "Weight is mandatory"
        if (registerUIState.weightGoal == null) weightGoalError = "Weight Goal is mandatory"

        val hasError = heightError != null || weightError != null || weightGoalError != null

        registerUIState = registerUIState.copy(
            heightError = heightError,
            weightError = weightError,
            weightGoalError = weightGoalError,
            isStep4Valid = !hasError
        )
    }

    private fun validateStep6() {
        var activityLevelError: String? = null
        if (registerUIState.activityLevel == null) {
            activityLevelError = "Select an option"
        }
        registerUIState = registerUIState.copy(activityLevelError = activityLevelError)
    }

    private fun calculateBmi(weightKg: Float, heightCm: Int): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun computeAge(birthDate: String): Int {
        return try {
            val parts = birthDate.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val dob = java.time.LocalDate.of(year, month, day)
            val today = java.time.LocalDate.now()
            java.time.Period.between(dob, today).years
        } catch (e: Exception) {
            30
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun finalizeAndRegister(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            Log.d("Register", "finalizeAndRegister called")
            val state = registerUIState

            val activityLevel = state.activityLevel ?: return@launch onResult(
                Result.failure(Exception("Missing activity level"))
            )
            val heightCm = state.height ?: return@launch onResult(
                Result.failure(Exception("Missing height"))
            )
            val weightKg = state.weight ?: return@launch onResult(
                Result.failure(Exception("Missing weight"))
            )
            val gender = state.gender ?: return@launch onResult(
                Result.failure(Exception("Missing gender"))
            )
            val age = computeAge(state.birthDate)

            val bmi = calculateBmi(weightKg, heightCm)

            // Harris-Benedict + nível de atividade 0–4
            val generoString = when (gender) {
                Gender.MALE -> "M"
                Gender.FEMALE -> "F"
            }
            val isMale = generoString == "M"

            val maintenance = calculateMaintenanceCalories(
                isMale = isMale,
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                activityLevel = activityLevel
            )

            // peso objetivo
            val finalWeightGoal = if (state.autoWeightGoal) {
                // aqui podes decidir se queres usar um target de IMC “normal”
                // vindo de uma função core, por enquanto usa o que user meter
                state.weightGoal ?: weightKg
            } else {
                state.weightGoal ?: weightKg
            }

            val deltaKg = finalWeightGoal - weightKg
            val kgPerWeek = targetKgPerWeek(deltaKg)
            val kcalPerKg = 7700f
            val weeklyKcal = kgPerWeek * kcalPerKg
            val dailyKcalChange = weeklyKcal / 7f

            // opção 1: usar função genérica de core para daily target
            val caloriesDaily = calculateDailyCaloriesForWeightChange(
                maintenanceCalories = maintenance,
                currentWeightKg = weightKg,
                goalWeightKg = finalWeightGoal
            )

            val roundedCalories = caloriesDaily
            val roundedWeightGoal =
                String.format("%.1f", finalWeightGoal).replace(',', '.').toFloat()

            registerUIState = state.copy(weightGoal = roundedWeightGoal)

            val request = RegisterRequest(
                nome = state.name,
                email = state.email,
                password = state.password,
                altura = heightCm,
                data_nascenca = state.birthDate,
                genero = generoString,
                peso_atual = weightKg,
                peso_inicial = weightKg,
                peso_meta = roundedWeightGoal,
                calorias_diarias = roundedCalories,
                dificuldades_anteriores = state.difficulty ?: 0,
                objetivo = state.goal ?: 0,
                nivel_atividade = activityLevel
            )

            val result = authRepository.register(request)

            result
                .onSuccess { registerResponse ->
                    val userResult = authRepository.getUserByEmail(state.email)
                    userResult
                        .onSuccess { userDto ->
                            val progress = userDto.progress
                            val dailyCalories =
                                progress?.calorias_diarias ?: roundedCalories
                            val goalWeight =
                                progress?.peso_meta ?: roundedWeightGoal

                            userLocalRepository.saveUserLocal(
                                userId = userDto.id,
                                token = registerResponse.token,
                                bmi = bmi,
                                currentWeight = weightKg,
                                goalWeight = goalWeight,
                                heightCm = heightCm,
                                dailyCalories = dailyCalories,
                                gender = generoString,
                                birthDate = state.birthDate,
                                activityLevel = state.activityLevel
                            )
                        }
                        .onFailure { e ->
                            Log.e("Register", "Failed to fetch user by email", e)
                        }

                    registerUIState = registerUIState.copy(registerError = null)
                    onResult(Result.success(Unit))
                }
                .onFailure { e ->
                    registerUIState = registerUIState.copy(
                        registerError = e.message ?: "Registration failed"
                    )
                    onResult(Result.failure(e))
                }
        }
    }
}
