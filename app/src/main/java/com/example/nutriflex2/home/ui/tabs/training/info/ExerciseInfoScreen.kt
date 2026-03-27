package com.example.nutriflex2.home.ui.tabs.training.info

import androidx.annotation.OptIn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import components.LeftTitleText

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInfoScreen(
    onBack: () -> Unit,
    isAddingMode: Boolean = false,
    isReplacement: Boolean = false,
    exIndex: Int = -1,
    navController: NavController? = null,
    onAddExercise: (String) -> Unit = {},
    viewModel: ExerciseInfoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val secondaryColor = Color(0xFF03A9F4) // Azul NutriFlex

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (val s = state) {
                        is ExerciseInfoUiState.Success -> s.exercise.name
                        else -> "Exercise Details"
                    }
                    LeftTitleText(titleText)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if ((isAddingMode || isReplacement) && state is ExerciseInfoUiState.Success) {
                val exercise = (state as ExerciseInfoUiState.Success).exercise
                ExtendedFloatingActionButton(
                    text = { Text(if (isReplacement) "Replace Exercise" else "Add to Session", fontWeight = FontWeight.Bold) },
                    icon = { Icon(if (isReplacement) Icons.Default.Add else Icons.Default.Add, contentDescription = "Action") },
                    onClick = {
                        val data = "${exercise.id}|${exercise.name}|${exercise.bodyParts?.firstOrNull() ?: ""}|${exercise.imageUrl ?: exercise.gifUrl}"
                        onAddExercise(data)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    ) { padding ->
        when (val s = state) {
            is ExerciseInfoUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ExerciseInfoUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is ExerciseInfoUiState.Success -> {
                val exercise = s.exercise
                val context = LocalContext.current

                // Hoisting do ExoPlayer para o topo do ecrã (Success state)
                val exoPlayer = remember(exercise.id) {
                    ExoPlayer.Builder(context).build().apply {
                        if (!exercise.videoUrl.isNullOrEmpty()) {
                            setMediaItem(MediaItem.fromUri(exercise.videoUrl!!))
                            repeatMode = Player.REPEAT_MODE_ALL
                            playWhenReady = true
                            prepare()
                        }
                    }
                }

                DisposableEffect(exoPlayer) {
                    onDispose { exoPlayer.release() }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Media Player Area (Video or Image)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (!exercise.videoUrl.isNullOrEmpty()) {
                                    ExerciseVideoPlayer(player = exoPlayer)
                                } else {
                                    AsyncImage(
                                        model = exercise.gifUrl ?: exercise.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    // Highlights Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val primaryText = (exercise.bodyParts ?: emptyList()).ifEmpty { 
                                listOf(exercise.bodyPart ?: "N/A") 
                            }.joinToString(", ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

                            val typeText = exercise.exerciseType?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A"

                            val equipmentText = (exercise.equipments ?: emptyList()).ifEmpty { 
                                listOf(exercise.equipment ?: "None") 
                            }.joinToString(", ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

                            HighlightCard(
                                modifier = Modifier.weight(1f),
                                label = "PRIMARY",
                                value = primaryText,
                                icon = Icons.Default.FitnessCenter
                            )
                            HighlightCard(
                                modifier = Modifier.weight(1f),
                                label = "EQUIPMENT",
                                value = equipmentText,
                                icon = Icons.Default.Handyman
                            )
                            HighlightCard(
                                modifier = Modifier.weight(1f),
                                label = "TYPE",
                                value = typeText,
                                icon = Icons.Default.Layers
                            )
                        }
                    }

                    // About Section
                    val overview = exercise.overview
                    if (!overview.isNullOrEmpty()) {
                        item {
                            SectionHeader(icon = Icons.Default.Info, title = "About", color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Instructions Section
                    val instructions = exercise.instructions
                    if (!instructions.isNullOrEmpty()) {
                        item {
                            SectionHeader(icon = Icons.Default.List, title = "How to perform", color = MaterialTheme.colorScheme.secondary)
                        }
                        itemsIndexed(instructions) { index, step ->
                            InstructionStep(number = index + 1, text = step)
                        }
                    }

                    // Variations Section
                    val variations = exercise.variations
                    if (!variations.isNullOrEmpty()) {
                        item {
                            SectionHeader(icon = Icons.Default.AltRoute, title = "Variations", color = MaterialTheme.colorScheme.secondary)
                        }
                        items(variations) { variation ->
                            VariationItem(text = variation, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Pro Tips
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFF4CAF50))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pro Tips", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "• Focus on slow and controlled movements.\n• Maintain a neutral spine throughout.\n• Inhale on the way down, exhale on the effort.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun ExerciseVideoPlayer(player: Player) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun HighlightCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = primaryColor.copy(alpha = 0.1f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = primaryColor, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, 
                color = primaryColor.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun InstructionStep(number: Int, text: String) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = primaryColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$number", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }
    }
}

@Composable
fun VariationItem(text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}
