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

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.rawResults.filter { recipe ->
            val calories = parseCalories(recipe)
            matchesCalorieRange(calories, state.calorieRange)
        }
        _uiState.value = state.copy(suggestions = filtered)
    }

    private fun parseCalories(recipe: FatSecretRecipeSummary): Int {
        val nutrition = recipe.nutrition
        return try {
            val matcher = Pattern.compile("(\\d+)").matcher(nutrition.calories)
            if (matcher.find()) {
                matcher.group(1)?.toInt() ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun matchesCalorieRange(calories: Int, range: CalorieRangeFilter): Boolean = when (range) {
        CalorieRangeFilter.NONE -> true
        CalorieRangeFilter.UNDER_100 -> calories < 100
        CalorieRangeFilter.FROM_100_TO_250 -> calories in 100..250
        CalorieRangeFilter.FROM_250_TO_500 -> calories in 250..500
        CalorieRangeFilter.OVER_500 -> calories > 500
    }
}
