package com.example.nutriflex2.home.ui.tabs.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSessionViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderId: Int = checkNotNull(savedStateHandle["folderId"])
    
    private val _uiState = MutableStateFlow(CreateSessionUiState(folderId = folderId))
    val uiState: StateFlow<CreateSessionUiState> = _uiState.asStateFlow()

    fun onWorkoutNameChange(name: String) {
        _uiState.update { it.copy(workoutName = name) }
    }

    fun onSelectExercise(index: Int) {
        _uiState.update { it.copy(selectedExerciseIndex = index) }
    }

    fun addExercise(name: String, muscleGroup: String, imageUrl: String? = null) {
        _uiState.update { state ->
            val newExercise = ExerciseUiModel(
                name = name,
                muscleGroup = muscleGroup,
                imageUrl = imageUrl,
                sets = listOf(SetUiModel(repsMin = 8, repsMax = 12, weightKg = 0.0, isActive = true))
            )
            state.copy(
                exercises = state.exercises + newExercise,
                selectedExerciseIndex = state.exercises.size
            )
        }
    }

    fun onExerciseSelected(data: String) {
        val parts = data.split("|")
        if (parts.size < 4) return
        val id = parts[0]
        val name = parts[1]
        val bodyPart = parts[2]
        val imageUrl = parts[3]
        
        addExercise(name, bodyPart, imageUrl)
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList()
            newList.removeAt(index)
            state.copy(
                exercises = newList,
                selectedExerciseIndex = if (state.selectedExerciseIndex >= newList.size) {
                    (newList.size - 1).coerceAtLeast(0)
                } else state.selectedExerciseIndex
            )
        }
    }

    fun onExerciseNotesChange(index: Int, notes: String) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList()
            newList[index] = newList[index].copy(notes = notes)
            state.copy(exercises = newList)
        }
    }

    fun addSetToSelectedExercise() {
        _uiState.update { state ->
            val exerciseIndex = state.selectedExerciseIndex
            if (exerciseIndex !in state.exercises.indices) return@update state

            val currentExercises = state.exercises.toMutableList()
            val currentExercise = currentExercises[exerciseIndex]
            
            val lastSet = currentExercise.sets.lastOrNull()
            val newSet = SetUiModel(
                repsMin = lastSet?.repsMin ?: 8,
                repsMax = lastSet?.repsMax ?: 12,
                weightKg = lastSet?.weightKg ?: 0.0,
                isActive = true
            )

            currentExercises[exerciseIndex] = currentExercise.copy(
                sets = currentExercise.sets + newSet
            )
            state.copy(exercises = currentExercises)
        }
    }

    fun removeSetFromSelectedExercise(setIndex: Int) {
        _uiState.update { state ->
            val exerciseIndex = state.selectedExerciseIndex
            if (exerciseIndex !in state.exercises.indices) return@update state

            val currentExercises = state.exercises.toMutableList()
            val currentExercise = currentExercises[exerciseIndex]
            val currentSets = currentExercise.sets.toMutableList()
            
            if (currentSets.size > 1) {
                currentSets.removeAt(setIndex)
                currentExercises[exerciseIndex] = currentExercise.copy(sets = currentSets)
                state.copy(exercises = currentExercises)
            } else state
        }
    }

    fun updateSetValues(exerciseIndex: Int, setIndex: Int, weight: Double? = null, repsMin: Int? = null, repsMax: Int? = null) {
        _uiState.update { state ->
            val currentExercises = state.exercises.toMutableList()
            val currentExercise = currentExercises[exerciseIndex]
            val currentSets = currentExercise.sets.toMutableList()
            
            val set = currentSets[setIndex]
            currentSets[setIndex] = set.copy(
                weightKg = weight ?: set.weightKg,
                repsMin = repsMin ?: set.repsMin,
                repsMax = repsMax ?: set.repsMax
            )
            
            currentExercises[exerciseIndex] = currentExercise.copy(sets = currentSets)
            state.copy(exercises = currentExercises)
        }
    }

    fun saveWorkout() {
        val state = _uiState.value
        if (state.workoutName.isBlank() || state.exercises.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Criar a Sessão
                val sessaoId = treinoRepository.createSessao(state.workoutName, state.folderId)
                
                // 2. Criar Exercícios e Sets (Simplificado para o fluxo local)
                // Nota: O repositório/API precisaria de um batch insert para ser eficiente,
                // mas vamos seguir o fluxo sequencial definido no TreinoRepository por agora.
                state.exercises.forEachIndexed { index, exerciseUi ->
                    // Aqui precisaríamos do ID da API do exercício. 
                    // Como é um mock/placeholder no ecrã de criação por agora:
                    val exId = treinoRepository.createExercicio(
                        exercicioApiId = "placeholder_id", // TODO: Integrar com Search
                        notas = exerciseUi.notes,
                        idSessao = sessaoId
                    )
                    
                    // TODO: Implementar salvaguarda dos Sets na API se suportado
                    // Atualmente o repository tem createSet(request: CreateSetRequest)
                }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
