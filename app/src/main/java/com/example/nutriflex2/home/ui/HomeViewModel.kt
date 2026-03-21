package com.example.nutriflex2.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.treino.domain.models.Sessao
import com.example.treino.domain.repository.TreinoRepository
import components.WeightHistoryPoint
import components.WeightRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
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

    val nextWorkoutId: Int? = null,
    val nextWorkoutName: String = "",
    val nextWorkoutExercises: Int = 0,
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

// Eventos de UI para snacks/toasts, etc.
sealed interface HomeUiEvent {
    data class ShowSnackbar(val message: String) : HomeUiEvent
    data class ShowToast(val message: String) : HomeUiEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userLocalRepository: UserLocalRepository,
    private val userRepository: UserRepository,
    private val dietaRepository: DietaRepository,
    private val treinoRepository: TreinoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        checkAndResetDailyCaloriesAndMacros()
        observeUserData()
        loadNextWorkout()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userLocalRepository.getUserLocal().onEach { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        dailyCalories = user.dailyCalories,
                        eatenCaloriesToday = user.eatenCaloriesToday,
                        dailyCarbsGrams = user.dailyCarbsGrams,
                        dailyProteinGrams = user.dailyProteinGrams,
                        dailyFatGrams = user.dailyFatGrams,
                        eatenProteinGrams = user.eatenProteinToday,
                        eatenCarbsGrams = user.eatenCarbsToday,
                        eatenFatGrams = user.eatenFatToday,
                        currentWeight = user.currentWeight,
                        goalWeight = user.goalWeight,
                        bmi = user.bmi
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val existing = userLocalRepository.getAllWeightHistory(user.userId)
                        if (existing.isEmpty()) {
                            userLocalRepository.seedMockWeightHistory(user.userId)
                        }
                        loadWeightHistory(WeightRange.ONE_MONTH)
                    }
                }
            }.collect()
        }
    }

    private fun loadNextWorkout() {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal().firstOrNull() ?: return@launch
            
            treinoRepository.observePastas(user.userId).collect { pastas ->
                val allSessions = mutableListOf<Sessao>()
                pastas.forEach { pasta ->
                    val sessions = treinoRepository.observeSessoes(pasta.id).firstOrNull() ?: emptyList()
                    allSessions.addAll(sessions)
                }

                if (allSessions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(nextWorkoutId = null)
                    return@collect
                }
                
                val nextWorkout = allSessions.first()
                val exercises = treinoRepository.observeExercicios(nextWorkout.id).firstOrNull() ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    nextWorkoutId = nextWorkout.id,
                    nextWorkoutName = nextWorkout.nome,
                    nextWorkoutExercises = exercises.size
                )
            }
        }
    }

    fun onChangeCurrentWeight(newWeight: Float, currentRange: WeightRange) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal().firstOrNull() ?: return@launch
            val heightCm = user.heightCm
            val heightM = heightCm / 100f
            val newBmi = if (heightM > 0f) newWeight / (heightM * heightM) else 0f

            val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.LocalDate.now().toString()
            } else { "" }

            userLocalRepository.updateCurrentWeight(newWeight)
            userLocalRepository.updateBmi(newBmi)
            userLocalRepository.addWeightEntry(userId = user.userId, weight = newWeight, date = today)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recalculateDailyCaloriesAndUpdateState(currentWeight = newWeight, goalWeight = _uiState.value.goalWeight)
                loadWeightHistory(currentRange)
            }

            userRepository.createProgress(idUser = user.userId, pesoAtual = newWeight, pesoMeta = _uiState.value.goalWeight, caloriasDiarias = _uiState.value.dailyCalories)
            _uiEvent.send(HomeUiEvent.ShowToast("Current weight updated!"))
        }
    }

    fun onChangeGoalWeight(newWeight: Float) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal().firstOrNull() ?: return@launch
            userLocalRepository.updateGoalWeight(newWeight)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                recalculateDailyCaloriesAndUpdateState(currentWeight = _uiState.value.currentWeight, goalWeight = newWeight)
            }

            userRepository.createProgress(idUser = user.userId, pesoAtual = _uiState.value.currentWeight, pesoMeta = newWeight, caloriasDiarias = _uiState.value.dailyCalories)
            _uiEvent.send(HomeUiEvent.ShowToast("Goal weight updated!"))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun recalculateDailyCaloriesAndUpdateState(currentWeight: Float, goalWeight: Float) {
        val user = userLocalRepository.getUserLocal().firstOrNull() ?: return
        val heightCm = user.heightCm
        val gender = user.gender
        val birthDate = user.birthDate
        val activityLevel = user.activityLevel

        val age = try {
            val (y, m, d) = birthDate.split("-").map { it.toInt() }
            val dob = java.time.LocalDate.of(y, m, d)
            val today = java.time.LocalDate.now()
            java.time.Period.between(dob, today).years
        } catch (e: Exception) { 30 }

        val maintenance = calculateMaintenanceCalories(isMale = gender == "M", weightKg = currentWeight, heightCm = heightCm, age = age, activityLevel = activityLevel)
        val newCalories = calculateDailyCaloriesForWeightChange(maintenanceCalories = maintenance, currentWeightKg = currentWeight, goalWeightKg = goalWeight)
        userLocalRepository.updateDailyCaloriesValue(newCalories)
    }
    
    fun checkAndResetDailyCaloriesAndMacros() {
        viewModelScope.launch {
            userLocalRepository.checkAndResetDailyCaloriesAndMacros()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadWeightHistory(range: WeightRange) {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal().firstOrNull() ?: return@launch
            val userId = user.userId
            val today = java.time.LocalDate.now()

            val list = if (range == WeightRange.ALL_TIME) {
                userLocalRepository.getAllWeightHistory(userId)
            } else {
                val from = when (range) {
                    WeightRange.TWO_WEEKS    -> today.minusDays(14)
                    WeightRange.ONE_MONTH    -> today.minusMonths(1)
                    WeightRange.THREE_MONTHS -> today.minusMonths(3)
                    else -> today.minusYears(100)
                }.toString()
                userLocalRepository.getWeightHistoryFrom(userId, from)
            }

            _uiState.value = _uiState.value.copy(weightHistory = list.map { WeightHistoryPoint(it.date, it.weight) })
        }
    }
}
