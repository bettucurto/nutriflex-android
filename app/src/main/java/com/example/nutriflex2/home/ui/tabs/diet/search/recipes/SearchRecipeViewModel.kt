package com.example.nutriflex2.home.ui.tabs.diet.search.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretRecipeSummary
import components.CalorieRangeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

data class SearchRecipeUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val suggestions: List<FatSecretRecipeSummary> = emptyList(),
    val rawResults: List<FatSecretRecipeSummary> = emptyList(),
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
class SearchRecipeViewModel @Inject constructor(
    private val dietaRepository: DietaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchRecipeUiState())
    val uiState: StateFlow<SearchRecipeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val defaultQuery = "Salad"
            _uiState.value = _uiState.value.copy(query = defaultQuery)
            searchRecipes(defaultQuery)
        }
    }

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
            delay(300L)
            searchRecipes(newQuery)
        }
    }

    private suspend fun searchRecipes(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        try {
            val searchResult = dietaRepository.searchRecipes(query)
            if (searchResult.sucesso) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    rawResults = searchResult.receitas
                )
                applyFilters()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Search failed",
                    rawResults = emptyList(),
                    suggestions = emptyList()
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = e.message ?: "Error searching recipes",
                rawResults = emptyList(),
                suggestions = emptyList()
            )
        }
    }

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

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.rawResults.filter { recipe ->
            val calories = parseCalories(recipe)
            val carbsPct = parseMacroPct(recipe, "carbohydrate")
            val proteinPct = parseMacroPct(recipe, "protein")
            val fatPct = parseMacroPct(recipe, "fat")

            matchesCalorieRange(calories, state.calorieRange) &&
                    carbsPct in state.carbsMin..state.carbsMax &&
                    proteinPct in state.proteinMin..state.proteinMax &&
                    fatPct in state.fatMin..state.fatMax
        }
        _uiState.value = state.copy(suggestions = filtered)
    }

    private fun parseCalories(recipe: FatSecretRecipeSummary): Int {
        val calString = recipe.nutrition.calories ?: return 0
        return calString.filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    private fun parseMacroPct(recipe: FatSecretRecipeSummary, macro: String): Int {
        val nutrition = recipe.nutrition
        val macroValueStr = when (macro) {
            "carbohydrate" -> nutrition.carbohydrate
            "protein" -> nutrition.protein
            "fat" -> nutrition.fat
            else -> null
        } ?: return 0

        val totalMacros = (nutrition.carbohydrate?.filter { it.isDigit() }?.toIntOrNull() ?: 0) +
                (nutrition.protein?.filter { it.isDigit() }?.toIntOrNull() ?: 0) +
                (nutrition.fat?.filter { it.isDigit() }?.toIntOrNull() ?: 0)

        if (totalMacros == 0) return 0

        val macroValue = macroValueStr.filter { it.isDigit() }.toIntOrNull() ?: 0
        return ((macroValue.toDouble() / totalMacros) * 100).toInt()
    }


    private fun matchesCalorieRange(calories: Int, range: CalorieRangeFilter): Boolean = when (range) {
        CalorieRangeFilter.NONE -> true
        CalorieRangeFilter.UNDER_100 -> calories < 100
        CalorieRangeFilter.FROM_100_TO_250 -> calories in 100..250
        CalorieRangeFilter.FROM_250_TO_500 -> calories in 250..500
        CalorieRangeFilter.OVER_500 -> calories > 500
    }
}