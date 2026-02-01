package com.example.nutriflex2.diet.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dieta.domain.FatSecretFood
import com.example.nutriflex2.R
import components.LeftTitleText


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchMealsScreen(
    navController: NavController,
    onBack: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPhoto: () -> Unit,
    viewModel: SearchMealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val filtersExpanded = remember { mutableStateOf(false) }

    // Foco correto para Material3
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Diet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Search bar
            Box {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { newValue -> viewModel.onQueryChange(newValue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester), // <-- aqui
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        placeholder = { Text("Search food") },
                        singleLine = true,
                        interactionSource = interactionSource,
                    )

                    // DROPDOWN (só com foco + condições)
                    DropdownMenu(
                        expanded = isFocused &&
                                state.autocompleteSuggestions.isNotEmpty() &&
                                state.query.length >= 2 &&
                                !state.isLoading,
                        modifier = Modifier.fillMaxWidth(0.92f),
                        onDismissRequest = { focusManager.clearFocus() },
                        properties = PopupProperties(focusable = false)
                    ) {
                        state.autocompleteSuggestions.take(3).forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(text = suggestion, style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    viewModel.onQueryChange(suggestion)
                                    focusManager.clearFocus()
                                },

                            )
                        }
                    }

                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Photo + Favorites buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedButton(
                    onClick = onOpenPhoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Photo")
                }
                ElevatedButton(
                    onClick = onOpenFavorites,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Favorites")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toggle button for filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { filtersExpanded.value = !filtersExpanded.value }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Advanced filters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.primary
                )
                Icon(
                    imageVector = if (filtersExpanded.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = filtersExpanded.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        // Calorie Range
                        Text(
                            "Calorie Range",
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalorieChip(
                                text = "Under 100",
                                selected = state.calorieRange == CalorieRangeFilter.UNDER_100,
                                onClick = {
                                    viewModel.onCalorieRangeSelected(
                                        if (state.calorieRange == CalorieRangeFilter.UNDER_100)
                                            CalorieRangeFilter.NONE else CalorieRangeFilter.UNDER_100
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            CalorieChip(
                                text = "100 to 250",
                                selected = state.calorieRange == CalorieRangeFilter.FROM_100_TO_250,
                                onClick = {
                                    viewModel.onCalorieRangeSelected(
                                        if (state.calorieRange == CalorieRangeFilter.FROM_100_TO_250)
                                            CalorieRangeFilter.NONE else CalorieRangeFilter.FROM_100_TO_250
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalorieChip(
                                text = "250 to 500",
                                selected = state.calorieRange == CalorieRangeFilter.FROM_250_TO_500,
                                onClick = {
                                    viewModel.onCalorieRangeSelected(
                                        if (state.calorieRange == CalorieRangeFilter.FROM_250_TO_500)
                                            CalorieRangeFilter.NONE else CalorieRangeFilter.FROM_250_TO_500
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            CalorieChip(
                                text = "Over 500",
                                selected = state.calorieRange == CalorieRangeFilter.OVER_500,
                                onClick = {
                                    viewModel.onCalorieRangeSelected(
                                        if (state.calorieRange == CalorieRangeFilter.OVER_500)
                                            CalorieRangeFilter.NONE else CalorieRangeFilter.OVER_500
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Carbs Percentage
                        MacroRangeSection(
                            title = "Carbs Percentage",
                            min = state.carbsMin,
                            max = state.carbsMax,
                            onRangeChange = { min, max ->
                                viewModel.onCarbsRangeChanged(min, max)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Protein Percentage
                        MacroRangeSection(
                            title = "Protein Percentage",
                            min = state.proteinMin,
                            max = state.proteinMax,
                            onRangeChange = { min, max ->
                                viewModel.onProteinRangeChanged(min, max)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fat Percentage
                        MacroRangeSection(
                            title = "Fat Percentage",
                            min = state.fatMin,
                            max = state.fatMax,
                            onRangeChange = { min, max ->
                                viewModel.onFatRangeChanged(min, max)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggestions / loading
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()   // novo indicador material3
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.suggestions) { food ->
                        FoodRow(
                            food = food,
                            onAddClick = {},
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalorieChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colorScheme.primary.copy(alpha = 0.1f),
            selectedLabelColor = colorScheme.primary
        ),
        border = BorderStroke(
            1.dp,
            if (selected) colorScheme.primary else colorScheme.outline
        )
    )
}

@Composable
fun MacroRangeSection(
    title: String,
    min: Int,
    max: Int,
    onRangeChange: (Int, Int) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = colorScheme.secondary)
    Spacer(modifier = Modifier.height(4.dp))

    Text("Min: $min%   Max: $max%", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurface)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RangeSlider(
            value = min.toFloat()..max.toFloat(),
            onValueChange = { range ->
                val newMin = range.start.toInt().coerceIn(0, 100)
                val newMax = range.endInclusive.toInt().coerceIn(newMin, 100)
                onRangeChange(newMin, newMax)
            },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}


@Composable
fun FoodRow(
    food: FatSecretFood,
    navController: NavController,
    onAddClick: () -> Unit,
) {
    val context = LocalContext.current

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .clickable{navController.navigate("foodDetail/${food.id}")}
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DEBUG: ver o que chega da API
            println("FOOD IMAGE URL: ${food.image}")

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(food.image)
                    .crossfade(true)
                    .build(),
                contentDescription = food.nomeEn,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.nutrilogo),
                error = painterResource(R.drawable.nutrilogo),
                onError = { state ->
                    state.result.throwable.printStackTrace()
                    println("COIL ERROR: ${state.result.throwable.message}")
                }
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.nomeEn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.secondary
                )
                Text(
                    food.descricaoEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface
                )
            }

            IconButton(onClick = onAddClick) {
                Text("+", fontSize = 20.sp, color = colorScheme.primary)
            }
        }
    }
}