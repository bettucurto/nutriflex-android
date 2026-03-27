package com.example.nutriflex2.home.ui.tabs.training.search.workout

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWorkoutScreen(
    navController: NavController,
    viewModel: SearchWorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Browse Workouts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // --- Filters Section ---
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                // Label for Difficulty
                Text(
                    text = "Difficulty Level",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val difficulties = listOf(1 to "Beginner", 2 to "Intermediate", 3 to "Advanced")
                    difficulties.forEach { (id, label) ->
                        val isSelected = state.selectedDifficulty == id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onDifficultyFilterSelected(id) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label for Frequency
                Text(
                    text = "Frequency (Days/Week)",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf(1, 2, 3, 4, 5, 6)) { freq ->
                        val isSelected = state.selectedFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFrequencyFilterSelected(freq) },
                            label = { Text("$freq Days") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LaunchedEffect(state.actionFinished) {
                    if (state.actionFinished) {
                        Toast.makeText(context, "Workout Saved!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        viewModel.resetAction()
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.workouts) { workout ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(workout.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val level = when(workout.experience) { 1 -> "Beginner"; 2 -> "Intermediate"; 3 -> "Advanced"; else -> "N/A" }
                                    val freq = workout.frequency?.let { "$it days/week" } ?: "N/A"
                                    Text(
                                        "Level: $level | Frequency: $freq",
                                        fontSize = 12.sp, color = colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = !state.isDuplicating) { 
                                            viewModel.duplicateWorkout(workout.id)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (state.isDuplicating) colorScheme.surfaceVariant else colorScheme.secondaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (state.isDuplicating) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add",
                                                tint = colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
