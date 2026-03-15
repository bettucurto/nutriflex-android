package com.example.nutriflex2.home.ui.tabs.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInfoScreen(
    onBack: () -> Unit,
    viewModel: ExerciseInfoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = when (val s = state) {
                        is ExerciseInfoUiState.Success -> s.exercise.name.uppercase()
                        else -> "Details"
                    }
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is ExerciseInfoUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ExerciseInfoUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is ExerciseInfoUiState.Success -> {
                val exercise = s.exercise
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Media Player Area
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = exercise.gifUrl ?: exercise.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Play Icon Overlay (Static for now)
                                Surface(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .align(Alignment.Center),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp)
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
                            HighlightCard(
                                modifier = Modifier.weight(1f),
                                label = "PRIMARY",
                                value = exercise.target ?: "N/A",
                                icon = Icons.Default.FitnessCenter
                            )
                            HighlightCard(
                                modifier = Modifier.weight(1f),
                                label = "SECONDARY",
                                value = exercise.secondaryMuscles?.firstOrNull() ?: "None",
                                icon = Icons.Default.Layers
                            )
                            HighlightCard(
                                modifier = Modifier.weight(1f),
                                label = "EQUIPMENT",
                                value = exercise.equipment ?: "None",
                                icon = Icons.Default.Handyman
                            )
                        }
                    }

                    // About Section
                    item {
                        SectionHeader(icon = Icons.Default.Info, title = "About")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This exercise targets the ${exercise.target} and ${exercise.bodyPart}. It's excellent for building strength and definition in the targeted areas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }

                    // Instructions Section
                    val instructions = exercise.instructions
                    if (!instructions.isNullOrEmpty()) {
                        item {
                            SectionHeader(icon = Icons.Default.List, title = "How to perform")
                        }
                        itemsIndexed(instructions) { index, step ->
                            InstructionStep(number = index + 1, text = step)
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

@Composable
fun HighlightCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            Text(
                value.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
    }
}

@Composable
fun InstructionStep(number: Int, text: String) {
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$number", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }
    }
}
