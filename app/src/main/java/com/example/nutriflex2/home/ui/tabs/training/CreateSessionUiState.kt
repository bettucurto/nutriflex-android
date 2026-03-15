package com.example.nutriflex2.home.ui.tabs.training

data class CreateSessionUiState(
    val folderId: Int = 0,
    val workoutName: String = "",
    val exercises: List<ExerciseUiModel> = emptyList(),
    val selectedExerciseIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

data class ExerciseUiModel(
    val id: Int = 0, // 0 para novos
    val name: String = "",
    val muscleGroup: String = "",
    val imageUrl: String? = null,
    val notes: String = "",
    val sets: List<SetUiModel> = emptyList()
)

data class SetUiModel(
    val id: Int = 0,
    val repsMin: Int = 0,
    val repsMax: Int = 0,
    val weightKg: Double = 0.0,
    val isActive: Boolean = false
)
