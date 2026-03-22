package com.example.nutriflex2.home.ui.tabs.diet

import android.net.Uri

data class ScanMealUiState(
    val recentImages: List<Uri> = emptyList(),
    val albums: Map<String, List<Uri>> = emptyMap(),
    val selectedAlbum: String = "All",
    val isLoading: Boolean = false,
    val capturedImageUri: Uri? = null,
    val error: String? = null,
    val successMessage: String? = null
)
