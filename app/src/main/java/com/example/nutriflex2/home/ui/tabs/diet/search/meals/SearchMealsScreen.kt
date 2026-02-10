package com.example.nutriflex2.diet.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dieta.domain.FatSecretFood
import com.example.nutriflex2.R
import components.CalorieRangeFilter
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
                title = { LeftTitleText("Meals") },
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
            // --- Search Bar ---
            Box {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { newValue -> viewModel.onQueryChange(newValue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        placeholder = { Text("Search food") },
                        singleLine = true,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Autocomplete Dropdown
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
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("Favorites", color = colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Filters Toggle ---
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
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.3f)
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

            Spacer(modifier = Modifier.height(16.dp))

            // --- Meal List ---
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Mais espaço entre cards
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.suggestions) { food ->
                        FoodRow(
                            food = food,
                            onAddClick = { /* Adicionar lógica de clique */ },
                            onClick = { navController.navigate("foodDetail/${food.id}") } // Navegação ao clicar no card
                        )
                    }
                }
            }
        }
    }
}

// --- Componentes Auxiliares ---

@Composable
fun CalorieChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, maxLines = 1) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colorScheme.primaryContainer,
            selectedLabelColor = colorScheme.onPrimaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color.Transparent else colorScheme.outline
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
                inactiveTrackColor = colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun FoodRow(
    food: FatSecretFood,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    val context = LocalContext.current

    // Cores das barras baseadas na imagem (Azul=Carbs, Verde=Protein, Amarelo=Fat)
    val carbColor = Color(0xFF42A5F5)
    val proteinColor = Color(0xFF66BB6A)
    val fatColor = Color(0xFFFFCA28)
    val backgroundColor = Color(0xFFE0E0E0) // Cor do fundo das barras

    Surface(
        shape = RoundedCornerShape(20.dp), // Bordas bem arredondadas
        color = Color.White, // Fundo branco como na imagem
        tonalElevation = 0.dp,
        shadowElevation = 6.dp, // Sombra suave
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Linha Superior: Imagem + Textos + Calorias
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagem Redonda
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(food.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = food.nomeEn,
                    modifier = Modifier
                        .size(60.dp) // Tamanho maior
                        .clip(CircleShape) // Redonda
                        .background(Color.LightGray), // Placeholder background
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.nutrilogo), // Garanta que este recurso existe
                    error = painterResource(R.drawable.nutrilogo)
                )

                Spacer(Modifier.width(16.dp))

                // Títulos e Descrição
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Título (Nome da comida)
                        Text(
                            text = food.nomeEn,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.Black,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        // Calorias (canto superior direito)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${food.calories ?: 0} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Opcional: Icon de expandir se fosse uma lista expansível,
                            // mas a imagem não mostra explicitamente.
                            // Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.LightGray)
                        }
                    }

                    // Descrição (Marca ou detalhe)
                    Text(
                        text = food.descricaoEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 2,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Linha Inferior: Barras de Macros
            // Layout: Label + Valor à direita, Barra em baixo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Espaço entre colunas de macros
            ) {
                // Carbs
                MacroItem(
                    label = "Carbs",
                    percentage = (food.carbsPct ?: 0),
                    color = carbColor,
                    trackColor = backgroundColor,
                    modifier = Modifier.weight(1f)
                )

                // Protein
                MacroItem(
                    label = "Protein",
                    percentage = (food.proteinPct ?: 0),
                    color = proteinColor,
                    trackColor = backgroundColor,
                    modifier = Modifier.weight(1f)
                )

                // Fat
                MacroItem(
                    label = "Fat",
                    percentage = (food.fatPct ?: 0),
                    color = fatColor,
                    trackColor = backgroundColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    percentage: Int, // 0 a 100
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Texto: "Carbs 24%"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra de Progresso
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)), // Bordas arredondadas na barra
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp
        )
    }
}