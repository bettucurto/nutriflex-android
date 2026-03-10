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

    // Totais calculados dinamicamente (em gramas ou unidades respetivas)
    val caloriesTotal: Double = 0.0,
    val carbsTotal: Double = 0.0,
    val proteinTotal: Double = 0.0,
    val fatTotal: Double = 0.0,
    
    // Detalhes para a Nutrition Facts Table
    val saturatedFatTotal: Double = 0.0,
    val cholesterolTotal: Double = 0.0,
    val sodiumTotal: Double = 0.0,
    val fiberTotal: Double = 0.0,
    val sugarsTotal: Double = 0.0
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
        val mealIdStr = checkNotNull(savedStateHandle["mealId"]) as String
        val idInt = mealIdStr.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Sincronizar ingredientes do remoto
            launch {
                repository.refreshIngredientsFromRemote(idInt)
            }
            
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
                    _uiState.value = _uiState.value.copy(
                        ingredients = ingredients,
                        isLoading = false
                    )
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

        // Nota: A Refeicao favoritada no domínio atualmente guarda carbsPct, proteinPct, fatPct.
        // Se quisermos mostrar gramas na UI (como o design das receitas faz), 
        // precisamos de estimar baseando-nos em 4kcal/g para carb/prot e 9kcal/g para fat.
        
        val totalCals = (meal.calories?.toDouble() ?: 0.0) * factor
        
        val estCarbs = if (totalCals > 0) (totalCals * (meal.carbsPct ?: 0) / 100.0) / 4.0 else 0.0
        val estProt = if (totalCals > 0) (totalCals * (meal.proteinPct ?: 0) / 100.0) / 4.0 else 0.0
        val estFat = if (totalCals > 0) (totalCals * (meal.fatPct ?: 0) / 100.0) / 9.0 else 0.0

        _uiState.value = state.copy(
            caloriesTotal = totalCals,
            carbsTotal = estCarbs,
            proteinTotal = estProt,
            fatTotal = estFat,
            // Valores detalhados começam a 0 pois não estão no modelo simplificado da Refeicao
            saturatedFatTotal = 0.0,
            cholesterolTotal = 0.0,
            sodiumTotal = 0.0,
            fiberTotal = 0.0,
            sugarsTotal = 0.0
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
