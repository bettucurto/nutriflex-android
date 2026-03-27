package com.example.nutriflex2.home.ui.tabs.training.info

import com.example.treino.data.remote.ExerciseDbSummaryDto

sealed class ExerciseInfoUiState {
    object Loading : ExerciseInfoUiState()
    data class Success(val exercise: ExerciseDbSummaryDto) : ExerciseInfoUiState()
    data class Error(val message: String) : ExerciseInfoUiState()
}
