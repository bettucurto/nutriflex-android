package com.example.nutriflex2.home.ui.tabs.diet.info.meals

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretFoodDetails
import com.example.dieta.domain.FatSecretServing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

data class MealInfoUiState(
    val isLoading: Boolean = true,
    val food: FatSecretFoodDetails? = null,
    val error: String? = null,
    val servings: List<FatSecretServing> = emptyList(),
    val selectedServingIndex: Int = 0,
    val portionCount: Double = 1.0,

    // Totais calculados
    val caloriesTotal: Double = 0.0,
    val carbsTotal: Double = 0.0,
    val proteinTotal: Double = 0.0,
    val fatTotal: Double = 0.0,
    val saturatedFatTotal: Double = 0.0,
    val cholesterolTotal: Double = 0.0,
    val sodiumTotal: Double = 0.0,
    val fiberTotal: Double = 0.0,
    val sugarsTotal: Double = 0.0,
    val vitATotal: Double = 0.0,
    val vitCTotal: Double = 0.0,
    val calciumTotal: Double = 0.0,
    val ironTotal: Double = 0.0
)

@HiltViewModel
class MealInfoViewModel @Inject constructor(
    private val repository: DietaRepository,
    private val userLocalRepository: UserLocalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealInfoUiState())
    val uiState: StateFlow<MealInfoUiState> = _uiState.asStateFlow()

    // NULL se for "pesquisa normal", tem valor se for "criar refeição favorita"
    private var mealId: Int? = null

    init {
        val foodId = checkNotNull(savedStateHandle["foodId"]) as String
        mealId = savedStateHandle["mealId"] as? Int
        
        val initialServingId = savedStateHandle.get<String>("servingId")
        val initialPortion = savedStateHandle.get<String>("portion")?.toDoubleOrNull()
        val initialServingDesc = savedStateHandle.get<String>("servingDesc")

        viewModelScope.launch {
            loadFoodDetails(foodId, initialServingId, initialPortion, initialServingDesc)
        }
    }

    private suspend fun loadFoodDetails(
        foodId: String,
        initialServingId: String? = null,
        initialPortion: Double? = null,
        initialServingDesc: String? = null
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val details = repository.getFoodDetails(foodId)

            val adjustedServings = details.servings.map { serving ->
                if (serving.description.contains("100 g", ignoreCase = true) ||
                    serving.description.contains("100g")
                ) {
                    serving.copy(
                        description = "Grams",
                        metricAmount = 1.0,
                        calories = serving.calories / 100.0,
                        carbs = serving.carbs / 100.0,
                        protein = serving.protein / 100.0,
                        fat = serving.fat / 100.0,
                        saturatedfat = serving.saturatedfat / 100.0,
                        cholesterol = serving.cholesterol / 100.0,
                        sodium = serving.sodium / 100.0,
                        fiber = serving.fiber / 100.0,
                        sugar = serving.sugar / 100.0,
                        vitamina = serving.vitamina / 100.0,
                        vitaminc = serving.vitaminc / 100.0,
                        calcium = serving.calcium / 100.0,
                        iron = serving.iron / 100.0
                    )
                } else serving
            }

            // Lógica de seleção inicial
            var selectedIndex = -1

            // 1. Tentar por ID
            if (initialServingId != null) {
                selectedIndex = adjustedServings.indexOfFirst { it.id == initialServingId }
            }

            // 2. Tentar por Descrição (se ID falhou ou não foi providenciado)
            if (selectedIndex == -1 && initialServingDesc != null) {
                selectedIndex = adjustedServings.indexOfFirst { 
                    it.description.equals(initialServingDesc, ignoreCase = true) 
                }
            }

            // 3. Fallback para "Grams" ou o primeiro
            if (selectedIndex == -1) {
                selectedIndex = adjustedServings.indexOfFirst { it.description == "Grams" }
                if (selectedIndex == -1) selectedIndex = 0
            }

            // Lógica de quantidade inicial
            val portion = initialPortion ?: if (adjustedServings.getOrNull(selectedIndex)?.description == "Grams") 100.0 else 1.0

            _uiState.value = MealInfoUiState(
                isLoading = false,
                food = details,
                servings = adjustedServings,
                selectedServingIndex = selectedIndex,
                portionCount = portion
            ).recalculate()

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Erro: ${e.message}"
            )
        }
    }

    fun onServingSelected(index: Int) {
        _uiState.value = _uiState.value.copy(selectedServingIndex = index).recalculate()
    }

    fun onPortionCountChange(newCount: Double) {
        _uiState.value = _uiState.value.copy(portionCount = newCount).recalculate()
    }

    private fun MealInfoUiState.recalculate(): MealInfoUiState {
        val serving = servings.getOrNull(selectedServingIndex) ?: return this
        val factor = portionCount.coerceAtLeast(0.0)

        return copy(
            caloriesTotal = serving.calories * factor,
            carbsTotal = serving.carbs * factor,
            proteinTotal = serving.protein * factor,
            fatTotal = serving.fat * factor,
            saturatedFatTotal = serving.saturatedfat * factor,
            cholesterolTotal = serving.cholesterol * factor,
            sodiumTotal = serving.sodium * factor,
            fiberTotal = serving.fiber * factor,
            sugarsTotal = serving.sugar * factor,
            vitATotal = serving.vitamina * factor,
            vitCTotal = serving.vitaminc * factor,
            calciumTotal = serving.calcium * factor,
            ironTotal = serving.iron * factor
        )
    }

    /**
     * LÓGICA:
     * - mealId == null  → pesquisa normal → adiciona aos totais diários (calorias + macros)
     * - mealId != null  → criar/editar refeição favorita → só adiciona ingrediente à refeição
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun addToMeal() {
        val food = _uiState.value.food ?: return
        val serving = _uiState.value.servings.getOrNull(_uiState.value.selectedServingIndex) ?: return

        viewModelScope.launch {
            // Se tiver mealId → adicionar ingrediente à refeição favorita
            mealId?.let { mealIdLocal ->
                repository.addIngredientToMeal(
                    mealId = mealIdLocal,
                    alimentoApiId = food.id,
                    nomeAlimento = food.nomeEn,
                    tipoPorcao = serving.description,
                    quantidadePorcoes = _uiState.value.portionCount
                )
            }

            // Se não tiver mealId → é log de refeição normal → acumula no dia
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

        userLocalRepository.checkAndResetDailyCaloriesAndMacros() // Moved here from addCaloriesEaten and addDailyMacrosEaten

        // Soma calorias do dia
        userLocalRepository.addCaloriesEaten(calories.toInt())

        // Soma macros do dia
        userLocalRepository.addDailyMacrosEaten(
            protein = protein,
            carbs = carbs,
            fat = fat
        )
    }
}
