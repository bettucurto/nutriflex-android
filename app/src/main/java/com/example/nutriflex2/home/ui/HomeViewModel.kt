package com.example.nutriflex2.home.ui

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import components.WeightHistoryPoint
import components.WeightRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import local.UserLocalRepository
import remote.UserRepository
import utils.calculateDailyCaloriesForWeightChange
import utils.calculateMaintenanceCalories
import utils.targetKgPerWeek
import javax.inject.Inject

data class HomeUiState(
    val dailyCalories: Int = 0,
    val eatenCaloriesToday: Int = 0,

    val dailyCarbsGrams: Int = 0,
    val dailyProteinGrams: Int = 0,
    val dailyFatGrams: Int = 0,
    val eatenProteinGrams: Int = 0,
    val eatenCarbsGrams: Int = 0,
    val eatenFatGrams: Int = 0,

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

    val remainingProtein: Int
        get() = (dailyProteinGrams - eatenProteinGrams).coerceAtLeast(0)

    val remainingCarbs: Int
        get() = (dailyCarbsGrams - eatenCarbsGrams).coerceAtLeast(0)

    val remainingFat: Int
        get() = (dailyFatGrams - eatenFatGrams).coerceAtLeast(0)

    val proteinProgress: Float
        get() = if (dailyProteinGrams == 0) 0f
        else (eatenProteinGrams.toFloat() / dailyProteinGrams.toFloat()).coerceIn(0f, 1f)

    val carbsProgress: Float
        get() = if (dailyCarbsGrams == 0) 0f
        else (eatenCarbsGrams.toFloat() / dailyCarbsGrams.toFloat()).coerceIn(0f, 1f)

    val fatProgress: Float
        get() = if (dailyFatGrams == 0) 0f
        else (eatenFatGrams.toFloat() / dailyFatGrams.toFloat()).coerceIn(0f, 1f)
}

// Eventos de UI para toasts, etc.
sealed class HomeUiEvent {
    data class ShowToast(val message: String) : HomeUiEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userLocalRepository: UserLocalRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Canal de eventos one-shot (toasts, navegação, etc.)
    private val _uiEvent = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal()
            onMealLogged()

            _uiState.value = HomeUiState(
                dailyCalories = user?.dailyCalories ?: 0,
                eatenCaloriesToday = user?.eatenCaloriesToday ?: 0,

                dailyCarbsGrams = user?.dailyCarbsGrams ?: 0,
                dailyProteinGrams = user?.dailyProteinGrams ?: 0,
                dailyFatGrams = user?.dailyFatGrams ?: 0,
                eatenProteinGrams = user?.eatenProteinToday ?: 0,
                eatenCarbsGrams = user?.eatenCarbsToday ?: 0,
                eatenFatGrams = user?.eatenFatToday ?: 0,

                nextWorkoutId = 1,
                nextWorkoutName = "Superior",
                nextWorkoutExercises = 6,
                currentWeight = user?.currentWeight ?: 75f,
                goalWeight = user?.goalWeight ?: 70f,
                bmi = user?.bmi ?: 0f
            )

            if (user != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existing = userLocalRepository.getAllWeightHistory(user.userId)
                if (existing.isEmpty()) {
                    userLocalRepository.seedMockWeightHistory(user.userId)
                }
                loadWeightHistory(WeightRange.ONE_MONTH)
            }
        }
    }

    fun onChangeCurrentWeight(
        newWeight: Float,
        currentRange: WeightRange,
    ) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal() ?: return@launch
            val heightCm = user.heightCm
            val heightM = heightCm / 100f
            val newBmi = if (heightM > 0f) newWeight / (heightM * heightM) else 0f

            val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.LocalDate.now().toString()
            } else { "" }

            // 1) Atualiza local
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
                loadWeightHistory(currentRange)
            }

            // 2) Enviar progresso
            val goalWeight = _uiState.value.goalWeight
            val dailyCals = _uiState.value.dailyCalories

            viewModelScope.launch {
                userRepository.createProgress(
                    idUser = user.userId,
                    pesoAtual = newWeight,
                    pesoMeta = goalWeight,
                    caloriasDiarias = dailyCals
                )
            }

            _uiEvent.send(HomeUiEvent.ShowToast("Current weight updated!"))
        }
    }

    fun onChangeGoalWeight(
        newWeight: Float,
    ) {
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

            // Enviar registo de progresso com novo objetivo
            val currentWeight = _uiState.value.currentWeight
            val dailyCals = _uiState.value.dailyCalories

            viewModelScope.launch {
                userRepository.createProgress(
                    idUser = user.userId,
                    pesoAtual = currentWeight,
                    pesoMeta = newWeight,
                    caloriasDiarias = dailyCals
                )
            }

            _uiEvent.send(HomeUiEvent.ShowToast("Goal weight updated!"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun recalculateDailyCaloriesAndUpdateState(
        currentWeight: Float,
        goalWeight: Float
    ) {
        val user = userLocalRepository.getUserLocal() ?: return

        val heightCm = user.heightCm
        val gender = user.gender           // "M" ou "F"
        val birthDate = user.birthDate
        val activityLevel = user.activityLevel ?: 0

        val isMale = gender == "M"

        val age = try {
            val (y, m, d) = birthDate.split("-").map { it.toInt() }
            val dob = java.time.LocalDate.of(y, m, d)
            val today = java.time.LocalDate.now()
            java.time.Period.between(dob, today).years
        } catch (e: Exception) {
            30
        }

        val maintenance = calculateMaintenanceCalories(
            isMale = isMale,
            weightKg = currentWeight,
            heightCm = heightCm,
            age = age,
            activityLevel = activityLevel
        )

        val newCalories = calculateDailyCaloriesForWeightChange(
            maintenanceCalories = maintenance,
            currentWeightKg = currentWeight,
            goalWeightKg = goalWeight
        )

        userLocalRepository.updateDailyCaloriesValue(newCalories)

        _uiState.value = _uiState.value.copy(
            dailyCalories = newCalories
        )
    }

    fun onMealLogged() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "onMealLogged called")
            _uiEvent.send(HomeUiEvent.ShowToast("Meal Logged Successfully!"))
            val user = userLocalRepository.getUserLocal() ?: return@launch
            _uiState.value = _uiState.value.copy(
                dailyCalories = user.dailyCalories,
                eatenCaloriesToday = user.eatenCaloriesToday,
                dailyCarbsGrams = user.dailyCarbsGrams,
                dailyProteinGrams = user.dailyProteinGrams,
                dailyFatGrams = user.dailyFatGrams,
                eatenProteinGrams = user.eatenProteinToday,
                eatenCarbsGrams = user.eatenCarbsToday,
                eatenFatGrams = user.eatenFatToday
            )
        }
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
