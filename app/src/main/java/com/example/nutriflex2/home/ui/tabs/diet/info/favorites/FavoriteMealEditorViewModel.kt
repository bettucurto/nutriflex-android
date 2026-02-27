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

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(mealName = newName)
    }

    /**
     * Adiciona um ingrediente à refeição (chamado quando voltas do ecrã de detalhes)
     */
    fun addIngredient(food: FatSecretFoodDetails, serving: FatSecretServing, quantity: Double) {
        val newIngredient = DraftIngredient(
            food = food,
            selectedServing = serving,
            quantity = quantity
        )

        val newList = _uiState.value.ingredients + newIngredient

        // Define imagem de capa se for o primeiro item com imagem válida
        val currentCover = _uiState.value.coverImage
        val newCover = if (currentCover == null && !food.image.isNullOrBlank()) {
            food.image
        } else currentCover

        _uiState.value = _uiState.value.copy(
            ingredients = newList,
            coverImage = newCover
        )
        recalculateTotals()
    }

    fun removeIngredient(uniqueId: String) {
        val currentList = _uiState.value.ingredients.toMutableList()
        currentList.removeAll { it.uniqueId == uniqueId }

        // Atualiza imagem de capa se removemos o primeiro item
        val newCover = currentList.firstOrNull { !it.food.image.isNullOrBlank() }?.food?.image

        _uiState.value = _uiState.value.copy(
            ingredients = currentList,
            coverImage = newCover
        )
        recalculateTotals()
    }

    private fun recalculateTotals() {
        var cal = 0.0
        var carb = 0.0
        var prot = 0.0
        var fat = 0.0
        var satFat = 0.0
        var chol = 0.0
        var sod = 0.0
        var fib = 0.0
        var sug = 0.0

        _uiState.value.ingredients.forEach { item ->
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

        _uiState.value = _uiState.value.copy(
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMeal() { // <--- REMOVIDO O PARÂMETRO userId: Int
        if (_uiState.value.mealName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a meal name")
            return
        }
        if (_uiState.value.ingredients.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Add at least one food")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Obter o Utilizador Logado da Base de Dados Local
                val user = userLocalRepository.getUserLocal().firstOrNull()

                // Agora sim, acedemos a user.userId (conforme está no teu UserLocalRepository)
                val userId = user?.userId ?: throw Exception("User not found locally")

                // 2. Criar a Refeição
                val totalCalories = _uiState.value.caloriesTotal.toInt()
                val totalCarbs = _uiState.value.carbsTotal
                val totalProtein = _uiState.value.proteinTotal
                val totalFat = _uiState.value.fatTotal
                val totalMacros = totalCarbs + totalProtein + totalFat

                val carbsPct = if (totalMacros > 0) ((totalCarbs / totalMacros) * 100).toInt() else 0
                val proteinPct = if (totalMacros > 0) ((totalProtein / totalMacros) * 100).toInt() else 0
                val fatPct = if (totalMacros > 0) ((totalFat / totalMacros) * 100).toInt() else 0

                val newMealId = repository.addMeal(
                    name = _uiState.value.mealName,
                    userId = userId,
                    calories = totalCalories,
                    image = _uiState.value.coverImage,
                    carbsPct = carbsPct,
                    proteinPct = proteinPct,
                    fatPct = fatPct,
                    description = "${_uiState.value.ingredients.size} ingredients"
                )

                // 3. Adicionar Ingredientes
                _uiState.value.ingredients.forEach { draft ->
                    repository.addIngredientToMeal(
                        mealId = newMealId,
                        alimentoApiId = draft.food.id,
                        tipoPorcao = draft.selectedServing.description,
                        quantidadePorcoes = draft.quantity
                    )
                }

                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}