package com.example.nutriflex2.home.ui.tabs.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import local.UserLocalRepository
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DietUiState(
    val dailyCalories: Int = 0,
    val eatenCaloriesToday: Int = 0,
    val dailyCarbsGrams: Int = 0,
    val dailyProteinGrams: Int = 0,
    val dailyFatGrams: Int = 0,
    val eatenProteinGrams: Int = 0,
    val eatenCarbsGrams: Int = 0,
    val eatenFatGrams: Int = 0,
    val isFasting: Boolean = false,
    val fastingStartTime: String? = null,
    val fastingDuration: String = "16:8",
    val remainingTime: String = "00:00:00",
    val progress: Float = 0f
) {
    val remainingCalories: Int
        get() = (dailyCalories - eatenCaloriesToday).coerceAtLeast(0)

    val caloriesProgress: Float
        get() = if (dailyCalories == 0) 0f
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


@HiltViewModel
class DietTabViewModel @Inject constructor(
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DietUiState())
    val uiState: StateFlow<DietUiState> = _uiState.asStateFlow()
    
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    init {
        viewModelScope.launch {
            userLocalRepository.getUserLocal().onEach { user ->
                _uiState.value = _uiState.value.copy(
                    dailyCalories = user?.dailyCalories ?: 0,
                    eatenCaloriesToday = user?.eatenCaloriesToday ?: 0,
                    dailyCarbsGrams = user?.dailyCarbsGrams ?: 0,
                    dailyProteinGrams = user?.dailyProteinGrams ?: 0,
                    dailyFatGrams = user?.dailyFatGrams ?: 0,
                    eatenProteinGrams = user?.eatenProteinToday ?: 0,
                    eatenCarbsGrams = user?.eatenCarbsToday ?: 0,
                    eatenFatGrams = user?.eatenFatToday ?: 0
                )
            }.collect()
        }
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                updateFastingProgress()
                delay(1000)
            }
        }
    }

    private fun updateFastingProgress() {
        val state = _uiState.value
        if (state.fastingStartTime == null) return

        val start = LocalDateTime.parse(state.fastingStartTime, formatter)
        val now = LocalDateTime.now()
        
        // Exemplo: "16:8"
        val parts = state.fastingDuration.split(":")
        val durationHours = if (state.isFasting) {
            parts[0].toLong() // Horas de Jejum
        } else {
            parts[1].toLong() // Horas de Comer
        }
        
        val end = start.plusHours(durationHours)
        val remaining = Duration.between(now, end)
        
        if (remaining.isNegative) {
            // Quando acaba um período, inicia automaticamente o outro (Ciclo)
            val isStartingFasting = !state.isFasting
            _uiState.value = _uiState.value.copy(
                isFasting = isStartingFasting,
                fastingStartTime = now.format(formatter)
            )
            return
        }

        val formattedRemaining = String.format("%02d:%02d:%02d", 
            remaining.toHours(), remaining.toMinutesPart(), remaining.toSecondsPart())
        
        val totalDurationMillis = Duration.ofHours(durationHours).toMillis().toFloat()
        val elapsedMillis = Duration.between(start, now).toMillis().toFloat()
        val progress = (elapsedMillis / totalDurationMillis).coerceIn(0f, 1f)

        _uiState.value = _uiState.value.copy(
            remainingTime = formattedRemaining,
            progress = progress
        )
    }

    fun toggleFasting() {
        val now = LocalDateTime.now().format(formatter)
        val isCurrentlyFasting = _uiState.value.isFasting
        
        // Se estava em jejum, passa para Eating Window. Se estava a comer, passa para Jejum.
        _uiState.value = _uiState.value.copy(
            isFasting = !isCurrentlyFasting,
            fastingStartTime = now,
            progress = 0f
        )
    }

    fun updateFastingDuration(duration: String) {
        // Ao mudar a duração (ex: de 16:8 para 12:12), reinicia o cronómetro para o novo objetivo
        val now = LocalDateTime.now().format(formatter)
        _uiState.value = _uiState.value.copy(
            fastingDuration = duration,
            fastingStartTime = now,
            progress = 0f
        )
    }
}
