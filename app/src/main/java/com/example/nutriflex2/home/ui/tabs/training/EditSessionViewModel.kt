package com.example.nutriflex2.home.ui.tabs.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.data.remote.CreateSessaoRequest
import com.example.treino.data.remote.SessionExerciseRequest
import com.example.treino.data.remote.SessionSetRequest
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditSessionViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])
    
    private val _uiState = MutableStateFlow(CreateSessionUiState())
    val uiState: StateFlow<CreateSessionUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sessionDto = treinoRepository.getSessaoWithDetails(sessionId)
                
                val mappedExercises = sessionDto.exercicios.map { exDto ->
                    ExerciseUiModel(
                        id = exDto.exercicioApiId,
                        name = exDto.nome ?: "Exercise",
                        muscleGroup = exDto.bodypart ?: "",
                        imageUrl = exDto.imagem,
                        notes = exDto.notas,
                        sets = exDto.sets.map { setDto ->
                            SetUiModel(
                                id = setDto.id,
                                weightKg = setDto.peso,
                                repsMin = setDto.repeticoesMin,
                                repsMax = setDto.repeticoesMax,
                                type = try { SetType.valueOf(setDto.tipoSet) } catch(e: Exception) { SetType.REGULAR },
                                isActive = true
                            )
                        }
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    workoutName = sessionDto.nome,
                    exercises = mappedExercises,
                    folderId = sessionDto.idPasta
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onWorkoutNameChange(name: String) {
        _uiState.update { it.copy(workoutName = name) }
    }

    fun onSelectExercise(index: Int) {
        _uiState.update { it.copy(selectedExerciseIndex = index) }
    }

    fun addExercise(id: String, name: String, bodyPart: String, imageUrl: String? = null) {
        _uiState.update { state ->
            val newExercise = ExerciseUiModel(
                id = id,
                name = name,
                muscleGroup = bodyPart,
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
        addExercise(parts[0], parts[1], parts[2], parts[3])
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            val newList = state.exercises.toMutableList()
            if (index in newList.indices) newList.removeAt(index)
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
            if (index in newList.indices) newList[index] = newList[index].copy(notes = notes)
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
            currentExercises[exerciseIndex] = currentExercise.copy(sets = currentExercise.sets + newSet)
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
            if (currentSets.size > 1 && setIndex in currentSets.indices) {
                currentSets.removeAt(setIndex)
                currentExercises[exerciseIndex] = currentExercise.copy(sets = currentSets)
                state.copy(exercises = currentExercises)
            } else state
        }
    }

    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: Double?) {
        _uiState.update { state ->
            val currentExercises = state.exercises.toMutableList()
            if (exerciseIndex !in currentExercises.indices) return@update state
            val currentExercise = currentExercises[exerciseIndex]
            val currentSets = currentExercise.sets.toMutableList()
            if (setIndex in currentSets.indices) {
                currentSets[setIndex] = currentSets[setIndex].copy(weightKg = weight ?: 0.0)
                currentExercises[exerciseIndex] = currentExercise.copy(sets = currentSets)
                state.copy(exercises = currentExercises)
            } else state
        }
    }

    fun updateSetRepsMin(exerciseIndex: Int, setIndex: Int, reps: Int?) {
        _uiState.update { state ->
            val currentExercises = state.exercises.toMutableList()
            if (exerciseIndex !in currentExercises.indices) return@update state
            val currentExercise = currentExercises[exerciseIndex]
            val currentSets = currentExercise.sets.toMutableList()
            if (setIndex in currentSets.indices) {
                currentSets[setIndex] = currentSets[setIndex].copy(repsMin = reps ?: 0)
                currentExercises[exerciseIndex] = currentExercise.copy(sets = currentSets)
                state.copy(exercises = currentExercises)
            } else state
        }
    }

    fun updateSetRepsMax(exerciseIndex: Int, setIndex: Int, reps: Int?) {
        _uiState.update { state ->
            val currentExercises = state.exercises.toMutableList()
            if (exerciseIndex !in currentExercises.indices) return@update state
            val currentExercise = currentExercises[exerciseIndex]
            val currentSets = currentExercise.sets.toMutableList()
            if (setIndex in currentSets.indices) {
                currentSets[setIndex] = currentSets[setIndex].copy(repsMax = reps ?: 0)
                currentExercises[exerciseIndex] = currentExercise.copy(sets = currentSets)
                state.copy(exercises = currentExercises)
            } else state
        }
    }

    fun updateSetType(exerciseIndex: Int, setIndex: Int, newType: SetType) {
        _uiState.update { state ->
            val currentExercises = state.exercises.toMutableList()
            if (exerciseIndex in currentExercises.indices) {
                val targetExercise = currentExercises[exerciseIndex]
                val updatedSets = targetExercise.sets.toMutableList()
                if (setIndex in updatedSets.indices) {
                    updatedSets[setIndex] = updatedSets[setIndex].copy(type = newType)
                    currentExercises[exerciseIndex] = targetExercise.copy(sets = updatedSets)
                    state.copy(exercises = currentExercises)
                } else state
            } else state
        }
    }

    fun updateSession() {
        val state = _uiState.value
        if (state.workoutName.isBlank() || state.exercises.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val exerciciosPayload = state.exercises.mapIndexed { index, exercise ->
                    SessionExerciseRequest(
                        idExercicio = exercise.id,
                        nome = exercise.name, // Adicionado o nome
                        notas = exercise.notes,
                        ordem = index + 1,
                        imagem = exercise.imageUrl,
                        bodypart = exercise.muscleGroup,
                        sets = exercise.sets.mapIndexed { sIndex, set ->
                            SessionSetRequest(
                                tipoSet = set.type.name,
                                peso = set.weightKg,
                                repeticoesMin = set.repsMin,
                                repeticoesMax = set.repsMax,
                                ordem = sIndex + 1
                            )
                        }
                    )
                }

                val request = CreateSessaoRequest(
                    nome = state.workoutName,
                    idPasta = state.folderId,
                    exercicios = exerciciosPayload
                )

                treinoRepository.updateSessao(sessionId, request)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
