package com.example.nutriflex2.home.ui.tabs.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

data class DietUiState(
    val dailyCalories: Int = 0,
    val eatenCaloriesToday: Int = 0,
    val dailyCarbsGrams: Int = 0,
    val dailyProteinGrams: Int = 0,
    val dailyFatGrams: Int = 0,
    // por agora assumimos que nada foi consumido em macros
    val eatenProteinGrams: Int = 0,
    val eatenCarbsGrams: Int = 0,
    val eatenFatGrams: Int = 0,
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

    init {
        viewModelScope.launch {
            userLocalRepository.getUserLocal().onEach { user ->
                _uiState.value = DietUiState(
                    dailyCalories = user?.dailyCalories ?: 0,
                    eatenCaloriesToday = user?.eatenCaloriesToday ?: 0,
                    dailyCarbsGrams = user?.dailyCarbsGrams ?: 0,
                    dailyProteinGrams = user?.dailyProteinGrams ?: 0,
                    dailyFatGrams = user?.dailyFatGrams ?: 0,
                    eatenProteinGrams = user?.eatenProteinToday ?: 0,
                    eatenCarbsGrams = user?.eatenCarbsToday ?: 0,
                    eatenFatGrams = user?.eatenFatToday ?: 0,
                )
            }.collect()
        }
    }
}

