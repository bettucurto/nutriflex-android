package com.example.nutriflex2.home.ui.tabs.training.active

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.treino.domain.models.Exercicio
import com.example.treino.domain.models.ExercicioSet
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onBack: () -> Unit,
    onFinish: (WorkoutSummary) -> Unit,
    navController: NavController,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val replacedExercise = navController.currentBackStackEntry?.savedStateHandle?.get<Exercicio>("replaced_exercise")
    val replaceIndex = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("replace_index")

    LaunchedEffect(replacedExercise, replaceIndex) {
        if (replacedExercise != null && replaceIndex != null && replaceIndex != -1) {
            viewModel.replaceExercise(replaceIndex, replacedExercise)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Exercicio>("replaced_exercise")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("replace_index")
        }
    }
    
    val sheetState = rememberModalBottomSheetState()
    var showSetTypeSheet by remember { mutableStateOf(false) }
    var setIndexToEdit by remember { mutableStateOf(-1) }
    var exIndexToEdit by remember { mutableStateOf(-1) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    WorkoutForegroundService.ACTION_FINISH_SET_NOTIFICATION -> {
                        val exIndex = intent.getIntExtra("exIndex", -1)
                        val setIndex = intent.getIntExtra("setIndex", -1)
                        if (exIndex != -1 && setIndex != -1) viewModel.toggleSetChecked(exIndex, setIndex)
                    }
                    WorkoutForegroundService.ACTION_TIMER_ADJUSTED -> {
                        val adjust = intent.getIntExtra(WorkoutForegroundService.EXTRA_ADJUST_SECONDS, 0)
                        viewModel.adjustRestTimeFromService(adjust)
                    }
                    WorkoutForegroundService.ACTION_TIMER_SKIPPED -> {
                        viewModel.skipRestFromService()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(WorkoutForegroundService.ACTION_FINISH_SET_NOTIFICATION)
            addAction(WorkoutForegroundService.ACTION_TIMER_ADJUSTED)
            addAction(WorkoutForegroundService.ACTION_TIMER_SKIPPED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished && uiState.summary != null) onFinish(uiState.summary!!)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText(uiState.sessionName) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    Text(viewModel.formatTime(uiState.timerSeconds), modifier = Modifier.padding(end = 16.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
                    itemsIndexed(uiState.exercises) { exIndex, activeEx ->
                        ActiveWorkoutExerciseCard(
                            activeEx = activeEx,
                            navController = navController,
                            exIndex = exIndex,
                            onToggleCheck = { setIndex -> viewModel.toggleSetChecked(exIndex, setIndex) },
                            onUpdateSet = { setIndex, w, r ->
                                if (w != null) viewModel.updateSetWeight(exIndex, setIndex, w)
                                if (r != null) viewModel.updateSetReps(exIndex, setIndex, r)
                            },
                            onAddSet = { viewModel.addSet(exIndex) },
                            onRemoveExercise = { viewModel.removeExercise(exIndex) },
                            onNotesChange = { n -> viewModel.updateExerciseNotes(exIndex, n) },
                            onOpenSetTypeMenu = { setIndex ->
                                exIndexToEdit = exIndex
                                setIndexToEdit = setIndex
                                showSetTypeSheet = true
                            },
                            onRemoveSet = { setIndex -> viewModel.removeSet(exIndex, setIndex) }
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.finishWorkout() },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Finish Workout", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.isResting,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    RestTimerBar(
                        timeLeftFormatted = viewModel.formatRestTime(uiState.restTimeLeft),
                        onSkip = { viewModel.skipRest() },
                        onAdd = { viewModel.addRestTime(15) },
                        onSubtract = { viewModel.subtractRestTime(15) }
                    )
                }
            }
        }
    }

    if (showSetTypeSheet) {
        ModalBottomSheet(onDismissRequest = { showSetTypeSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Select Set Type", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                ListItem(
                    headlineContent = { Text("Regular Set") }, 
                    leadingContent = { Icon(Icons.Default.FormatListNumbered, null) },
                    modifier = Modifier.clickable {
                        viewModel.toggleSetTypeExplicit(exIndexToEdit, setIndexToEdit, "REGULAR")
                        showSetTypeSheet = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Warmup Set") }, 
                    leadingContent = { Icon(Icons.Default.Waves, null, tint = Color(0xFFFFB300)) },
                    modifier = Modifier.clickable {
                        viewModel.toggleSetTypeExplicit(exIndexToEdit, setIndexToEdit, "WARMUP")
                        showSetTypeSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun ActiveWorkoutExerciseCard(
    activeEx: ActiveExerciseModel,
    navController: NavController,
    exIndex: Int,
    onToggleCheck: (Int) -> Unit,
    onUpdateSet: (Int, Double?, Int?) -> Unit,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onNotesChange: (String) -> Unit,
    onOpenSetTypeMenu: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { 
                        navController.navigate("exercise_info/${activeEx.exercicio.exercicioApiId}?isAddingMode=false") 
                    }
            ) {
                AsyncImage(model = activeEx.exercicio.imagem ?: "", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)), startY = 100f)))
                Text(
                    text = "${activeEx.exercicio.nome} (${activeEx.exercicio.bodypart ?: ""})", 
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp), 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp
                )
                
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = Color.White)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Replace Exercise") }, onClick = { 
                            showMenu = false
                            navController.navigate("search_exercise/$exIndex?isReplacement=true")
                        }, leadingIcon = { Icon(Icons.Default.SwapHoriz, null) })
                        DropdownMenuItem(text = { Text("Remove Exercise", color = MaterialTheme.colorScheme.error) }, onClick = {  showMenu = false; onRemoveExercise() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXERCISE NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    BasicTextField(
                        value = activeEx.exercicio.notas,
                        onValueChange = onNotesChange,
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { inner -> 
                            if (activeEx.exercicio.notas.isEmpty()) Text("Add coaching tips...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
                            inner()
                        }
                    )
                }
            }

            ActiveWorkoutSetTable(activeEx.sets, onToggleCheck, onUpdateSet, onOpenSetTypeMenu, onRemoveSet)

            Button(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(20.dp))
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
    onUpdateSet: (Int, Double?, Int?) -> Unit,
    onOpenSetTypeMenu: (Int) -> Unit,
    onRemoveSet: (Int) -> Unit
) {
    val headerColor = MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("SET", modifier = Modifier.weight(0.12f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Text("LAST", modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Text("KG", modifier = Modifier.weight(0.22f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Text("REPS", modifier = Modifier.weight(0.22f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Box(modifier = Modifier.weight(0.12f))
        }
        sets.forEachIndexed { index, set ->
            ActiveWorkoutSetRow(index, set, sets, { onToggleCheck(index) }, { w, r -> onUpdateSet(index, w, r) }, { onOpenSetTypeMenu(index) }, { onRemoveSet(index) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActiveWorkoutSetRow(
    index: Int,
    set: ExercicioSet,
    allSets: List<ExercicioSet>,
    onToggleCheck: () -> Unit,
    onUpdateSet: (Double?, Int?) -> Unit,
    onOpenSetTypeMenu: () -> Unit,
    onRemoveSet: () -> Unit
) {
    val isChecked = set.isChecked
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(0.12f).size(36.dp).clip(CircleShape).clickable(enabled = !isChecked) { onOpenSetTypeMenu() }, contentAlignment = Alignment.Center) {
            val (text, color) = when (set.tipoSet) {
                "WARMUP" -> "W" to Color(0xFFFFB300)
                else -> {
                    val num = allSets.take(index + 1).count { it.tipoSet == "REGULAR" || it.tipoSet.isEmpty() }
                    num.toString() to if (isChecked) primaryColor else MaterialTheme.colorScheme.secondary
                }
            }
            Text(text, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 15.sp)
        }
        Text(if (set.pesoUltimaVez > 0) "${set.repeticoesUltimaVez} x ${set.pesoUltimaVez}kg" else "-", modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        ActiveSetInputField(
            value = if (set.peso == 0.0) "" else set.peso.toString(),
            placeholder = "kg",
            onValueChange = { onUpdateSet(it.toDoubleOrNull(), null) },
            modifier = Modifier.weight(0.22f),
            enabled = !isChecked
        )

        ActiveSetInputField(
            value = if (set.repeticoesMin == 0 && set.repeticoesMax == 0) "" else if (set.repeticoesMin == set.repeticoesMax) "${set.repeticoesMin}" else "${set.repeticoesMin}-${set.repeticoesMax}",
            placeholder = "${set.repeticoesMin}-${set.repeticoesMax}",
            onValueChange = { onUpdateSet(null, it.toIntOrNull()) },
            modifier = Modifier.weight(0.22f),
            enabled = !isChecked
        )

        // AQUI ESTÁ A MUDANÇA: Substituímos o IconButton por um Box normal
        // Aumentámos o weight para 0.12f (igual ao cabeçalho) e demos padding à esquerda (start)
        Box(modifier = Modifier.weight(0.16f).padding(start = 8.dp, end = 4.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(28.dp) // Um tamanho simpático e perfeitamente quadrado
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isChecked) primaryColor else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        if (!isChecked) {
                            // Se peso está vazio, usa o peso da última vez ou 0
                            val weightToUse = if (set.peso == 0.0) {
                                if (set.pesoUltimaVez > 0) set.pesoUltimaVez else 0.0
                            } else set.peso

                            // Se reps estão vazias, usa as reps da última vez ou repsMin (ou max)
                            val repsToUse = if (set.repeticoesMin == 0) {
                                if (set.repeticoesUltimaVez > 0) set.repeticoesUltimaVez else set.repeticoesMin
                            } else set.repeticoesMin

                            // Atualiza os valores antes de fazer o toggle check
                            if (set.peso == 0.0 || set.repeticoesMin == 0) {
                                onUpdateSet(weightToUse, repsToUse)
                            }
                        }
                        onToggleCheck()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = if (isChecked) Color.White else Color.Transparent, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ActiveSetInputField(
    value: String, 
    placeholder: String, 
    onValueChange: (String) -> Unit, 
    modifier: Modifier, 
    enabled: Boolean
) {
    // Usamos um estado local para permitir a edição livre antes de submeter ao ViewModel
    // O LaunchedEffect garante que se o valor externo mudar (ex: preenchimento automático), o campo é atualizado.
    var textState by remember { mutableStateOf(value) }
    LaunchedEffect(value) { textState = value }

    Surface(
        modifier = modifier.padding(horizontal = 2.dp), 
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), 
        shape = RoundedCornerShape(6.dp)
    ) {
        BasicTextField(
            value = textState,
            onValueChange = { newValue ->
                textState = newValue
                onValueChange(newValue)
            },
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            textStyle = TextStyle(
                textAlign = TextAlign.Center, 
                fontWeight = FontWeight.Bold, 
                color = if(enabled) MaterialTheme.colorScheme.onSurface else Color.Gray, 
                fontSize = 14.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            readOnly = !enabled,
            singleLine = true,
            decorationBox = { inner ->
                if (textState.isEmpty() && enabled) {
                    Text(
                        placeholder, 
                        color = Color.Gray.copy(0.5f), 
                        textAlign = TextAlign.Center, 
                        modifier = Modifier.fillMaxWidth(), 
                        fontSize = 12.sp
                    )
                }
                inner()
            }
        )
    }
}

@Composable
fun RestTimerBar(timeLeftFormatted: String, onSkip: () -> Unit, onAdd: () -> Unit, onSubtract: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.inverseSurface, shadowElevation = 8.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSubtract) { Text("-15", color = Color.White, fontWeight = FontWeight.Bold) }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("RESTING", color = Color.White.copy(0.6f), fontSize = 10.sp)
                    Text(timeLeftFormatted, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onAdd) { Text("+15", color = Color.White, fontWeight = FontWeight.Bold) }
            }
            Button(onClick = onSkip) { Text("SKIP") }
        }
    }
}

