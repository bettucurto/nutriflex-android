package com.example.nutriflex2.home.ui.tabs.training.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseInfoViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    private val _uiState = MutableStateFlow<ExerciseInfoUiState>(ExerciseInfoUiState.Loading)
    val uiState: StateFlow<ExerciseInfoUiState> = _uiState.asStateFlow()

    init {
        fetchExerciseDetails()
    }

    private fun fetchExerciseDetails() {
        viewModelScope.launch {
            _uiState.value = ExerciseInfoUiState.Loading
            try {
                val response = treinoRepository.getExerciseDetails(exerciseId)
                if (response.success) {
                    _uiState.value = ExerciseInfoUiState.Success(response.data)
                } else {
                    _uiState.value = ExerciseInfoUiState.Error("Failed to load exercise details")
                }
            } catch (e: Exception) {
                _uiState.value = ExerciseInfoUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
