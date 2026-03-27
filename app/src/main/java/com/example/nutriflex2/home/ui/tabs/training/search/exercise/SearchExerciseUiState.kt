package com.example.nutriflex2.home.ui.tabs.training.search.exercise

import com.example.treino.data.remote.ExerciseDbSummaryDto

data class SearchExerciseUiState(
    val searchQuery: String = "",
    val searchResults: List<ExerciseDbSummaryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Filters
    val selectedBodyPart: String? = null,
    val selectedEquipment: String? = null,
    val bodyParts: List<String> = SearchExerciseConstants.bodyPartFilters,
    val equipments: List<String> = SearchExerciseConstants.equipmentFilters
)
