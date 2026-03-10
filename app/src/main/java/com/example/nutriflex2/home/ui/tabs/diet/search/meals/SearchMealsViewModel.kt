package com.example.nutriflex2.home.ui.tabs.diet.search.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretFood
import components.CalorieRangeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import com.example.dieta.domain.Refeicao
import local.UserLocalRepository
import kotlinx.coroutines.flow.collectLatest

data class SearchMealsUiState(
    val query: String = "",
    val isLoading: Boolean = false,

    val suggestions: List<FatSecretFood> = emptyList(),
    val rawResults: List<FatSecretFood> = emptyList(),
    val favoriteMeals: List<Refeicao> = emptyList(),
    val errorMessage: String? = null,

    val calorieRange: CalorieRangeFilter = CalorieRangeFilter.NONE,
    val carbsMin: Int = 0,
    val carbsMax: Int = 100,
    val proteinMin: Int = 0,
    val proteinMax: Int = 100,
    val fatMin: Int = 0,
    val fatMax: Int = 100,
    val autocompleteSuggestions: List<String> = emptyList(),
)



@HiltViewModel
class SearchMealsViewModel @Inject constructor(
    private val dietaRepository: DietaRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchMealsUiState())
    val uiState: StateFlow<SearchMealsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            userLocalRepository.getUserLocal().collectLatest { user ->
                android.util.Log.d("SearchMealsVM", "User observed: ${user?.userId}")
                user?.let {
                    // Carregar favoritos do utilizador da base de dados local apenas
                    launch {
                        dietaRepository.observeMeals(it.userId).collect { meals ->
                            android.util.Log.d("SearchMealsVM", "Observed ${meals.size} meals for user ${it.userId}")
                            _uiState.update { it.copy(favoriteMeals = meals) }
                        }
                    }
                }
            }
        }
        // pesquisa inicial para não começar vazio
        viewModelScope.launch {
            // podes trocar "chicken" por algo mais neutro, tipo "apple"
            val defaultQuery = "Apple"
            _uiState.update { it.copy(query = defaultQuery) }
            searchFoods(defaultQuery)
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, autocompleteSuggestions = emptyList()) }

        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(
                suggestions = emptyList(),
                rawResults = emptyList(),
                autocompleteSuggestions = emptyList()
            ) }
            return
        }

        // autocomplete imediato (sem debounce)
        if (newQuery.length >= 2) {
            viewModelScope.launch {
                try {
                    val suggestions = dietaRepository.searchAutocomplete(newQuery)
                    _uiState.update { it.copy(autocompleteSuggestions = suggestions) }
                } catch (e: Exception) {
                    // ignora erro de autocomplete
                }
            }
        }

        // pesquisa completa com debounce
        searchJob = viewModelScope.launch {
            delay(300L)
            searchFoods(newQuery)
        }
    }

    private suspend fun searchFoods(query: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val foods = dietaRepository.searchFoods(query)
            _uiState.update { it.copy(
                isLoading = false,
                rawResults = foods
            ) }
            applyFilters()
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isLoading = false,
                errorMessage = e.message ?: "Error searching foods",
                rawResults = emptyList(),
                suggestions = emptyList()
            ) }
        }
    }

    // --------- Filters API (called from UI) ---------

    fun onCalorieRangeSelected(range: CalorieRangeFilter) {
        _uiState.update { it.copy(calorieRange = range) }
        applyFilters()
    }

    fun onCarbsRangeChanged(min: Int, max: Int) {
        _uiState.update { it.copy(
            carbsMin = min.coerceIn(0, 100),
            carbsMax = max.coerceIn(0, 100)
        ) }
        applyFilters()
    }

    fun onProteinRangeChanged(min: Int, max: Int) {
        _uiState.update { it.copy(
            proteinMin = min.coerceIn(0, 100),
            proteinMax = max.coerceIn(0, 100)
        ) }
        applyFilters()
    }

    fun onFatRangeChanged(min: Int, max: Int) {
        _uiState.update { it.copy(
            fatMin = min.coerceIn(0, 100),
            fatMax = max.coerceIn(0, 100)
        ) }
        applyFilters()
    }

    // --------- Filtering logic ---------

    private fun applyFilters() {
        _uiState.update { state ->
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
            state.copy(suggestions = filtered)
        }
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
