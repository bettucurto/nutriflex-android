package com.example.nutriflex2.home.ui.tabs.diet.favorites

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretFoodDetails
import com.example.dieta.domain.FatSecretServing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.UserLocalRepository
import java.util.UUID
import javax.inject.Inject

// Wrapper para um ingrediente na lista temporária de criação
data class DraftIngredient(
    val uniqueId: String = UUID.randomUUID().toString(), // Para identificar na lista UI
    val food: FatSecretFoodDetails,
    val selectedServing: FatSecretServing,
    val quantity: Double
)

data class FavoriteMealEditorUiState(
    val isLoading: Boolean = false,
    val mealId: Int? = null, // Se presente, estamos em modo de edição
    val mealName: String = "",
    val ingredients: List<DraftIngredient> = emptyList(),
    val error: String? = null,
    val isSaved: Boolean = false,

    // Totais Agregados
    val caloriesTotal: Double = 0.0,
    val carbsTotal: Double = 0.0,
    val proteinTotal: Double = 0.0,
    val fatTotal: Double = 0.0,
    val saturatedFatTotal: Double = 0.0,
    val cholesterolTotal: Double = 0.0,
    val sodiumTotal: Double = 0.0,
    val fiberTotal: Double = 0.0,
    val sugarsTotal: Double = 0.0,

    // Para a imagem de capa (primeiro alimento)
    val coverImage: String? = null
)

@HiltViewModel
class FavoriteMealEditorViewModel @Inject constructor(
    private val repository: DietaRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteMealEditorUiState())
    val uiState: StateFlow<FavoriteMealEditorUiState> = _uiState.asStateFlow()

    /**
     * Carrega uma refeição existente para edição
     */
    fun loadMeal(id: Int) {
        if (_uiState.value.mealId == id) return // Já carregado

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mealId = id) }
            try {
                // Obter a refeição e seus ingredientes do repositório
                val meal = repository.observeMeal(id).firstOrNull() ?: throw Exception("Meal not found")
                val ingredients = repository.observeIngredients(id).firstOrNull() ?: emptyList()

                // Converter IngredienteRefeicao para DraftIngredient
                val draftIngredients = ingredients.map { ing ->
                    val foodDetails = repository.getFoodDetails(ing.alimentoApiId)
                    
                    // Ajustar as porções do alimento para serem consistentes com o MealInfoViewModel (ex: 100g -> Grams de 1g)
                    val adjustedServings = foodDetails.servings.map { adjustServing(it) }
                    
                    val targetType = ing.tipoPorcao.trim().lowercase()
                    android.util.Log.d("MealEditorVM", "Matching serving for ${ing.nomeAlimento}. Target: '$targetType'")
                    
                    // Procurar a porção correta entre as AJUSTADAS
                    var serving = adjustedServings.find { 
                        it.description.trim().lowercase() == targetType ||
                        it.measurementDescription?.trim()?.lowercase() == targetType
                    }

                    // Lógica especial para Gramas/Grams/g
                    if (serving == null && (targetType == "grams" || targetType == "gram" || targetType == "g")) {
                        serving = adjustedServings.find { it.description == "Grams" }
                    }

                    if (serving == null) {
                        serving = adjustedServings.find {
                            val cleanDesc = it.description.trim().lowercase().replace(" ", "")
                            val cleanTarget = targetType.replace(" ", "")
                            cleanDesc == cleanTarget
                        }
                    }

                    val finalServing = serving ?: adjustedServings.first()

                    android.util.Log.d("MealEditorVM", "Matched with: '${finalServing.description}'")

                    DraftIngredient(
                        food = foodDetails.copy(servings = adjustedServings),
                        selectedServing = finalServing,
                        quantity = ing.quantidadePorcoes
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    mealName = meal.nome,
                    ingredients = draftIngredients,
                    coverImage = meal.image
                ) }
                recalculateTotals()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun adjustServing(serving: FatSecretServing): FatSecretServing {
        return if (serving.description.contains("100 g", ignoreCase = true) ||
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

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(mealName = newName) }
    }

    /**
     * Adiciona um ingrediente à refeição (chamado quando voltas do ecrã de detalhes)
     */
    fun addIngredient(food: FatSecretFoodDetails, serving: FatSecretServing, quantity: Double) {
        // Aqui o 'serving' já vem ajustado do MealInfoViewModel
        val newIngredient = DraftIngredient(
            food = food,
            selectedServing = serving,
            quantity = quantity
        )

        _uiState.update { state ->
            val newList = state.ingredients + newIngredient
            val newCover = if (state.coverImage == null && !food.image.isNullOrBlank()) {
                food.image
            } else state.coverImage
            
            state.copy(
                ingredients = newList,
                coverImage = newCover
            )
        }
        recalculateTotals()
    }

    fun removeIngredient(uniqueId: String) {
        _uiState.update { state ->
            val currentList = state.ingredients.toMutableList()
            currentList.removeAll { it.uniqueId == uniqueId }
            val newCover = currentList.firstOrNull { !it.food.image.isNullOrBlank() }?.food?.image
            
            state.copy(
                ingredients = currentList,
                coverImage = newCover
            )
        }
        recalculateTotals()
    }

    private fun recalculateTotals() {
        _uiState.update { state ->
            var cal = 0.0
            var carb = 0.0
            var prot = 0.0
            var fat = 0.0
            var satFat = 0.0
            var chol = 0.0
            var sod = 0.0
            var fib = 0.0
            var sug = 0.0

            state.ingredients.forEach { item ->
                val factor = item.quantity
                val s = item.selectedServing

                cal += s.calories * factor
                carb += s.carbs * factor
                prot += s.protein * factor
                fat += s.fat * factor
                satFat += s.saturatedfat * factor
                chol += s.cholesterol * factor
                sod += s.sodium * factor
                fib += s.fiber * factor
                sug += s.sugar * factor
            }

            state.copy(
                caloriesTotal = cal,
                carbsTotal = carb,
                proteinTotal = prot,
                fatTotal = fat,
                saturatedFatTotal = satFat,
                cholesterolTotal = chol,
                sodiumTotal = sod,
                fiberTotal = fib,
                sugarsTotal = sug
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMeal() {
        if (_uiState.value.mealName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a meal name") }
            return
        }
        if (_uiState.value.ingredients.isEmpty()) {
            _uiState.update { it.copy(error = "Add at least one food") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = userLocalRepository.getUserLocal().firstOrNull()
                val userId = user?.userId ?: throw Exception("User not found locally")

                val totalCalories = _uiState.value.caloriesTotal.toInt()
                val totalCarbs = _uiState.value.carbsTotal
                val totalProtein = _uiState.value.proteinTotal
                val totalFat = _uiState.value.fatTotal
                val totalMacros = totalCarbs + totalProtein + totalFat

                val carbsPct = if (totalMacros > 0) ((totalCarbs / totalMacros) * 100).toInt() else 0
                val proteinPct = if (totalMacros > 0) ((totalProtein / totalMacros) * 100).toInt() else 0
                val fatPct = if (totalMacros > 0) ((totalFat / totalMacros) * 100).toInt() else 0

                // Gerar descrição formatada: "Calories: xx | Protein: x.xg | Carbs: x.xg | Fats: x.xg"
                val formattedDescription = String.format(
                    "Calories: %d | Protein: %.1fg | Carbs: %.1fg | Fats: %.1fg",
                    totalCalories, totalProtein, totalCarbs, totalFat
                )

                val currentMealId = _uiState.value.mealId
                val mealIdToUse: Int

                if (currentMealId == null) {
                    mealIdToUse = repository.addMeal(
                        name = _uiState.value.mealName,
                        userId = userId,
                        calories = totalCalories,
                        image = _uiState.value.coverImage,
                        carbsPct = carbsPct,
                        proteinPct = proteinPct,
                        fatPct = fatPct,
                        description = formattedDescription
                    )
                } else {
                    repository.updateMeal(
                        id = currentMealId,
                        name = _uiState.value.mealName,
                        calories = totalCalories,
                        image = _uiState.value.coverImage,
                        carbsPct = carbsPct,
                        proteinPct = proteinPct,
                        fatPct = fatPct,
                        description = formattedDescription
                    )
                    mealIdToUse = currentMealId
                }

                // Para ingredientes, simplificamos: removemos os atuais e inserimos os novos no backend
                // Como não temos um método remoto direto para apagar todos os ingredientes, 
                // por agora vamos apenas adicionar (o backend idealmente deveria gerir a atualização).
                // NOTA: Se o backend permitir duplicados, isto pode ser um problema.
                
                _uiState.value.ingredients.forEach { draft ->
                    repository.addIngredientToMeal(
                        mealId = mealIdToUse,
                        alimentoApiId = draft.food.id,
                        nomeAlimento = draft.food.nomeEn,
                        tipoPorcao = draft.selectedServing.description,
                        quantidadePorcoes = draft.quantity
                    )
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
