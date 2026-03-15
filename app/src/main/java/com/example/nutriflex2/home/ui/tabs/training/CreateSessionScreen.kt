package com.example.nutriflex2.home.ui.tabs.training

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    onBack: () -> Unit,
    navController: NavController? = null,
    viewModel: CreateSessionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Observar retorno do SearchExercise via SavedStateHandle
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
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveWorkout() },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.padding(16.dp),
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text("Save Workout", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item {
                WorkoutNameInput(
                    name = state.workoutName,
                    onNameChange = viewModel::onWorkoutNameChange
                )
            }

            if (state.exercises.isEmpty()) {
                item {
                    EmptySessionContent(
                        onAddFirstExercise = {
                            navController?.navigate("search_exercise")
                        }
                    )
                }
            } else {
                item {
                    ExerciseSelectorRow(
                        exercises = state.exercises,
                        selectedIndex = state.selectedExerciseIndex,
                        onSelect = viewModel::onSelectExercise,
                        onAddNew = {
                            navController?.navigate("search_exercise")
                        }
                    )
                }

                val selectedEx = state.exercises[state.selectedExerciseIndex]
                item {
                    ActiveExerciseCard(
                        exercise = selectedEx,
                        onNotesChange = { viewModel.onExerciseNotesChange(state.selectedExerciseIndex, it) }
                    )
                }

                item {
                    SetsAndRepsTable(
                        sets = selectedEx.sets,
                        onUpdateSet = { setIndex, weight, min, max ->
                            viewModel.updateSetValues(state.selectedExerciseIndex, setIndex, weight, min, max)
                        },
                        onRemoveSet = { viewModel.removeSetFromSelectedExercise(it) }
                    )
                }

                item {
                    AddSetButton(onClick = { viewModel.addSetToSelectedExercise() })
                }
            }
        }
    }
}

@Composable
private fun EmptySessionContent(onAddFirstExercise: () -> Unit) {
    val primaryGreen = colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícone Central com Brilho e Sombra
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryGreen.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = primaryGreen
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Your session is empty",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "Ready to crush it? Add your first exercise to start building your custom routine.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Primary Action
        Button(
            onClick = onAddFirstExercise,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Exercise", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Action (Ghost Button)
        OutlinedButton(
            onClick = { /* TODO: Navegar para templates */ },
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Text(
                "Browse Templates",
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun WorkoutNameInput(name: String, onNameChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "WORKOUT NAME",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("e.g., Monday Push Day", fontSize = 24.sp, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun ExerciseSelectorRow(
    exercises: List<ExerciseUiModel>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddNew: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(exercises) { index, exercise ->
            val isSelected = index == selectedIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelect(index) }
                    .padding(4.dp)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    AsyncImage(
                        model = exercise.imageUrl ?: "https://via.placeholder.com/150",
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(
                                BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Indicador Numérico
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                if (isSelected) colorScheme.primary else Color.White,
                                CircleShape
                            )
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    exercise.name,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) colorScheme.primary else Color.Gray
                )
            }
        }
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAddNew() }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(1.dp, colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "New",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ActiveExerciseCard(exercise: ExerciseUiModel, onNotesChange: (String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = exercise.imageUrl ?: "https://via.placeholder.com/150",
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(exercise.muscleGroup, color = Color.Gray, fontSize = 14.sp)
                }
                IconButton(onClick = { /* Settings */ }) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = exercise.notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add notes here...", color = Color.LightGray) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                    focusedBorderColor = colorScheme.primary.copy(alpha = 0.5f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        }
    }
}

@Composable
private fun SetsAndRepsTable(
    sets: List<SetUiModel>,
    onUpdateSet: (Int, Double?, Int?, Int?) -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SETS & REPS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            Text("KG", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
            Text("REPS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(100.dp), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(32.dp))
        }

        sets.forEachIndexed { index, set ->
            val isActive = set.isActive
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) colorScheme.primary else Color.LightGray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isActive) colorScheme.primary else Color.Transparent,
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isActive) Color.Transparent else Color.LightGray,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        color = if (isActive) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Set ${index + 1}",
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) colorScheme.primary else Color.DarkGray
                )

                // KG Input
                TextField(
                    value = if (set.weightKg == 0.0) "" else set.weightKg.toString(),
                    onValueChange = { onUpdateSet(index, it.toDoubleOrNull(), null, null) },
                    modifier = Modifier.width(60.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) colorScheme.primary else Color.Gray
                    )
                )

                // REPS Input (Min-Max)
                Row(modifier = Modifier.width(100.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = if (set.repsMin == 0) "" else set.repsMin.toString(),
                        onValueChange = { onUpdateSet(index, null, it.toIntOrNull(), null) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) colorScheme.primary else Color.Gray
                        )
                    )
                    Text("-", color = Color.LightGray)
                    TextField(
                        value = if (set.repsMax == 0) "" else set.repsMax.toString(),
                        onValueChange = { onUpdateSet(index, null, null, it.toIntOrNull()) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) colorScheme.primary else Color.Gray
                        )
                    )
                }

                IconButton(onClick = { onRemoveSet(index) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddSetButton(onClick: () -> Unit) {
    val primaryColor = colorScheme.primary
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
            drawRoundRect(
                color = Color.LightGray,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Set", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
