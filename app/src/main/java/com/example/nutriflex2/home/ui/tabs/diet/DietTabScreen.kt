package com.example.nutriflex2.home.ui.tabs.diet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import components.DietCaloriesCard
import components.LeftTitleText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTabScreen(
    onNavigateToSearchMeals: () -> Unit,
    onNavigateToSearchRecipes: () -> Unit,
    viewModel: DietTabViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(65.dp))
            LeftTitleText("Diet")

            DietCaloriesCard(
                remainingCalories = state.remainingCalories,
                dailyTargetCalories = state.dailyCalories,
                caloriesProgress = state.caloriesProgress,
                proteinRemaining = state.remainingProtein,
                proteinTarget = state.dailyProteinGrams,
                proteinProgress = state.proteinProgress,
                carbsRemaining = state.remainingCarbs,
                carbsTarget = state.dailyCarbsGrams,
                carbsProgress = state.carbsProgress,
                fatRemaining = state.remainingFat,
                fatTarget = state.dailyFatGrams,
                fatProgress = state.fatProgress,
                onAddClick = {
                    showSheet = true
                    scope.launch { sheetState.show() }
                }
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState
        ) {
            LogMealSheetContent(
                onPhotoClick = { /* TODO: navigate to photo capture */ },
                onFavoritesClick = { /* TODO */ },
                onSearchMealsClick = {
                    showSheet = false
                    scope.launch { sheetState.hide() }
                    onNavigateToSearchMeals()
                },
                onSearchRecipesClick = {
                    showSheet = false
                    scope.launch { sheetState.hide() }
                    onNavigateToSearchRecipes()
                }
            )
        }
    }
}

@Composable
fun LogMealSheetContent(
    onPhotoClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSearchMealsClick: () -> Unit,
    onSearchRecipesClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Log Meal", style = typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogMealButton(
                text = "Photo",
                icon = Icons.Default.PhotoCamera,
                modifier = Modifier.weight(1f),
                onClick = onPhotoClick
            )
            LogMealButton(
                text = "Favorites",
                icon = Icons.Default.Favorite,
                modifier = Modifier.weight(1f),
                onClick = onFavoritesClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LogMealButton(
                text = "Search Meals",
                icon = Icons.Default.Search,
                modifier = Modifier.weight(1f),
                onClick = onSearchMealsClick
            )
            LogMealButton(
                text = "Search Recipes",
                icon = Icons.Default.RiceBowl,
                modifier = Modifier.weight(1f),
                onClick = onSearchRecipesClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LogMealButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.outlinedButtonColors()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp),
                colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text)
        }
    }
}
