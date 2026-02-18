package com.example.nutriflex2.home.ui.tabs.diet.info.recipes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

data class RecipeInfoUiState(
    val isLoading: Boolean = true,
    val recipe: FatSecretRecipe? = null,
    val error: String? = null,
    val portionCount: Double = 1.0, // Quantas porções o utilizador vai comer

    // Totais calculados
    val caloriesTotal: Double = 0.0,
    val carbsTotal: Double = 0.0,
    val proteinTotal: Double = 0.0,
    val fatTotal: Double = 0.0,
    val saturatedFatTotal: Double = 0.0,
    val cholesterolTotal: Double = 0.0,
    val sodiumTotal: Double = 0.0,
    val fiberTotal: Double = 0.0,
    val sugarsTotal: Double = 0.0
)

@HiltViewModel
class RecipeInfoViewModel @Inject constructor(
    private val repository: DietaRepository,
    private val userLocalRepository: UserLocalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeInfoUiState())
    val uiState: StateFlow<RecipeInfoUiState> = _uiState.asStateFlow()

    private var mealId: Int? = null

    init {
        val recipeId = checkNotNull(savedStateHandle["recipeId"]) as String
        mealId = savedStateHandle["mealId"] as? Int

        viewModelScope.launch {
            loadRecipeDetails(recipeId)
        }
    }

    private suspend fun loadRecipeDetails(recipeId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val details = repository.getRecipeDetails(recipeId)

            _uiState.value = RecipeInfoUiState(
                isLoading = false,
                recipe = details,
                portionCount = 1.0
            ).recalculate()

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Erro: ${e.message}"
            )
        }
    }

    fun onPortionCountChange(newCount: Double) {
        _uiState.value = _uiState.value.copy(portionCount = newCount).recalculate()
    }

    private fun RecipeInfoUiState.recalculate(): RecipeInfoUiState {
        val currentRecipe = recipe ?: return this
        val factor = portionCount.coerceAtLeast(0.0)

        // As receitas normalmente trazem 1 Serving principal na lista
        val serving = currentRecipe.servings.firstOrNull()

        return copy(
            caloriesTotal = (serving?.calories?.toDoubleOrNull() ?: 0.0) * factor,
            carbsTotal = (serving?.carbohydrate?.toDoubleOrNull() ?: 0.0) * factor,
            proteinTotal = (serving?.protein?.toDoubleOrNull() ?: 0.0) * factor,
            fatTotal = (serving?.fat?.toDoubleOrNull() ?: 0.0) * factor,
            saturatedFatTotal = (serving?.saturatedFat?.toDoubleOrNull() ?: 0.0) * factor,
            cholesterolTotal = (serving?.cholesterol?.toDoubleOrNull() ?: 0.0) * factor,
            sodiumTotal = (serving?.sodium?.toDoubleOrNull() ?: 0.0) * factor,
            fiberTotal = (serving?.fiber?.toDoubleOrNull() ?: 0.0) * factor,
            sugarsTotal = (serving?.sugar?.toDoubleOrNull() ?: 0.0) * factor
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToMeal() {
        val currentRecipe = _uiState.value.recipe ?: return

        viewModelScope.launch {
            mealId?.let { mealIdLocal ->
                repository.addIngredientToMeal(
                    mealId = mealIdLocal,
                    alimentoApiId = currentRecipe.id,
                    tipoPorcao = "Serving",
                    quantidadePorcoes = _uiState.value.portionCount
                )
            }

            if (mealId == null) {
                addToDailyTotals(
                    calories = _uiState.value.caloriesTotal,
                    protein = _uiState.value.proteinTotal,
                    carbs = _uiState.value.carbsTotal,
                    fat = _uiState.value.fatTotal
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun addToDailyTotals(
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double
    ) {
        val user = userLocalRepository.getUserLocal() ?: return
        userLocalRepository.checkAndResetDailyCaloriesAndMacros()
        userLocalRepository.addCaloriesEaten(calories.toInt())
        userLocalRepository.addDailyMacrosEaten(protein = protein, carbs = carbs, fat = fat)
    }
}