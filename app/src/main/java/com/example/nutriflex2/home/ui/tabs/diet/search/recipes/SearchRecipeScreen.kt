package com.example.nutriflex2.home.ui.tabs.diet.search.recipes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.suggestions) { recipe ->
                        RecipeRow(
                            recipe = recipe,
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
fun RecipeRow(
    recipe: FatSecretRecipeSummary,
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
            .clickable { navController.navigate("recipeDetail/${recipe.id}") }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(recipe.image)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.nomeEn,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.nutrilogo),
                error = painterResource(R.drawable.nutrilogo)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.nomeEn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.secondary
                )
                Text(
                    recipe.descricaoEn,
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
