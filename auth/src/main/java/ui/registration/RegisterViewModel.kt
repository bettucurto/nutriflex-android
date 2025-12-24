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
                        weightGoal = 0f,          // app decide depois o valor real
                        weightGoalError = null
                    )
                } else {
                    registerUIState.copy(autoWeightGoal = false)
                }
            }
            is RegisterUIEvent.RegisterEmailChanged -> {
                registerUIState = registerUIState.copy(email= event.email)
            }
            is RegisterUIEvent.RegisterPasswordChanged -> {
                registerUIState = registerUIState.copy(password = event.password)
            }

            RegisterUIEvent.NextClickedStep1 -> {
                validateStep1()
            }
            RegisterUIEvent.NextClickedStep2 -> {
                validateStep2()
            }
            RegisterUIEvent.NextClickedStep3 -> {
                validateStep3()
            }
            RegisterUIEvent.NextClickedStep4 -> {
                validateStep4()
            }
            RegisterUIEvent.NextClickedStep5 -> {
                validateStep4()
            }
        }
    }
    private fun validateStep1() {
        var nameError: String? = null
        var genderError: String? = null
        var birthDateError: String? = null

        if (registerUIState.name.isBlank()) {
            nameError = "Name is mandatory"
        }
        if (registerUIState.gender == null) {
            genderError = "Gender is mandatory"
        }
        if (registerUIState.birthDate.isBlank()) {
            birthDateError = "Birth Date is mandatory"
        }

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

        registerUIState = registerUIState.copy(
            goalError = goalError
        )
    }
    private fun validateStep3() {
        var difficultyError: String? = null

        if (registerUIState.difficulty == null) {
            difficultyError = "Select an option"
        }

        registerUIState = registerUIState.copy(
            difficultyError = difficultyError
        )
    }
    private fun validateStep4() {
        var heightError: String? = null
        var weightError: String? = null
        var weightGoalError: String? = null

        if (registerUIState.height == null) {
            heightError = "Height is mandatory"
        }
        if (registerUIState.weight == null) {
            weightError = "Weight is mandatory"
        }
        if (registerUIState.weightGoal == null) {
            weightGoalError = "Weight Goal is mandatory"
        }

        val hasError = heightError != null || weightError != null || weightGoalError != null

        registerUIState = registerUIState.copy(
            heightError = heightError,
            weightError = weightError,
            weightGoalError = weightGoalError,
            isStep4Valid = !hasError
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun finalizeAndRegister(onResult: (Result<Unit>) -> Unit) {
        Log.d("Register", "finalizeAndRegister called")
        val state = registerUIState
        Log.d("Register", "state at finalize: height=${state.height}, weight=${state.weight}, gender=${state.gender}")

        val heightCm = state.height ?: run {
            Log.e("Register", "Missing height")
            return onResult(Result.failure(Exception("Missing height")))
        }
        val weightKg = state.weight ?: run {
            Log.e("Register", "Missing weight")
            return onResult(Result.failure(Exception("Missing weight")))
        }
        val gender = state.gender ?: run {
            Log.e("Register", "Missing gender")
            return onResult(Result.failure(Exception("Missing gender")))
        }
        val age = computeAge(state.birthDate)

        val bmi = calculateBmi(weightKg, heightCm)
        val baseGet = calculateGet(gender, weightKg, heightCm, age)

        val difficulty = state.difficulty // 0..3

        val (finalWeightGoal, caloriesDaily) = if (state.autoWeightGoal) {
            val targetWeight = normalBmiWeightForHeight(heightCm)
            val adjPercent = bmiAdjustmentPercent(bmi)
            val calories = baseGet * (1f + adjPercent)
            targetWeight to calories
        } else {
            val weightGoal = state.weightGoal ?: weightKg
            val basePercent = bmiAdjustmentPercent(bmi)
            val difficultyFactor = when (difficulty) {
                0 -> 0.5f
                1 -> 1f
                2, 3 -> 1.2f
                else -> 1f
            }
            val adjPercent = basePercent * difficultyFactor
            val calories = baseGet * (1f + adjPercent)
            weightGoal to calories
        }

        val roundedCalories = caloriesDaily.toInt()
        val roundedWeightGoal = String.format("%.1f", finalWeightGoal).replace(',', '.').toFloat()

        registerUIState = state.copy(
            weightGoal = roundedWeightGoal
        )

        val generoString = when (gender) {
            Gender.MALE -> "M"
            Gender.FEMALE -> "F"
        }

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
            objetivo = state.goal ?: 0
        )

        val result = authRepository.register(request)
        Log.d("Register", "register result=$result")
        result
            .onSuccess { registerResponse ->
                // 1) ir buscar os dados completos do utilizador por email
                viewModelScope.launch {
                    val userResult = authRepository.getUserByEmail(state.email)
                    userResult
                        .onSuccess { userDto ->
                            val progress = userDto.progress
                            val bmi = calculateBmi(weightKg, heightCm)
                            val dailyCalories = progress?.calorias_diarias ?: roundedCalories
                            val goalWeight = progress?.peso_meta ?: roundedWeightGoal

                            userLocalRepository.saveUserLocal(
                                userId = userDto.id,
                                token = registerResponse.token,
                                bmi = bmi,
                                currentWeight = weightKg,
                                goalWeight = goalWeight,
                                dailyCalories = dailyCalories
                            )
                        }
                        .onFailure { e ->
                            Log.e("Register", "Failed to fetch user by email", e)
                        }
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
            30 // fallback razoável se parsing falhar
        }
    }

    private fun calculateTmb(
        gender: Gender,
        weightKg: Float,
        heightCm: Int,
        age: Int
    ): Float {
        return if (gender == Gender.MALE) {
            10f * weightKg + 6.25f * heightCm - 5f * age + 5f
        } else {
            10f * weightKg + 6.25f * heightCm - 5f * age - 161f
        }
    }

    private fun calculateGet(
        gender: Gender,
        weightKg: Float,
        heightCm: Int,
        age: Int
    ): Float {
        val tmb = calculateTmb(gender, weightKg, heightCm, age)
        val activityFactor = 1.375f // levemente ativo
        return tmb * activityFactor
    }

    private fun bmiAdjustmentPercent(bmi: Float): Float {
        return when {
            bmi < 18.5f -> 0.05f    // magreza: +5%
            bmi < 25f -> 0f         // normal
            bmi < 30f -> -0.05f     // sobrepeso: -5%
            bmi < 40f -> -0.10f     // obesidade: -10%
            else -> -0.175f         // obesidade grave: -17.5% (meio termo 15–20)
        }
    }

    // peso alvo com IMC médio de 22 (intervalo 18.5–24.9)
    private fun normalBmiWeightForHeight(heightCm: Int, targetBmi: Float = 22f): Float {
        val heightM = heightCm / 100f
        return targetBmi * heightM * heightM
    }


}