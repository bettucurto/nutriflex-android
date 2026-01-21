package com.example.nutriflex2.home.ui.tabs.diet.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretFoodDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodDetailUiState(
    val isLoading: Boolean = true,
    val food: FatSecretFoodDetails? = null,
    val error: String? = null,
    val quantity: Int = 1,
    val portionGrams: Double = 100.0,
    val caloriesTotal: Int = 0,
    val carbsTotal: Double = 0.0,
    val proteinTotal: Double = 0.0,
    val fatTotal: Double = 0.0
)

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val repository: DietaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    init {
        val foodId = checkNotNull(savedStateHandle["foodId"])
        loadFoodDetails(foodId as String)
    }

    private fun loadFoodDetails(foodId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val details = repository.getFoodDetails(foodId)
                val baseCals = details.calorias.toIntOrNull() ?: 0
                val baseCarbs = details.carboidratos.toDoubleOrNull() ?: 0.0
                val baseProt = details.proteina.toDoubleOrNull() ?: 0.0
                val baseFat = details.gordura.toDoubleOrNull() ?: 0.0
                _uiState.value = _uiState.value.copy(
                    food = details,
                    isLoading = false,
                    caloriesTotal = baseCals,
                    carbsTotal = baseCarbs,
                    proteinTotal = baseProt,
                    fatTotal = baseFat
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load food details"
                )
            }
        }
    }

    fun updateQuantity(qty: Int) {
        recalculateTotals(qty, _uiState.value.portionGrams)
    }

    fun updatePortionGrams(grams: Double) {
        recalculateTotals(_uiState.value.quantity, grams)
    }

    private fun recalculateTotals(qty: Int, grams: Double) {
        val current = _uiState.value.food ?: return
        val baseCals = current.calorias.toIntOrNull() ?: 0
        // Assume base values are for 100g; scale by grams/100 * qty
        val scaleFactor = (grams / 100.0) * qty
        _uiState.value = _uiState.value.copy(
            quantity = qty,
            portionGrams = grams,
            caloriesTotal = (baseCals * scaleFactor).toInt(),
            carbsTotal = (current.carboidratos.toDoubleOrNull() ?: 0.0) * scaleFactor,
            proteinTotal = (current.proteina.toDoubleOrNull() ?: 0.0) * scaleFactor,
            fatTotal = (current.gordura.toDoubleOrNull() ?: 0.0) * scaleFactor
        )
    }

    fun addToMeal() {
        // TODO: Implement add to selected meal via repository
    }
}
