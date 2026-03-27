package com.example.nutriflex2.home.ui.tabs.training.search.exercise

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutriflex2.R
import com.example.treino.data.remote.ExerciseDbSummaryDto
import com.example.treino.domain.models.Exercicio
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchExerciseScreen(
    navController: NavController,
    exIndex: Int = -1,
    isReplacement: Boolean = false,
    viewModel: SearchExerciseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Search Exercise") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Search Bar ---
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
                    .background(colorScheme.surface, RoundedCornerShape(12.dp)),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search exercise name...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // --- Filters Section ---
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                // Label for Body Parts
                Text(
                    text = "Target Muscle",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(state.bodyParts) { bodyPart ->
                        val isSelected = state.selectedBodyPart == bodyPart
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onBodyPartFilterSelected(bodyPart) },
                            label = { 
                                Text(bodyPart.lowercase().replaceFirstChar { it.uppercase() }) 
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                selectedLeadingIconColor = colorScheme.onPrimary,
                                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label for Equipment
                Text(
                    text = "Equipment",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.equipments) { equipment ->
                        val isSelected = state.selectedEquipment == equipment
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEquipmentFilterSelected(equipment) },
                            label = { 
                                Text(equipment.lowercase().split(' ').joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } }) 
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                selectedLeadingIconColor = colorScheme.onPrimary,
                                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent,
                                enabled = true,
                                selected = isSelected
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
                if (state.searchResults.isEmpty()) {
                    EmptySearchContent(searchQuery = state.searchQuery)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.searchResults) { exercise ->
                            ExerciseListItem(
                                exercise = exercise,
                                onExerciseClick = { id ->
                                    if (id.isNotBlank()) {
                                        navController.navigate("exercise_info/$id?isAddingMode=false&isReplacement=$isReplacement&exIndex=$exIndex")
                                    }
                                },
                                onAddClick = {
                                    if (exercise.id.isNotBlank()) {
                                        if (isReplacement && exIndex != -1) {
                                            // Converte DTO para Modelo de Domínio (Exercicio)
                                            // Nota: Aqui assumes-se que existe um mapper ou lógica para converter ExerciseDbSummaryDto -> Exercicio
                                            // Como alternativa, cria-se o objeto Exercicio manualmente para o retorno.
                                            val replacedEx = Exercicio(
                                                id = 0,
                                                exercicioApiId = exercise.id,
                                                nome = exercise.name,
                                                notas = "",
                                                idSessao = 0,
                                                ordem = 0,
                                                imagem = exercise.imageUrl ?: exercise.gifUrl,
                                                bodypart = exercise.bodyParts?.firstOrNull()
                                            )
                                            navController.previousBackStackEntry?.savedStateHandle?.set("replaced_exercise", replacedEx)
                                            navController.previousBackStackEntry?.savedStateHandle?.set("replace_index", exIndex)
                                            navController.popBackStack()
                                        } else {
                                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                                "selected_exercise_data", 
                                                "${exercise.id}|${exercise.name}|${exercise.bodyParts?.firstOrNull() ?: ""}|${exercise.imageUrl ?: exercise.gifUrl}"
                                            )
                                            navController.popBackStack()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchContent(searchQuery: String) {
    val primaryGreen = MaterialTheme.colorScheme.primary
    val isInitialState = searchQuery.isBlank()

    val title = if (isInitialState) "Find your next challenge" else "No results found"
    val subtitle = if (isInitialState) {
        "Type the name of an exercise or muscle group to start building your workout."
    } else {
        "We couldn't find anything for \"$searchQuery\". Try adjusting your search term."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = primaryGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subtitle,
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExerciseListItem(
    exercise: ExerciseDbSummaryDto,
    onExerciseClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExerciseClick(exercise.id) }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(exercise.imageUrl ?: exercise.gifUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = exercise.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(1.dp, Color(0xFFE0E0E0)), CircleShape)
                    .background(colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.nutrilogo),
                error = painterResource(R.drawable.nutrilogo)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = exercise.name.split(' ').joinToString(" ") { it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                val equipment = exercise.equipments?.firstOrNull() ?: exercise.equipment ?: ""
                if (equipment.isNotEmpty()) {
                    Surface(
                        color = Color(0xFF03A9F4),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = equipment.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdsClick,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    val primaryTarget = exercise.target ?: ""
                    val secondaryTargets = exercise.secondaryMuscles ?: emptyList()
                    val allTargets = (listOf(primaryTarget) + secondaryTargets).filter { it.isNotBlank() }
                    
                    val targetText = if (allTargets.isNotEmpty()) {
                        "Target: " + allTargets.joinToString(", ") { 
                            it.lowercase().replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } 
                        }
                    } else {
                        "Target: " + (exercise.bodyParts ?: emptyList()).joinToString(", ") { 
                            it.lowercase().replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } 
                        }
                    }
                    
                    Text(
                        text = targetText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddClick() },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
