package com.example.nutriflex2.home.ui.tabs.training

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.treino.domain.models.ExercicioSet
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onBack: () -> Unit,
    onFinish: (WorkoutSummary) -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Request Notification Permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled, service will start anyway but notification might be hidden
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished && uiState.summary != null) {
            onFinish(uiState.summary!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText(uiState.sessionName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    Text(
                        text = viewModel.formatTime(uiState.timerSeconds),
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(uiState.exercises) { exIndex, activeEx ->
                        ActiveWorkoutExerciseCard(
                            activeEx = activeEx,
                            onToggleCheck = { setIndex -> viewModel.toggleSetChecked(exIndex, setIndex) },
                            onUpdateSet = { setIndex, weight, reps -> 
                                viewModel.updateSetValues(exIndex, setIndex, weight, reps) 
                            },
                            onAddSet = { viewModel.addSet(exIndex) },
                            onRemoveExercise = { viewModel.removeExercise(exIndex) },
                            onNotesChange = { notes -> viewModel.updateExerciseNotes(exIndex, notes) }
                        )
                    }

                    item {
                        Button(
                            onClick = { viewModel.finishWorkout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Finish Workout", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }

                // --- Rest Timer Bar ---
                AnimatedVisibility(
                    visible = uiState.isResting,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    RestTimerBar(
                        timeLeft = uiState.restTimeLeft,
                        timeLeftFormatted = viewModel.formatRestTime(uiState.restTimeLeft),
                        onSkip = { viewModel.skipRest() },
                        onAdd = { viewModel.addRestTime(15) },
                        onSubtract = { viewModel.subtractRestTime(15) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveWorkoutExerciseCard(
    activeEx: ActiveExerciseModel,
    onToggleCheck: (Int) -> Unit,
    onUpdateSet: (Int, Double?, Int?) -> Unit,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onNotesChange: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            // Header Image with Gradient and Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16/9f)
            ) {
                AsyncImage(
                    model = activeEx.exercicio.imagem ?: "https://via.placeholder.com/150",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
                Text(
                    text = activeEx.exercicio.nome,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                // Action Icon (3 dots)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onRemoveExercise()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            // Notes Section
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "EXERCISE NOTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    BasicTextField(
                        value = activeEx.exercicio.notas,
                        onValueChange = onNotesChange,
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (activeEx.exercicio.notas.isEmpty()) {
                                Text("Add coaching tips or session focus...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Set Table
            ActiveWorkoutSetTable(
                sets = activeEx.sets,
                onToggleCheck = onToggleCheck,
                onUpdateSet = onUpdateSet
            )

            // Add Set Button
            Button(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddCircle, 
                        contentDescription = null, 
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .clip(CircleShape),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD SET", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ActiveWorkoutSetTable(
    sets: List<ExercicioSet>,
    onToggleCheck: (Int) -> Unit,
    onUpdateSet: (Int, Double?, Int?) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("SET", modifier = Modifier.weight(0.12f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text("LAST", modifier = Modifier.weight(0.33f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text("KG", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text("REPS", modifier = Modifier.weight(0.2f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Box(modifier = Modifier.weight(0.15f))
        }

        sets.forEachIndexed { index, set ->
            ActiveWorkoutSetRow(
                index = index,
                set = set,
                onToggleCheck = { onToggleCheck(index) },
                onUpdateSet = { weight, reps -> onUpdateSet(index, weight, reps) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutSetRow(
    index: Int,
    set: ExercicioSet,
    onToggleCheck: () -> Unit,
    onUpdateSet: (Double?, Int?) -> Unit
) {
    val isChecked = set.isChecked
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isChecked) primaryColor else Color.Transparent
    val backgroundColor = if (isChecked) primaryColor.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (index + 1).toString(),
            modifier = Modifier.weight(0.12f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isChecked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
        )

        val lastText = if (set.pesoUltimaVez > 0) "${set.repeticoesUltimaVez} x ${set.pesoUltimaVez}kg" else "-"
        Text(
            text = lastText,
            modifier = Modifier.weight(0.33f),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = if (isChecked) primaryColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        ActiveSetInputField(
            value = if (set.peso == 0.0) "" else set.peso.toString(),
            onValueChange = { onUpdateSet(it.toDoubleOrNull(), null) },
            modifier = Modifier.weight(0.2f),
            isEnabled = !isChecked
        )

        ActiveSetInputField(
            value = if (set.repeticoesMin == 0) "" else set.repeticoesMin.toString(),
            onValueChange = { onUpdateSet(null, it.toIntOrNull()) },
            modifier = Modifier.weight(0.2f),
            isEnabled = !isChecked
        )

        Box(
            modifier = Modifier.weight(0.15f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                IconButton(
                    onClick = onToggleCheck,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isChecked) primaryColor else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isChecked) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSetInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    isEnabled: Boolean
) {
    Surface(
        modifier = modifier.padding(horizontal = 4.dp).heightIn(min = 32.dp).wrapContentHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            readOnly = !isEnabled,
            decorationBox = { innerTextField ->
                if (value.isEmpty() && isEnabled) {
                    Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun RestTimerBar(
    timeLeft: Int,
    timeLeftFormatted: String,
    onSkip: () -> Unit,
    onAdd: () -> Unit,
    onSubtract: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSubtract, modifier = Modifier.size(40.dp)) {
                    Text("-15", color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("RESTING", color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(timeLeftFormatted, color = MaterialTheme.colorScheme.inverseOnSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(onClick = onAdd, modifier = Modifier.size(40.dp)) {
                    Text("+15", color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            
            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SKIP", fontWeight = FontWeight.Bold)
            }
        }
    }
}
