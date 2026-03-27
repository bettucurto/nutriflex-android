package com.example.nutriflex2.home.ui.tabs.diet

import android.net.Uri

import com.example.dieta.domain.FoodRecognitionUseCase

data class ScanMealUiState(
    val recentImages: List<Uri> = emptyList(),
    val albums: Map<String, List<Uri>> = emptyMap(),
    val selectedAlbum: String = "All",
    val isLoading: Boolean = false,
    val capturedImageUri: Uri? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val detectedIngredients: List<FoodRecognitionUseCase.ProcessedIngredient>? = null
)
