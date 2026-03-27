package com.example.nutriflex2.home.ui.tabs.training

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val sessionName: String = "",
    val nextSessaoId: Int? = null,
    val exercises: List<ActiveExerciseModel> = emptyList(),
    val timerSeconds: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFinished: Boolean = false,
    val summary: WorkoutSummary? = null,
    val isResting: Boolean = false,
    val restTimeLeft: Int = 0,
    val initialRestTime: Int = 90
)

data class ActiveExerciseModel(
    val exercicio: Exercicio,
    val sets: List<ExercicioSet>
)

data class WorkoutSummary(
    val duration: String,
    val totalVolume: Double,
    val completedSets: Int
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository,
    private val userLocalRepository: local.UserLocalRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])
    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        loadSession()
        startTimer()
        sendServiceCommand(WorkoutForegroundService.ACTION_START_WORKOUT)
    }

    private fun updateServiceWithActiveSet() {
        val state = _uiState.value
        val activeExIndex = state.exercises.indexOfFirst { ex -> ex.sets.any { !it.isChecked } }
        if (activeExIndex != -1) {
            val activeEx = state.exercises[activeExIndex]
            val activeSetIndex = activeEx.sets.indexOfFirst { !it.isChecked }
            val activeSet = activeEx.sets[activeSetIndex]
            
            sendServiceCommand(WorkoutForegroundService.ACTION_UPDATE_ACTIVE_SET) {
                putExtra(WorkoutForegroundService.EXTRA_EXERCISE_NAME, activeEx.exercicio.nome)
                putExtra(WorkoutForegroundService.EXTRA_CURRENT_SET, activeSetIndex + 1)
                putExtra(WorkoutForegroundService.EXTRA_TOTAL_SETS, activeEx.sets.size)
                putExtra(WorkoutForegroundService.EXTRA_REPS_INFO, "${activeSet.repeticoesMin}-${activeSet.repeticoesMax}")
                putExtra(WorkoutForegroundService.EXTRA_IMAGE_URL, activeEx.exercicio.imagem)
                putExtra("EXTRA_EX_INDEX", activeExIndex)
                putExtra("EXTRA_SET_INDEX", activeSetIndex)
            }
        }
    }

    private fun sendServiceCommand(action: String, extras: (Intent.() -> Unit)? = null) {
        val intent = Intent(context, WorkoutForegroundService::class.java).apply {
            this.action = action
            extras?.invoke(this)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sessionDetails = treinoRepository.getSessaoWithDetails(sessionId)
                val activeExercises = sessionDetails.exercicios.map { exDto ->
                    ActiveExerciseModel(
                        exercicio = Exercicio(exDto.id, exDto.exercicioApiId, exDto.nome ?: "Exercise", exDto.notas, sessionId, exDto.ordem, exDto.imagem, exDto.bodypart),
                        sets = exDto.sets.map { sDto ->
                            ExercicioSet(
                                sDto.id, 
                                sDto.tipoSet, 
                                sDto.peso, 
                                sDto.repeticoesMin, 
                                sDto.repeticoesMax, 
                                sDto.pesoUltimaVez ?: 0.0, 
                                sDto.repeticoesUltimaVez ?: 0, 
                                sDto.idExercicio, 
                                sDto.ordem, 
                                false
                            )
                        }
                    )
                }
                _uiState.update { it.copy(isLoading = false, sessionName = sessionDetails.nome, nextSessaoId = sessionDetails.idProximaSessao, exercises = activeExercises) }
                updateServiceWithActiveSet()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds + 1) }
            }
        }
    }

    fun startRestTimer(seconds: Int = 90, nextExName: String? = null) {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = true, restTimeLeft = seconds) }
        
        val finalNextExName = nextExName ?: run {
            val nextExIndex = _uiState.value.exercises.indexOfFirst { ex -> ex.sets.any { !it.isChecked } }
            if (nextExIndex != -1) _uiState.value.exercises[nextExIndex].exercicio.nome else "Finish"
        }

        sendServiceCommand(WorkoutForegroundService.ACTION_START_REST) {
            putExtra(WorkoutForegroundService.EXTRA_REST_SECONDS, seconds)
            putExtra(WorkoutForegroundService.EXTRA_NEXT_EXERCISE_NAME, finalNextExName)
        }

        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restTimeLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(restTimeLeft = it.restTimeLeft - 1) }
            }
            _uiState.update { it.copy(isResting = false) }
            updateServiceWithActiveSet()
        }
    }

    fun toggleSetChecked(exerciseIndex: Int, setIndex: Int) {
        var shouldStartRest = false
        var calculatedNextExName: String? = null
        
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex !in updatedExercises.indices) return@update state
            
            val exModel = updatedExercises[exerciseIndex]
            val updatedSets = exModel.sets.toMutableList()
            if (setIndex !in updatedSets.indices) return@update state
            
            val set = updatedSets[setIndex]
            val newChecked = !set.isChecked
            updatedSets[setIndex] = set.copy(isChecked = newChecked)
            updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
            
            if (newChecked) {
                shouldStartRest = true
                val nextExIndex = updatedExercises.indexOfFirst { ex -> ex.sets.any { !it.isChecked } }
                calculatedNextExName = if (nextExIndex != -1) updatedExercises[nextExIndex].exercicio.nome else "Finish"
            } else {
                sendServiceCommand(WorkoutForegroundService.ACTION_STOP_REST)
            }
            state.copy(exercises = updatedExercises)
        }
        
        if (shouldStartRest) {
            startRestTimer(_uiState.value.initialRestTime, calculatedNextExName)
        }
        updateServiceWithActiveSet()
    }

    fun toggleSetType(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex !in updatedExercises.indices) return@update state
            val exModel = updatedExercises[exerciseIndex]
            val updatedSets = exModel.sets.toMutableList()
            if (setIndex !in updatedSets.indices) return@update state
            val set = updatedSets[setIndex]
            if (!set.isChecked) {
                val nextType = when (set.tipoSet) {
                    "REGULAR" -> "WARMUP"
                    "WARMUP" -> "DROP"
                    "DROP" -> "FAILURE"
                    else -> "REGULAR"
                }
                updatedSets[setIndex] = set.copy(tipoSet = nextType)
                updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            } else state
        }
        updateServiceWithActiveSet()
    }

    fun toggleSetTypeExplicit(exerciseIndex: Int, setIndex: Int, newType: String) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex in updatedExercises.indices) {
                val exModel = updatedExercises[exerciseIndex]
                val updatedSets = exModel.sets.toMutableList()
                if (setIndex in updatedSets.indices && !updatedSets[setIndex].isChecked) {
                    updatedSets[setIndex] = updatedSets[setIndex].copy(tipoSet = newType)
                    updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
                    state.copy(exercises = updatedExercises)
                } else state
            } else state
        }
        updateServiceWithActiveSet()
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex !in updatedExercises.indices) return@update state
            val exModel = updatedExercises[exerciseIndex]
            val updatedSets = exModel.sets.toMutableList()
            if (setIndex in updatedSets.indices) {
                updatedSets.removeAt(setIndex)
                updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
            }
            state.copy(exercises = updatedExercises)
        }
        updateServiceWithActiveSet()
    }

    fun removeExercise(exerciseIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex in updatedExercises.indices) {
                updatedExercises.removeAt(exerciseIndex)
            }
            state.copy(exercises = updatedExercises)
        }
        updateServiceWithActiveSet()
    }

    fun updateExerciseNotes(exerciseIndex: Int, notes: String) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex in updatedExercises.indices) {
                val exerciseModel = updatedExercises[exerciseIndex]
                updatedExercises[exerciseIndex] = exerciseModel.copy(
                    exercicio = exerciseModel.exercicio.copy(notas = notes)
                )
            }
            state.copy(exercises = updatedExercises)
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = false, restTimeLeft = 0) }
        sendServiceCommand(WorkoutForegroundService.ACTION_SKIP_REST)
        updateServiceWithActiveSet()
    }

    fun skipRestFromService() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = false, restTimeLeft = 0) }
        updateServiceWithActiveSet()
    }

    fun addRestTime(seconds: Int) {
        _uiState.update { it.copy(restTimeLeft = it.restTimeLeft + seconds) }
        sendServiceCommand(WorkoutForegroundService.ACTION_ADD_TIME)
    }

    fun subtractRestTime(seconds: Int) {
        _uiState.update { it.copy(restTimeLeft = (it.restTimeLeft - seconds).coerceAtLeast(0)) }
        sendServiceCommand(WorkoutForegroundService.ACTION_SUBTRACT_TIME)
    }

    fun adjustRestTimeFromService(seconds: Int) {
        _uiState.update { it.copy(restTimeLeft = (it.restTimeLeft + seconds).coerceAtLeast(0)) }
    }

    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, weight: Double?) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex !in updatedExercises.indices) return@update state
            val exModel = updatedExercises[exerciseIndex]
            val updatedSets = exModel.sets.toMutableList()
            if (setIndex !in updatedSets.indices) return@update state
            val set = updatedSets[setIndex]
            if (!set.isChecked) {
                updatedSets[setIndex] = set.copy(peso = weight ?: 0.0)
                updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            } else state
        }
    }

    fun updateSetReps(exerciseIndex: Int, setIndex: Int, reps: Int?) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex !in updatedExercises.indices) return@update state
            val exModel = updatedExercises[exerciseIndex]
            val updatedSets = exModel.sets.toMutableList()
            if (setIndex !in updatedSets.indices) return@update state
            val set = updatedSets[setIndex]
            if (!set.isChecked) {
                updatedSets[setIndex] = set.copy(repeticoesMin = reps ?: 0)
                updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            } else state
        }
    }

    fun addSet(exerciseIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            val exModel = updatedExercises[exerciseIndex]
            val updatedSets = exModel.sets.toMutableList()
            val lastSet = updatedSets.lastOrNull()
            updatedSets.add(ExercicioSet(0, lastSet?.tipoSet ?: "REGULAR", lastSet?.peso ?: 0.0, lastSet?.repeticoesMin ?: 8, lastSet?.repeticoesMax ?: 12, 0.0, 0, exModel.exercicio.id, updatedSets.size + 1, false))
            updatedExercises[exerciseIndex] = exModel.copy(sets = updatedSets)
            state.copy(exercises = updatedExercises)
        }
    }

    fun replaceExercise(index: Int, newExercise: com.example.treino.domain.models.Exercicio) {
        _uiState.update { currentState ->
            val updatedExercises = currentState.exercises.toMutableList()
            if (index in updatedExercises.indices) {
                val existingActiveEx = updatedExercises[index]
                updatedExercises[index] = existingActiveEx.copy(exercicio = newExercise)
            }
            currentState.copy(exercises = updatedExercises)
        }
    }

    fun finishWorkout() {
        timerJob?.cancel()
        restTimerJob?.cancel()
        sendServiceCommand(WorkoutForegroundService.ACTION_STOP_WORKOUT)
        val state = _uiState.value
        viewModelScope.launch {
            var totalVolume = 0.0
            var completedSets = 0
            state.exercises.forEach { ex ->
                ex.sets.forEach { set ->
                    if (set.isChecked) {
                        totalVolume += (set.peso * set.repeticoesMin)
                        completedSets++
                        treinoRepository.updateSetHistory(set.id, set.peso.toDouble(), set.repeticoesMin)
                    }
                }
            }
            try {
                val sessionDetails = treinoRepository.getSessaoWithDetails(sessionId)
                if (sessionDetails.idProximaSessao > 0) {
                    val user = userLocalRepository.getUserLocal().firstOrNull()
                    if (user != null) {
                        userLocalRepository.updateNextWorkoutId(user.userId, sessionDetails.idProximaSessao)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _uiState.update { it.copy(isFinished = true, summary = WorkoutSummary(formatTime(state.timerSeconds), totalVolume, completedSets), isResting = false) }
        }
    }

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }

    fun formatRestTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }
}
