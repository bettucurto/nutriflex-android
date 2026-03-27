package com.example.nutriflex2.home.ui.tabs.diet.search.scan

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.dieta.domain.FatSecretFoodDetails
import com.example.dieta.domain.FatSecretServing
import com.example.dieta.domain.FoodRecognitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.UserLocalRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import javax.inject.Inject

data class ScanConfirmIngredient(
    val name: String,
    val weight: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val imageUrl: String? = null,
    val id: String = UUID.randomUUID().toString()
)

data class ScanConfirmUiState(
    val imageUri: Uri? = null,
    val ingredients: List<ScanConfirmIngredient> = emptyList(),
    val isLoading: Boolean = false,
    val isLogged: Boolean = false
)

@HiltViewModel
class ScanConfirmViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val userLocalRepository: UserLocalRepository,
    private val dietaRepository: DietaRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ScanConfirmUiState())
    val uiState: StateFlow<ScanConfirmUiState> = _uiState.asStateFlow()

    private var metadataMap: Map<String, FoodRecognitionUseCase.IngredientMetadata> = emptyMap()

    init {
        val uriStr: String? = savedStateHandle["imageUri"]
        val initialIngredientsJson: String? = savedStateHandle["ingredientsJson"]
        
        _uiState.update { it.copy(imageUri = uriStr?.let { Uri.parse(it) }) }
        
        loadMetadata()
        
        // Se passarmos via JSON ou lista, aqui processamos. 
        // Para simplificar, o ScanMealViewModel pode passar uma lista via SavedStateHandle se for parcelable,
        // mas como são classes custom, vamos assumir que o ScanConfirmScreen recebe os dados iniciais.
    }

    private fun loadMetadata() {
        val map = mutableMapOf<String, FoodRecognitionUseCase.IngredientMetadata>()
        try {
            val inputStream = getApplication<Application>().assets.open("ingredients_metadata.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLine() // skip header
            var line: String? = reader.readLine()
            while (line != null) {
                val tokens = line.split(";")
                if (tokens.size >= 6) {
                    val name = tokens[0].lowercase().trim()
                    map[name] = FoodRecognitionUseCase.IngredientMetadata(
                        name = name,
                        calPerGram = tokens[2].toDoubleOrNull() ?: 0.0,
                        fatPerGram = tokens[3].toDoubleOrNull() ?: 0.0,
                        carbPerGram = tokens[4].toDoubleOrNull() ?: 0.0,
                        proteinPerGram = tokens[5].toDoubleOrNull() ?: 0.0
                    )
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("ScanConfirm", "Error loading CSV", e)
        }
        metadataMap = map
    }

    fun setInitialIngredients(list: List<FoodRecognitionUseCase.ProcessedIngredient>) {
        val domainList = list.map {
            ScanConfirmIngredient(
                name = it.name,
                weight = it.weight,
                calories = it.calories,
                protein = it.protein,
                fat = it.fat,
                carbs = it.carbs
            )
        }
        _uiState.update { it.copy(ingredients = domainList) }

        // Buscar imagens para todos os ingredientes iniciais
        viewModelScope.launch {
            val updatedIngredients = domainList.map { ingredient ->
                val imageUrl = fetchImageUrlForName(ingredient.name)
                ingredient.copy(imageUrl = imageUrl)
            }
            _uiState.update { it.copy(ingredients = updatedIngredients) }
        }
    }

    fun removeIngredient(id: String) {
        _uiState.update { state ->
            state.copy(ingredients = state.ingredients.filter { it.id != id })
        }
    }

    fun updateWeight(id: String, newWeight: Double) {
        _uiState.update { state ->
            val newList = state.ingredients.map { 
                if (it.id == id) {
                    val meta = metadataMap[it.name.lowercase().trim()]
                    if (meta != null) {
                        it.copy(
                            weight = newWeight,
                            calories = meta.calPerGram * newWeight,
                            protein = meta.proteinPerGram * newWeight,
                            fat = meta.fatPerGram * newWeight,
                            carbs = meta.carbPerGram * newWeight
                        )
                    } else {
                        it.copy(weight = newWeight)
                    }
                } else it
            }
            state.copy(ingredients = newList)
        }
    }

    private suspend fun fetchImageUrlForName(name: String): String? {
        return try {
            val searchResults = dietaRepository.searchFoods(name)
            searchResults.firstOrNull { !it.image.isNullOrEmpty() }?.image
        } catch (e: Exception) { null }
    }

    fun addIngredientFromFatSecret(
        food: FatSecretFoodDetails,
        serving: FatSecretServing,
        quantity: Double
    ) {
        val weight = (serving.metricAmount ?: 1.0) * quantity
        
        val newItem = ScanConfirmIngredient(
            name = food.nomeEn,
            weight = weight,
            calories = serving.calories * quantity,
            protein = serving.protein * quantity,
            fat = serving.fat * quantity,
            carbs = serving.carbs * quantity,
            imageUrl = food.image
        )
        _uiState.update { it.copy(ingredients = it.ingredients + newItem) }
        
        // Tentar buscar imagem se estiver vazia
        if (food.image.isNullOrEmpty()) {
            viewModelScope.launch {
                val url = fetchImageUrlForName(food.nomeEn)
                if (url != null) {
                    _uiState.update { state ->
                        state.copy(ingredients = state.ingredients.map { 
                            if (it.id == newItem.id) it.copy(imageUrl = url) else it 
                        })
                    }
                }
            }
        }
    }

    fun addIngredientFromSearch(name: String, calories100g: Double, protein100g: Double, fat100g: Double, carbs100g: Double) {
        viewModelScope.launch {
            val imageUrl = fetchImageUrlForName(name)
            val weight = 100.0
            val newItem = ScanConfirmIngredient(
                name = name,
                weight = weight,
                calories = (calories100g / 100.0) * weight,
                protein = (protein100g / 100.0) * weight,
                fat = (fat100g / 100.0) * weight,
                carbs = (carbs100g / 100.0) * weight,
                imageUrl = imageUrl
            )
            _uiState.update { currentState ->
                currentState.copy(ingredients = currentState.ingredients + newItem)
            }
        }
    }

    fun logMeal() {
        viewModelScope.launch {
            val totalCal = _uiState.value.ingredients.sumOf { it.calories }
            val totalProt = _uiState.value.ingredients.sumOf { it.protein }
            val totalFat = _uiState.value.ingredients.sumOf { it.fat }
            val totalCarbs = _uiState.value.ingredients.sumOf { it.carbs }

            userLocalRepository.checkAndResetDailyCaloriesAndMacros()
            userLocalRepository.addCaloriesEaten(totalCal.toInt())
            userLocalRepository.addDailyMacrosEaten(
                protein = totalProt,
                carbs = totalCarbs,
                fat = totalFat
            )
            _uiState.update { it.copy(isLogged = true) }
        }
    }
}
