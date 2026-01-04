package com.example.nutriflex2.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import components.WeightHistoryPoint
import components.WeightRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject


data class HomeUiState(
    val dailyCalories: Int = 0,
    val eatenCaloriesToday: Int = 0,
    val nextWorkoutId: Int? = 1,
    val nextWorkoutName: String = "Superior",
    val nextWorkoutExercises: Int = 6,
    val currentWeight: Float = 75f,
    val goalWeight: Float = 70f,
    val bmi: Float = 0f,
    val weightHistory: List<WeightHistoryPoint> = emptyList()
) {
    val bmiCategory: String
        get() = when {
            bmi <= 0f -> "Unavailable"
            bmi < 18.5f -> "Underweight"
            bmi < 25f -> "Healthy"
            bmi < 30f -> "Overweight"
            bmi < 35f -> "Obese"
            else -> "Severe Obesity"
        }

    val bmiMin = 10f
    val bmiMax = 40f

    val bmiPosition: Float
        get() = if (bmi <= 0f) 0f
        else ((bmi.coerceIn(bmiMin, bmiMax) - bmiMin) / (bmiMax - bmiMin))

    // semanas até ao objetivo usando mesma regra de kg/semana
    val weeklyProgressWeeks: Int?
        get() {
            val deltaKg = goalWeight - currentWeight
            if (deltaKg == 0f) return 0
            if (dailyCalories <= 0) return null

            val kgPerWeek = targetKgPerWeek(deltaKg)
            if (kgPerWeek <= 0f) return null

            val weeks = kotlin.math.abs(deltaKg) / kgPerWeek
            return weeks.toInt().coerceAtLeast(1)
        }

    val remainingCalories: Int
        get() = (dailyCalories - eatenCaloriesToday).coerceAtLeast(0)

    val progress: Float
        get() = if (dailyCalories <= 0) 0f
        else (eatenCaloriesToday.toFloat() / dailyCalories.toFloat()).coerceIn(0f, 1f)
}

// função top-level para poder ser usada no getter
private fun targetKgPerWeek(deltaKg: Float): Float {
    val absDelta = kotlin.math.abs(deltaKg)
    return when {
        absDelta < 7f   -> 0.33f
        absDelta < 15f  -> 0.5f
        else            -> 1.0f
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal()

            _uiState.value = HomeUiState(
                dailyCalories = user?.dailyCalories ?: 0,
                eatenCaloriesToday = user?.eatenCaloriesToday ?: 0,
                nextWorkoutId = 1,
                nextWorkoutName = "Superior",
                nextWorkoutExercises = 6,
                currentWeight = user?.currentWeight ?: 75f,
                goalWeight = user?.goalWeight ?: 70f,
                bmi = user?.bmi ?: 0f
            )

            if (user != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // se ainda não tiver histórico, semeia mock
                val existing = userLocalRepository.getAllWeightHistory(user.userId)
                if (existing.isEmpty()) {
                    userLocalRepository.seedMockWeightHistory(user.userId)
                }
                // agora carrega normalmente do Room
                loadWeightHistory(WeightRange.ONE_MONTH)
            }

            //APAGAR HISTORICO DE PESO DO ROOM E ADICIONAR MOCK
//            if (user != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                userLocalRepository.clearWeightHistoryForUser(user.userId)
//                userLocalRepository.seedMockWeightHistory(user.userId)
//                loadWeightHistory(WeightRange.ONE_MONTH)
//            }
        }
    }


    fun onChangeCurrentWeight(newWeight: Float, currentRange: WeightRange) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal() ?: return@launch
            val heightCm = user.heightCm
            val heightM = heightCm / 100f
            val newBmi = if (heightM > 0f) newWeight / (heightM * heightM) else 0f

            val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.LocalDate.now().toString()
            } else {
                ""
            }

            userLocalRepository.updateCurrentWeight(newWeight)
            userLocalRepository.updateBmi(newBmi)
            userLocalRepository.addWeightEntry(
                userId = user.userId,
                weight = newWeight,
                date = today
            )

            _uiState.value = _uiState.value.copy(
                currentWeight = newWeight,
                bmi = newBmi
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recalculateDailyCaloriesAndUpdateState(
                    currentWeight = newWeight,
                    goalWeight = _uiState.value.goalWeight
                )
                // recarrega histórico para o range atual -> gráfico atualiza logo
                loadWeightHistory(currentRange)
            }
        }
    }

    fun onChangeGoalWeight(newWeight: Float) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal() ?: return@launch

            userLocalRepository.updateGoalWeight(newWeight)
            _uiState.value = _uiState.value.copy(goalWeight = newWeight)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recalculateDailyCaloriesAndUpdateState(
                    currentWeight = _uiState.value.currentWeight,
                    goalWeight = newWeight
                )
            }
        }
    }

    private fun calculateTmb(
        gender: String,
        weightKg: Float,
        heightCm: Int,
        age: Int
    ): Float {
        val isMale = gender == "M"
        return if (isMale) {
            10f * weightKg + 6.25f * heightCm - 5f * age + 5f
        } else {
            10f * weightKg + 6.25f * heightCm - 5f * age - 161f
        }
    }

    private fun calculateGet(
        gender: String,
        weightKg: Float,
        heightCm: Int,
        age: Int
    ): Float {
        val tmb = calculateTmb(gender, weightKg, heightCm, age)
        val activityFactor = 1.375f
        return tmb * activityFactor
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun recalculateDailyCaloriesAndUpdateState(
        currentWeight: Float,
        goalWeight: Float
    ) {
        val user = userLocalRepository.getUserLocal() ?: return

        val heightCm = user.heightCm
        val gender = user.gender
        val birthDate = user.birthDate

        val deltaKg = goalWeight - currentWeight
        if (deltaKg == 0f) return

        val age = try {
            val (y, m, d) = birthDate.split("-").map { it.toInt() }
            val dob = java.time.LocalDate.of(y, m, d)
            val today = java.time.LocalDate.now()
            java.time.Period.between(dob, today).years
        } catch (e: Exception) {
            30
        }

        val maintenance = calculateGet(gender, currentWeight, heightCm, age)

        val kgPerWeek = targetKgPerWeek(deltaKg)
        val kcalPerKg = 7700f
        val weeklyKcal = kgPerWeek * kcalPerKg
        val dailyKcalChange = weeklyKcal / 7f

        val newCalories = if (deltaKg < 0f) {
            (maintenance - dailyKcalChange).toInt()
        } else {
            (maintenance + dailyKcalChange).toInt()
        }

        userLocalRepository.updateDailyCaloriesValue(newCalories)

        _uiState.value = _uiState.value.copy(
            dailyCalories = newCalories
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadWeightHistory(range: WeightRange) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal() ?: return@launch
            val userId = user.userId
            val today = java.time.LocalDate.now()

            val list = if (range == WeightRange.ALL_TIME) {
                userLocalRepository.getAllWeightHistory(userId)
            } else {
                val from = when (range) {
                    WeightRange.TWO_WEEKS    -> today.minusDays(14)
                    WeightRange.ONE_MONTH    -> today.minusMonths(1)
                    WeightRange.THREE_MONTHS -> today.minusMonths(3)
                    WeightRange.ALL_TIME     -> today.minusYears(100)
                }.toString()

                userLocalRepository.getWeightHistoryFrom(userId, from)
            }

            _uiState.value = _uiState.value.copy(
                weightHistory = list.map { WeightHistoryPoint(it.date, it.weight) }
            )
        }
    }


}
