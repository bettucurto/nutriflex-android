package com.example.nutriflex2.home.ui.tabs.diet

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.components.R
import components.DietCaloriesCard
import kotlinx.coroutines.launch
import theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTabScreen(
    onNavigateToSearchMeals: () -> Unit,
    onNavigateToSearchRecipes: () -> Unit,
    onNavigateToScanMeal: () -> Unit,
    scrollState: ScrollState,
    viewModel: DietTabViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // --- GRADIENTE ANIMADO (IDÊNTICO AO HOME) ---
    val gradientColors = listOf(
        colorScheme.secondary,
        colorScheme.primary,
        colorScheme.secondary
    )
    val transition = rememberInfiniteTransition(label = "diet_bg_anim")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diet_bg_translate"
    )
    val animatedBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 1000f, translateAnim + 1000f),
        tileMode = TileMode.Mirror
    )

    // --- FORMA CONVEXA (CURVADA PARA FORA) ---
    val density = LocalDensity.current
    val convexShape = remember(density) {
        GenericShape { size, _ ->
            val curveHeight = with(density) { 30.dp.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - curveHeight)
            quadraticBezierTo(
                size.width / 2f, size.height + curveHeight, // Curva para baixo
                0f, size.height - curveHeight
            )
            close()
        }
    }

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Usar a cor de background do tema para cobrir o gradiente global do HomeScreen
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        color = colorScheme.background 
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Convexo Animado (Top Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(convexShape)
                    .background(brush = animatedBrush)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = AppTheme.dimens.extraLargePadding,
                            start = AppTheme.dimens.mediumPadding,
                            end = AppTheme.dimens.mediumPadding
                        )
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(text = "NUTRITION",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.formulacondensedbold)),
                    fontSize = 115.sp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 8.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center,

                )


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
                
                // Espaço extra para garantir que o scroll permite ver o background grey abaixo
                Spacer(modifier = Modifier.height(300.dp))
            }
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
                onPhotoClick = {
                    showSheet = false
                    scope.launch { sheetState.hide() }
                    onNavigateToScanMeal()
                },
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

        LogMealButton(
            text = "Photo",
            icon = Icons.Default.PhotoCamera,
            modifier = Modifier.fillMaxWidth(),
            onClick = onPhotoClick
        )

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
