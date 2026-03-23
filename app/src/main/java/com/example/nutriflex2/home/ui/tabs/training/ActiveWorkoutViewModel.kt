package com.example.nutriflex2.home.ui.tabs.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val sessionName: String = "",
    val exercises: List<ActiveExerciseModel> = emptyList(),
    val timerSeconds: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFinished: Boolean = false,
    val summary: WorkoutSummary? = null,
    
    // Rest Timer State
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
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
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

    private fun sendServiceCommand(action: String, extras: (android.content.Intent.() -> Unit)? = null) {
        val intent = android.content.Intent(context, WorkoutForegroundService::class.java).apply {
            this.action = action
            extras?.invoke(this)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sessionDetails = treinoRepository.getSessaoWithDetails(sessionId)
                
                val activeExercises = sessionDetails.exercicios.map { exDto ->
                    ActiveExerciseModel(
                        exercicio = Exercicio(
                            id = exDto.id,
                            exercicioApiId = exDto.exercicioApiId,
                            nome = exDto.nome ?: "Exercise",
                            notas = exDto.notas,
                            idSessao = sessionId,
                            ordem = exDto.ordem,
                            imagem = exDto.imagem,
                            bodypart = exDto.bodypart
                        ),
                        sets = exDto.sets.map { sDto ->
                            ExercicioSet(
                                id = sDto.id,
                                tipoSet = sDto.tipoSet,
                                peso = sDto.peso,
                                repeticoesMin = sDto.repeticoesMin,
                                repeticoesMax = sDto.repeticoesMax,
                                pesoUltimaVez = sDto.pesoUltimaVez,
                                repeticoesUltimaVez = sDto.repeticoesUltimaVez,
                                idExercicio = sDto.idExercicio,
                                ordem = sDto.ordem,
                                isChecked = false
                            )
                        }
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    sessionName = sessionDetails.nome,
                    exercises = activeExercises
                ) }
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

    // --- REST TIMER LOGIC ---
    
    fun startRestTimer(seconds: Int = 90) {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = true, restTimeLeft = seconds, initialRestTime = seconds) }
        
        // Obter o nome do próximo exercício para a notificação
        val currentExerciseIndex = _uiState.value.exercises.indexOfFirst { ex -> ex.sets.any { !it.isChecked } }
        val nextExerciseName = if (currentExerciseIndex != -1) {
            _uiState.value.exercises[currentExerciseIndex].exercicio.nome
        } else "Finishing Workout"

        sendServiceCommand(WorkoutForegroundService.ACTION_START_REST) {
            putExtra(WorkoutForegroundService.EXTRA_REST_SECONDS, seconds)
            putExtra(WorkoutForegroundService.EXTRA_NEXT_EXERCISE_NAME, nextExerciseName)
        }

        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restTimeLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(restTimeLeft = it.restTimeLeft - 1) }
            }
            _uiState.update { it.copy(isResting = false) }
        }
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = false, restTimeLeft = 0) }
        sendServiceCommand(WorkoutForegroundService.ACTION_SKIP_REST)
    }

    fun addRestTime(seconds: Int) {
        _uiState.update { it.copy(restTimeLeft = it.restTimeLeft + seconds) }
        if (seconds > 0) {
            sendServiceCommand(WorkoutForegroundService.ACTION_ADD_TIME)
        } else {
            sendServiceCommand(WorkoutForegroundService.ACTION_SUBTRACT_TIME)
        }
    }

    fun subtractRestTime(seconds: Int) {
        _uiState.update { 
            val newTime = (it.restTimeLeft - seconds).coerceAtLeast(0)
            it.copy(restTimeLeft = newTime)
        }
        sendServiceCommand(WorkoutForegroundService.ACTION_SUBTRACT_TIME)
    }

    // --- EXERCISE & SET MANAGEMENT (ISOLATED STATE) ---

    fun toggleSetChecked(exerciseIndex: Int, setIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            val exerciseModel = updatedExercises[exerciseIndex]
            val updatedSets = exerciseModel.sets.toMutableList()
            val set = updatedSets[setIndex]
            
            val newChecked = !set.isChecked
            updatedSets[setIndex] = set.copy(isChecked = newChecked)
            updatedExercises[exerciseIndex] = exerciseModel.copy(sets = updatedSets)
            
            // Trigger rest timer if checked
            if (newChecked) {
                startRestTimer(state.initialRestTime)
            } else {
                sendServiceCommand(WorkoutForegroundService.ACTION_STOP_REST)
            }
            
            state.copy(exercises = updatedExercises)
        }
    }

    fun updateSetValues(exerciseIndex: Int, setIndex: Int, weight: Double? = null, reps: Int? = null) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            val exerciseModel = updatedExercises[exerciseIndex]
            val updatedSets = exerciseModel.sets.toMutableList()
            val set = updatedSets[setIndex]
            
            if (!set.isChecked) {
                updatedSets[setIndex] = set.copy(
                    peso = weight ?: set.peso,
                    repeticoesMin = reps ?: set.repeticoesMin
                )
                updatedExercises[exerciseIndex] = exerciseModel.copy(sets = updatedSets)
                state.copy(exercises = updatedExercises)
            } else {
                state
            }
        }
    }

    fun addSet(exerciseIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            val exerciseModel = updatedExercises[exerciseIndex]
            val updatedSets = exerciseModel.sets.toMutableList()
            
            val lastSet = updatedSets.lastOrNull()
            val newSet = ExercicioSet(
                id = 0, // Temp ID
                tipoSet = lastSet?.tipoSet ?: "REGULAR",
                peso = lastSet?.peso ?: 0.0,
                repeticoesMin = lastSet?.repeticoesMin ?: 8,
                repeticoesMax = lastSet?.repeticoesMax ?: 12,
                pesoUltimaVez = 0.0,
                repeticoesUltimaVez = 0,
                idExercicio = exerciseModel.exercicio.id,
                ordem = updatedSets.size + 1,
                isChecked = false
            )
            
            updatedSets.add(newSet)
            updatedExercises[exerciseIndex] = exerciseModel.copy(sets = updatedSets)
            state.copy(exercises = updatedExercises)
        }
    }

    fun removeExercise(exerciseIndex: Int) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex in updatedExercises.indices) {
                updatedExercises.removeAt(exerciseIndex)
            }
            state.copy(exercises = updatedExercises)
        }
    }

    fun replaceExercise(exerciseIndex: Int, newExercise: Exercicio) {
        _uiState.update { state ->
            val updatedExercises = state.exercises.toMutableList()
            if (exerciseIndex in updatedExercises.indices) {
                updatedExercises[exerciseIndex] = ActiveExerciseModel(
                    exercicio = newExercise,
                    sets = listOf(
                        ExercicioSet(
                            id = 0,
                            tipoSet = "REGULAR",
                            peso = 0.0,
                            repeticoesMin = 8,
                            repeticoesMax = 12,
                            pesoUltimaVez = 0.0,
                            repeticoesUltimaVez = 0,
                            idExercicio = newExercise.id,
                            ordem = 1,
                            isChecked = false
                        )
                    )
                )
            }
            state.copy(exercises = updatedExercises)
        }
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

    fun finishWorkout() {
        timerJob?.cancel()
        restTimerJob?.cancel()
        sendServiceCommand(WorkoutForegroundService.ACTION_STOP_WORKOUT)
        val state = _uiState.value
        
        val durationFormatted = formatTime(state.timerSeconds)
        var totalVolume = 0.0
        var completedSets = 0
        
        viewModelScope.launch {
            state.exercises.forEach { ex ->
                ex.sets.forEach { set ->
                    if (set.isChecked) {
                        totalVolume += (set.peso * set.repeticoesMin)
                        completedSets++
                        // Atualizar histórico na DB local
                        treinoRepository.updateSetHistory(set.id, set.peso, set.repeticoesMin)
                    }
                }
            }
            
            val summary = WorkoutSummary(
                duration = durationFormatted,
                totalVolume = totalVolume,
                completedSets = completedSets
            )
            
            _uiState.update { it.copy(isFinished = true, summary = summary, isResting = false) }
        }
    }

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    fun formatRestTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }
}
