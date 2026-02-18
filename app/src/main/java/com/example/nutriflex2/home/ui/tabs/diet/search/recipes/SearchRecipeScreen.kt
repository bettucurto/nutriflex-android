package com.example.nutriflex2.home.ui.tabs.diet.search.recipes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dieta.domain.FatSecretRecipeSummary
import com.example.nutriflex2.R
import com.example.nutriflex2.diet.search.CalorieChip
import com.example.nutriflex2.diet.search.MacroItem
import components.CalorieRangeFilter
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecipeScreen(
    navController: NavController,
    onBack: () -> Unit,
    onOpenPhoto: () -> Unit,
    onOpenFavorites: () -> Unit,
    viewModel: SearchRecipeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val filtersExpanded = remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Recipes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- Search Bar ---
            OutlinedTextField(
                value = state.query,
                onValueChange = { newValue -> viewModel.onQueryChange(newValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search recipes") },
                singleLine = true,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Photo + Favorites Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedButton(
                    onClick = onOpenPhoto,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Photo", color = colorScheme.onSurface)
                }
                ElevatedButton(
                    onClick = onOpenFavorites,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Favorites", color = colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Filters Toggle ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
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
                    contentDescription = null,
                    tint = colorScheme.primary
                )
            }

            // --- Filters Section ---
            AnimatedVisibility(
                visible = filtersExpanded.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Calorie Range",
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips de Calorias
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CalorieChip("Under 100", state.calorieRange == CalorieRangeFilter.UNDER_100, Modifier.weight(1f)) {
                                    viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.UNDER_100) CalorieRangeFilter.NONE else CalorieRangeFilter.UNDER_100)
                                }
                                CalorieChip("100 - 250", state.calorieRange == CalorieRangeFilter.FROM_100_TO_250, Modifier.weight(1f)) {
                                    viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.FROM_100_TO_250) CalorieRangeFilter.NONE else CalorieRangeFilter.FROM_100_TO_250)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CalorieChip("250 - 500", state.calorieRange == CalorieRangeFilter.FROM_250_TO_500, Modifier.weight(1f)) {
                                    viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.FROM_250_TO_500) CalorieRangeFilter.NONE else CalorieRangeFilter.FROM_250_TO_500)
                                }
                                CalorieChip("Over 500", state.calorieRange == CalorieRangeFilter.OVER_500, Modifier.weight(1f)) {
                                    viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.OVER_500) CalorieRangeFilter.NONE else CalorieRangeFilter.OVER_500)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Macros Sliders
                        MacroRangeSection("Carbs %", state.carbsMin, state.carbsMax) { min, max -> viewModel.onCarbsRangeChanged(min, max) }
                        Spacer(modifier = Modifier.height(8.dp))
                        MacroRangeSection("Protein %", state.proteinMin, state.proteinMax) { min, max -> viewModel.onProteinRangeChanged(min, max) }
                        Spacer(modifier = Modifier.height(8.dp))
                        MacroRangeSection("Fat %", state.fatMin, state.fatMax) { min, max -> viewModel.onFatRangeChanged(min, max) }
                    }
                }
            }

            // --- Recipe List ---
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.suggestions) { recipe ->
                        RecipeRow(
                            recipe = recipe,
                            onClick = { navController.navigate("recipeDetail/${recipe.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroRangeSection(
    title: String,
    min: Int,
    max: Int,
    onRangeChange: (Int, Int) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = colorScheme.secondary)
            Text("$min% - $max%", style = MaterialTheme.typography.labelMedium, color = colorScheme.onSurfaceVariant)
        }
        RangeSlider(
            value = min.toFloat()..max.toFloat(),
            onValueChange = { range ->
                val newMin = range.start.toInt().coerceIn(0, 100)
                val newMax = range.endInclusive.toInt().coerceIn(newMin, 100)
                onRangeChange(newMin, newMax)
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.primary,
                activeTrackColor = colorScheme.primary,
                inactiveTrackColor = colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun RecipeRow(
    recipe: FatSecretRecipeSummary,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    // Cores adaptadas ao tema (Evitar fixar cores que não contrastam no Dark Mode)
    val carbColor = Color(0xFF4FC3F7)
    val proteinColor = Color(0xFF81C784)
    val fatColor = Color(0xFFFFB74D)
    val trackBackgroundColor = colorScheme.surfaceVariant

    // Parsing das macros
    val carbsVal = recipe.nutrition.carbohydrate?.filter { it.isDigit() }?.toFloatOrNull() ?: 0f
    val proteinVal = recipe.nutrition.protein?.filter { it.isDigit() }?.toFloatOrNull() ?: 0f
    val fatVal = recipe.nutrition.fat?.filter { it.isDigit() }?.toFloatOrNull() ?: 0f
    val totalMacros = carbsVal + proteinVal + fatVal

    val carbsPct = if (totalMacros > 0) ((carbsVal / totalMacros) * 100).toInt() else 0
    val proteinPct = if (totalMacros > 0) ((proteinVal / totalMacros) * 100).toInt() else 0
    val fatPct = if (totalMacros > 0) ((fatVal / totalMacros) * 100).toInt() else 0

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface, // Adapta-se automaticamente (branco no Light, escuro no Dark)
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Linha Superior: Imagem + Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(recipe.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.nomeEn,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant), // Adapta-se ao tema
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.nutrilogo),
                    error = painterResource(R.drawable.nutrilogo)
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = recipe.nomeEn,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = colorScheme.onSurface, // Adapta-se ao tema
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val calories = recipe.nutrition.calories?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                            Text(
                                text = "$calories kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.primary // Destaque na cor primária da app
                            )
                        }
                    }

                    Text(
                        text = recipe.descricaoEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant, // Texto secundário (cinza claro/escuro)
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barras de Macros (Usando o MacroItem importado do SearchMealsScreen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MacroItem(
                    label = "Carbs",
                    percentage = carbsPct,
                    color = carbColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )
                MacroItem(
                    label = "Protein",
                    percentage = proteinPct,
                    color = proteinColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )
                MacroItem(
                    label = "Fat",
                    percentage = fatPct,
                    color = fatColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}