package com.example.nutriflex2.diet.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretFood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CalorieRangeFilter {
    NONE, UNDER_100, FROM_100_TO_250, FROM_250_TO_500, OVER_500
}

data class SearchMealsUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val history: List<FatSecretFood> = emptyList(),

    // filtered list shown in UI
    val suggestions: List<FatSecretFood> = emptyList(),
    // raw API results before filters
    val rawResults: List<FatSecretFood> = emptyList(),
    val errorMessage: String? = null,

    val calorieRange: CalorieRangeFilter = CalorieRangeFilter.NONE,
    val carbsMin: Int = 0,
    val carbsMax: Int = 100,
    val proteinMin: Int = 0,
    val proteinMax: Int = 100,
    val fatMin: Int = 0,
    val fatMax: Int = 100,
)

@HiltViewModel
class SearchMealsViewModel @Inject constructor(
    private val dietaRepository: DietaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchMealsUiState())
    val uiState: StateFlow<SearchMealsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)

        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.value = _uiState.value.copy(
                suggestions = emptyList(),
                rawResults = emptyList()
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(300L) // debounce
            searchFoods(newQuery)
        }
    }

    private suspend fun searchFoods(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val foods = dietaRepository.searchFoods(query)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                rawResults = foods
            )
            applyFilters()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Error searching foods",
                rawResults = emptyList(),
                suggestions = emptyList()
            )
        }
    }

    fun onAddFoodToHistory(food: FatSecretFood) {
        val history = _uiState.value.history.toMutableList()
        history.add(0, food)
        _uiState.value = _uiState.value.copy(history = history.distinctBy { it.id })
    }

    // --------- Filters API (called from UI) ---------

    fun onCalorieRangeSelected(range: CalorieRangeFilter) {
        _uiState.value = _uiState.value.copy(calorieRange = range)
        applyFilters()
    }

    fun onCarbsRangeChanged(min: Int, max: Int) {
        _uiState.value = _uiState.value.copy(
            carbsMin = min.coerceIn(0, 100),
            carbsMax = max.coerceIn(0, 100)
        )
        applyFilters()
    }

    fun onProteinRangeChanged(min: Int, max: Int) {
        _uiState.value = _uiState.value.copy(
            proteinMin = min.coerceIn(0, 100),
            proteinMax = max.coerceIn(0, 100)
        )
        applyFilters()
    }

    fun onFatRangeChanged(min: Int, max: Int) {
        _uiState.value = _uiState.value.copy(
            fatMin = min.coerceIn(0, 100),
            fatMax = max.coerceIn(0, 100)
        )
        applyFilters()
    }

    // --------- Filtering logic ---------

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.rawResults.filter { food ->
            val calories = parseCalories(food)
            val carbsPct = parseMacroPct(food, "carbs")
            val proteinPct = parseMacroPct(food, "protein")
            val fatPct = parseMacroPct(food, "fat")

            matchesCalorieRange(calories, state.calorieRange) &&
                    carbsPct in state.carbsMin..state.carbsMax &&
                    proteinPct in state.proteinMin..state.proteinMax &&
                    fatPct in state.fatMin..state.fatMax
        }

        _uiState.value = state.copy(suggestions = filtered)
    }

    private fun parseCalories(food: FatSecretFood): Int =
        food.calories ?: 0

    private fun parseMacroPct(food: FatSecretFood, macro: String): Int =
        when (macro) {
            "carbs" -> food.carbsPct ?: 0
            "protein" -> food.proteinPct ?: 0
            "fat" -> food.fatPct ?: 0
            else -> 0
        }


    private fun matchesCalorieRange(
        calories: Int,
        range: CalorieRangeFilter
    ): Boolean = when (range) {
        CalorieRangeFilter.NONE -> true
        CalorieRangeFilter.UNDER_100 -> calories < 100
        CalorieRangeFilter.FROM_100_TO_250 -> calories in 100..250
        CalorieRangeFilter.FROM_250_TO_500 -> calories in 250..500
        CalorieRangeFilter.OVER_500 -> calories > 500
    }
}
