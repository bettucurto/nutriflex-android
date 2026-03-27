package com.example.nutriflex2.home.ui.tabs.training.edit

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutriflex2.home.ui.tabs.training.EmptySessionContent
import com.example.nutriflex2.home.ui.tabs.training.ExerciseDetailCard
import com.example.nutriflex2.home.ui.tabs.training.FinishWorkoutButton
import com.example.nutriflex2.home.ui.tabs.training.SessionExercisesRow
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    onBack: () -> Unit,
    navController: NavController? = null,
    viewModel: CreateSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showSetTypeSheet by remember { mutableStateOf(false) }
    var setIndexToEdit by remember { mutableStateOf(-1) }

    val entry = navController?.currentBackStackEntry
    val resultState = entry?.savedStateHandle?.getStateFlow<String?>("selected_exercise_data", null)?.collectAsState()
    val result = resultState?.value

    LaunchedEffect(result) {
        if (result != null) {
            viewModel.onExerciseSelected(result)
            entry?.savedStateHandle?.remove<String>("selected_exercise_data")
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Create Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FinishWorkoutButton(
                onClick = {
                    if (state.workoutName.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please fill in the session name",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.saveWorkout()
                    }
                },
                visible = state.exercises.isNotEmpty()
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WORKOUT NAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                        BasicTextField(
                            value = state.workoutName,
                            onValueChange = viewModel::onWorkoutNameChange,
                            modifier = Modifier.padding(16.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                            decorationBox = { innerTextField ->
                                if (state.workoutName.isEmpty()) Text("Morning Power Session", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 18.sp)
                                innerTextField()
                            }
                        )
                    }
                }
            }

            if (state.exercises.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("SESSION EXERCISES", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        SessionExercisesRow(
                            exercises = state.exercises,
                            selectedIndex = state.selectedExerciseIndex,
                            onSelect = viewModel::onSelectExercise,
                            onAddNew = { navController?.navigate("search_exercise/-1?isReplacement=false") }
                        )
                    }
                }
                item {
                    val selectedEx = state.exercises[state.selectedExerciseIndex]
                    ExerciseDetailCard(
                        exercise = selectedEx,
                        onNotesChange = {
                            viewModel.onExerciseNotesChange(
                                state.selectedExerciseIndex,
                                it
                            )
                        },
                        onAddSet = { viewModel.addSetToSelectedExercise() },
                        onUpdateSet = { setIndex, w, rMin, rMax ->
                            if (w != null) viewModel.updateSetWeight(
                                state.selectedExerciseIndex,
                                setIndex,
                                w
                            )
                            if (rMin != null) viewModel.updateSetRepsMin(
                                state.selectedExerciseIndex,
                                setIndex,
                                rMin
                            )
                            if (rMax != null) viewModel.updateSetRepsMax(
                                state.selectedExerciseIndex,
                                setIndex,
                                rMax
                            )
                        },
                        onRemoveExercise = { viewModel.removeExercise(state.selectedExerciseIndex) },
                        onSetClick = { index ->
                            setIndexToEdit = index
                            showSetTypeSheet = true
                        },
                        onRemoveSet = { index -> viewModel.removeSetFromSelectedExercise(index) },
                        onExerciseClick = { navController?.navigate("exercise_info/${selectedEx.id}?isAddingMode=false") }
                    )
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        EmptySessionContent(
                            onAddFirstExercise = { navController?.navigate("search_exercise/-1?isReplacement=false") },
                            onViewWorkouts = { navController?.navigate("workout_browser") }
                        )
                    }
                }
            }
        }
    }

    if (showSetTypeSheet) {
        ModalBottomSheet(onDismissRequest = { showSetTypeSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Set Type", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ListItem(headlineContent = { Text("Regular Set") }, leadingContent = { Icon(Icons.Default.FormatListNumbered, null) }, modifier = Modifier.clickable {
                    viewModel.updateSetType(state.selectedExerciseIndex, setIndexToEdit, SetType.REGULAR)
                    showSetTypeSheet = false
                })
                ListItem(headlineContent = { Text("Warmup Set") }, leadingContent = { Icon(Icons.Default.Waves, null, tint = Color(0xFF03A9F4)) }, modifier = Modifier.clickable {
                    viewModel.updateSetType(state.selectedExerciseIndex, setIndexToEdit, SetType.WARMUP)
                    showSetTypeSheet = false
                })
            }
        }
    }
}
