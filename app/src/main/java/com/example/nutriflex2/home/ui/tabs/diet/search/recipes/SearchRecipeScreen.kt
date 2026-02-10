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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dieta.domain.FatSecretRecipeSummary
import com.example.nutriflex2.R
import com.example.nutriflex2.diet.search.CalorieChip
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
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // --- Search Bar ---
            OutlinedTextField(
                value = state.query,
                onValueChange = { newValue -> viewModel.onQueryChange(newValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text("Search recipes") },
                singleLine = true,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(12.dp)
            )

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
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Recipe List ---
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
                            onAddClick = { /* Lógica de adicionar */ },
                            onClick = { navController.navigate("recipeDetail/${recipe.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeRow(
    recipe: FatSecretRecipeSummary,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    val context = LocalContext.current

    // Cores das barras de Macros
    val carbColor = Color(0xFF42A5F5)
    val proteinColor = Color(0xFF66BB6A)
    val fatColor = Color(0xFFFFCA28)
    val backgroundColor = Color(0xFFE0E0E0)

    // Parsing das macros para mostrar nas barras
    // Assumindo que o objeto 'nutrition' tem campos ou string "Carbs: 20g | Protein: 10g..."
    // Vamos usar os valores pré-calculados que adicionaremos ao objeto ou ViewModel,
    // mas aqui vou extrair diretamente para visualização.
    val carbsVal = recipe.nutrition.carbohydrate?.toFloatOrNull() ?: 0f
    val proteinVal = recipe.nutrition.protein?.toFloatOrNull() ?: 0f
    val fatVal = recipe.nutrition.fat?.toFloatOrNull() ?: 0f
    val totalMacros = carbsVal + proteinVal + fatVal

    // Convertendo para percentagem para as barras (aprox)
    val carbsPct = if (totalMacros > 0) ((carbsVal / totalMacros) * 100).toInt() else 0
    val proteinPct = if (totalMacros > 0) ((proteinVal / totalMacros) * 100).toInt() else 0
    val fatPct = if (totalMacros > 0) ((fatVal / totalMacros) * 100).toInt() else 0


    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
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
                        .background(Color.LightGray),
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
                            color = Color.Black,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${recipe.nutrition.calories} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Gray
                            )
                        }
                    }

                    Text(
                        text = recipe.descricaoEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barras de Macros (Usando o componente reutilizável do ecrã anterior se disponível, ou redefinindo aqui)
            // Vou redefinir caso não tenha acesso ao SearchMealsScreen.kt neste ficheiro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RecipeMacroItem("Carbs", carbsPct, carbColor, backgroundColor, Modifier.weight(1f))
                RecipeMacroItem("Protein", proteinPct, proteinColor, backgroundColor, Modifier.weight(1f))
                RecipeMacroItem("Fat", fatPct, fatColor, backgroundColor, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RecipeMacroItem(
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
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Black)
            Text(text = "$percentage%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
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