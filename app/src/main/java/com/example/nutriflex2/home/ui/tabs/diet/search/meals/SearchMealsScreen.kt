package com.example.nutriflex2.home.ui.tabs.diet.search.meals

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dieta.domain.FatSecretFood
import com.example.dieta.domain.Refeicao
import com.example.nutriflex2.R
import components.CalorieRangeFilter
import components.LeftTitleText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SearchMealsScreen(
    navController: NavController,
    onBack: () -> Unit,
    onOpenFavorites: () -> Unit, // Mantido por compatibilidade de assinatura se necessário, mas não será usado
    onOpenCreateMeal: () -> Unit,
    onFoodClick: ((String) -> Unit)? = null,
    viewModel: SearchMealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val filtersExpanded = remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

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
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                // --- Search Bar ---
                Box {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            placeholder = { Text("Search food") },
                            singleLine = true,
                            interactionSource = interactionSource,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

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
                                    text = {
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        viewModel.onQueryChange(suggestion)
                                        focusManager.clearFocus()
                                    },
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ElevatedButton(
                        onClick = onOpenCreateMeal,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .shadow(16.dp, RectangleShape),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Create Meal", color = colorScheme.primary, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Tabs para alternar entre Geral e Favoritos ---
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("General", fontSize = 16.sp, fontWeight = if(pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("Favorites", fontSize = 16.sp, fontWeight = if(pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Só mostrar filtros na tab Geral
                if (pagerState.currentPage == 0) {
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

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CalorieChip(
                                            "Under 100",
                                            state.calorieRange == CalorieRangeFilter.UNDER_100,
                                            Modifier.weight(1f)
                                        ) {
                                            viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.UNDER_100) CalorieRangeFilter.NONE else CalorieRangeFilter.UNDER_100)
                                        }
                                        CalorieChip(
                                            "100 - 250",
                                            state.calorieRange == CalorieRangeFilter.FROM_100_TO_250,
                                            Modifier.weight(1f)
                                        ) {
                                            viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.FROM_100_TO_250) CalorieRangeFilter.NONE else CalorieRangeFilter.FROM_100_TO_250)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CalorieChip(
                                            "250 - 500",
                                            state.calorieRange == CalorieRangeFilter.FROM_250_TO_500,
                                            Modifier.weight(1f)
                                        ) {
                                            viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.FROM_250_TO_500) CalorieRangeFilter.NONE else CalorieRangeFilter.FROM_250_TO_500)
                                        }
                                        CalorieChip(
                                            "Over 500",
                                            state.calorieRange == CalorieRangeFilter.OVER_500,
                                            Modifier.weight(1f)
                                        ) {
                                            viewModel.onCalorieRangeSelected(if (state.calorieRange == CalorieRangeFilter.OVER_500) CalorieRangeFilter.NONE else CalorieRangeFilter.OVER_500)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                MacroRangeSection(
                                    "Carbs %",
                                    state.carbsMin,
                                    state.carbsMax
                                ) { min, max -> viewModel.onCarbsRangeChanged(min, max) }
                                Spacer(modifier = Modifier.height(8.dp))
                                MacroRangeSection(
                                    "Protein %",
                                    state.proteinMin,
                                    state.proteinMax
                                ) { min, max -> viewModel.onProteinRangeChanged(min, max) }
                                Spacer(modifier = Modifier.height(8.dp))
                                MacroRangeSection(
                                    "Fat %",
                                    state.fatMin,
                                    state.fatMax
                                ) { min, max -> viewModel.onFatRangeChanged(min, max) }
                            }
                        }
                    }
                }
            }

            // --- Pager com as Listas ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> GeneralSearchTab(state, onFoodClick, navController)
                    1 -> FavoriteMealsTab(state, navController)
                }
            }
        }
    }
}

@Composable
fun GeneralSearchTab(
    state: SearchMealsUiState,
    onFoodClick: ((String) -> Unit)?,
    navController: NavController
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorScheme.primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.suggestions) { food ->
                FoodRow(
                    food = food,
                    onClick = {
                        if (onFoodClick != null) {
                            onFoodClick(food.id)
                        } else {
                            navController.navigate("foodDetail/${food.id}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FavoriteMealsTab(
    state: SearchMealsUiState,
    navController: NavController
) {
    if (state.favoriteMeals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No favorite meals created yet", color = colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(state.favoriteMeals) { meal ->
                FavoriteMealRow(
                    meal = meal,
                    onClick = {
                        navController.navigate("favoriteMealDetail/${meal.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun FavoriteMealRow(
    meal: Refeicao,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val carbColor = Color(0xFF4FC3F7)
    val proteinColor = Color(0xFF81C784)
    val fatColor = Color(0xFFFFB74D)
    val trackBackgroundColor = colorScheme.surfaceVariant

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(meal.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = meal.nome,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant),
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
                            text = meal.nome,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${meal.calories ?: 0} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = meal.description ?: "Custom Meal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MacroItem(
                    label = "Carbs",
                    percentage = (meal.carbsPct ?: 0),
                    color = carbColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )

                MacroItem(
                    label = "Protein",
                    percentage = (meal.proteinPct ?: 0),
                    color = proteinColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )

                MacroItem(
                    label = "Fat",
                    percentage = (meal.fatPct ?: 0),
                    color = fatColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )
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
            selectedLabelColor = colorScheme.onPrimaryContainer,
            containerColor = colorScheme.surface,
            labelColor = colorScheme.onSurfaceVariant
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
                inactiveTrackColor = colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun FoodRow(
    food: FatSecretFood,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    // Cores adaptadas ao tema (Evitar fixar cores que não contrastam no Dark Mode)
    val carbColor = Color(0xFF4FC3F7)
    val proteinColor = Color(0xFF81C784)
    val fatColor = Color(0xFFFFB74D)
    val trackBackgroundColor = colorScheme.surfaceVariant

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(food.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = food.nomeEn,
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
                            text = food.nomeEn,
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
                            Text(
                                text = "${food.calories ?: 0} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.primary // Destaque na cor primária da app
                            )
                        }
                    }

                    Text(
                        text = food.descricaoEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant, // Texto secundário (cinza claro/escuro consoante o tema)
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MacroItem(
                    label = "Carbs",
                    percentage = (food.carbsPct ?: 0),
                    color = carbColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )

                MacroItem(
                    label = "Protein",
                    percentage = (food.proteinPct ?: 0),
                    color = proteinColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )

                MacroItem(
                    label = "Fat",
                    percentage = (food.fatPct ?: 0),
                    color = fatColor,
                    trackColor = trackBackgroundColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    percentage: Int,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant // Adapta-se ao tema
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface // Adapta-se ao tema
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp
        )
    }
}