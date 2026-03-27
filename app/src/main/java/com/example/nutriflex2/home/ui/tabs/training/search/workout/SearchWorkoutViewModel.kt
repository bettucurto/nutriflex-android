package com.example.nutriflex2.home.ui.tabs.training.search.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

data class SearchWorkoutUiState(
    val workouts: List<Pasta> = emptyList(),
    val selectedDifficulty: Int? = null,
    val selectedFrequency: Int? = null,
    val isLoading: Boolean = false,
    val isDuplicating: Boolean = false,
    val actionFinished: Boolean = false
)

@HiltViewModel
class SearchWorkoutViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchWorkoutUiState())
    val uiState: StateFlow<SearchWorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value
                val response = treinoRepository.getPublicWorkouts(
                    frequency = state.selectedFrequency,
                    experience = state.selectedDifficulty
                )
                _uiState.update { it.copy(workouts = response, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onDifficultyFilterSelected(level: Int) {
        _uiState.update { it.copy(selectedDifficulty = if (it.selectedDifficulty == level) null else level) }
        loadWorkouts()
    }

    fun onFrequencyFilterSelected(days: Int) {
        _uiState.update { it.copy(selectedFrequency = if (it.selectedFrequency == days) null else days) }
        loadWorkouts()
    }

    fun duplicateWorkout(pastaId: Int) {
        if (_uiState.value.isDuplicating) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isDuplicating = true) }
            try {
                val user = userLocalRepository.getUserLocal().firstOrNull()
                user?.let {
                    treinoRepository.duplicatePastaGlobal(pastaId, it.userId)
                    _uiState.update { it.copy(actionFinished = true) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isDuplicating = false) }
            }
        }
    }

    fun resetAction() {
        _uiState.update { it.copy(actionFinished = false) }
    }
}
