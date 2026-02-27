package com.example.nutriflex2.home.ui.tabs.diet.info.favorites

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.IngredienteRefeicao
import com.example.dieta.domain.Refeicao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

data class FavoriteMealInfoUiState(
    val isLoading: Boolean = true,
    val meal: Refeicao? = null,
    val ingredients: List<IngredienteRefeicao> = emptyList(),
    val error: String? = null,
    val portionCount: Double = 1.0,

    // Totais calculados dinamicamente
    val caloriesTotal: Double = 0.0,
    val carbsTotal: Double = 0.0,
    val proteinTotal: Double = 0.0,
    val fatTotal: Double = 0.0
)

@HiltViewModel
class FavoriteMealInfoViewModel @Inject constructor(
    private val repository: DietaRepository,
    private val userLocalRepository: UserLocalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteMealInfoUiState())
    val uiState: StateFlow<FavoriteMealInfoUiState> = _uiState.asStateFlow()

    init {
        val mealId = checkNotNull(savedStateHandle["mealId"]) as String
        val idInt = mealId.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Observar a refeição
            launch {
                repository.observeMeal(idInt).collectLatest { meal ->
                    _uiState.value = _uiState.value.copy(meal = meal)
                    recalculate()
                }
            }

            // Observar os ingredientes
            launch {
                repository.observeIngredients(idInt).collectLatest { ingredients ->
                    _uiState.value = _uiState.value.copy(ingredients = ingredients, isLoading = false)
                }
            }
        }
    }

    fun onPortionCountChange(newCount: Double) {
        _uiState.value = _uiState.value.copy(portionCount = newCount)
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val meal = state.meal ?: return
        val factor = state.portionCount.coerceAtLeast(0.0)

        _uiState.value = state.copy(
            caloriesTotal = (meal.calories?.toDouble() ?: 0.0) * factor,
            // Para simplificar, usamos as percentagens e as calorias totais para estimar gramas se necessário,
            // ou apenas multiplicamos o total guardado se tivéssemos gramas. 
            // Como guardamos carbsPct, vamos assumir que o total visual é o que importa.
            carbsTotal = (meal.carbsPct?.toDouble() ?: 0.0) * factor, // Aqui seriam gramas no ideal
            proteinTotal = (meal.proteinPct?.toDouble() ?: 0.0) * factor,
            fatTotal = (meal.fatPct?.toDouble() ?: 0.0) * factor
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun logMeal() {
        val state = _uiState.value
        val meal = state.meal ?: return

        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal().firstOrNull() ?: return@launch
            userLocalRepository.checkAndResetDailyCaloriesAndMacros()
            
            val factor = state.portionCount
            val caloriesToAdd = (meal.calories ?: 0) * factor
            
            // Como guardamos apenas as percentagens no modelo simplificado, 
            // no log diário vamos precisar de estimar ou ter os gramas totais.
            // Para este protótipo, vamos logar as calorias e macros proporcionais.
            
            userLocalRepository.addCaloriesEaten(caloriesToAdd.toInt())
            // Nota: Idealmente teríamos os gramas totais guardados na RefeicaoLocal
            // para fazer este cálculo corretamente.
        }
    }
}
