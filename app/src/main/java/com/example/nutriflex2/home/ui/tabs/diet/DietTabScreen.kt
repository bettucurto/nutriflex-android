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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.platform.LocalConfiguration
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
import components.FastingCard
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var showFastingSheet by remember { mutableStateOf(false) }
    var infoDurationToShow by remember { mutableStateOf<String?>(null) }
    
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

                val configuration = LocalConfiguration.current

                val screenWidth = configuration.screenWidthDp
                // Com Formula Condensed, 0.18f da largura do ecrã aproxima-se de 90% da width.
                val dynamicFontSize = (screenWidth * 0.28f).sp

                // Title
                Text(
                    text = "NUTRITION",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.formulacondensedbold)),
                    fontSize = dynamicFontSize,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(top = AppTheme.dimens.smallPadding)
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

                Spacer(modifier = Modifier.height(AppTheme.dimens.largePadding + 20.dp))

                FastingCard(
                    isFasting = state.isFasting,
                    startTime = state.fastingStartTime,
                    endTime = null, // Parameter removed or not needed
                    fastingDuration = state.fastingDuration,
                    remainingTime = state.remainingTime,
                    progress = state.progress,
                    onEditObjectiveClick = {
                        showFastingSheet = true
                        scope.launch { sheetState.show() }
                    },
                    onToggleFasting = { viewModel.toggleFasting() }
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

    if (showFastingSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showFastingSheet = false
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState
        ) {
            FastingOptionsSheetContent(
                currentDuration = state.fastingDuration,
                onSelectDuration = { duration ->
                    viewModel.updateFastingDuration(duration)
                    showFastingSheet = false
                    scope.launch { sheetState.hide() }
                },
                onInfoClick = { duration ->
                    infoDurationToShow = duration
                }
            )
        }
    }

    if (infoDurationToShow != null) {
        ModalBottomSheet(
            onDismissRequest = { infoDurationToShow = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FastingInfoSheetContent(
                duration = infoDurationToShow!!,
                onClose = { infoDurationToShow = null }
            )
        }
    }
}

@Composable
fun FastingOptionsSheetContent(
    currentDuration: String,
    onSelectDuration: (String) -> Unit,
    onInfoClick: (String) -> Unit
) {
    val options = listOf(
        "12:12" to "Beginner",
        "14:10" to "Intermediate",
        "16:8" to "Standard",
        "18:6" to "Advanced",
        "20:4" to "Expert"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Fasting Objective",
            style = typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { (duration, level) ->
            FastingOptionItem(
                duration = duration,
                level = level,
                isSelected = currentDuration == duration,
                onClick = { onSelectDuration(duration) },
                onInfoClick = { onInfoClick(duration) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FastingOptionItem(
    duration: String,
    level: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
        contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(text = duration, style = typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = level, style = typography.labelMedium)
                }
            }

            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = if (isSelected) colorScheme.onPrimary else colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FastingInfoSheetContent(
    duration: String,
    onClose: () -> Unit
) {
    val info = when (duration) {
        "12:12" -> FastingInfo(
            title = "12:12 Beginner Protocol",
            description = "The most natural way to start fasting. You simply stop eating after dinner and resume at breakfast.",
            benefits = listOf(
                "Great for metabolic flexibility",
                "Easy to maintain with social life",
                "Improves sleep quality",
                "Gentle introduction to fat adaptation"
            ),
            schedule = "Eating window: 8:00 AM - 8:00 PM\nFasting window: 8:00 PM - 8:00 AM"
        )
        "14:10" -> FastingInfo(
            title = "14:10 Intermediate Protocol",
            description = "A step up from the beginner level. It increases the fasting window slightly to activate more metabolic benefits.",
            benefits = listOf(
                "Enhanced fat burning",
                "Better blood sugar regulation",
                "Increased focus in the morning",
                "Reduces late-night snacking"
            ),
            schedule = "Eating window: 10:00 AM - 8:00 PM\nFasting window: 8:00 PM - 10:00 AM"
        )
        "16:8" -> FastingInfo(
            title = "16:8 Standard Protocol",
            description = "The most popular intermittent fasting method. Often called the Leangains protocol, it balances social life with deep health benefits.",
            benefits = listOf(
                "Initiates Autophagy (Cellular cleanup)",
                "Significant weight management benefits",
                "Optimizes growth hormone levels",
                "Improves insulin sensitivity"
            ),
            schedule = "Eating window: 12:00 PM - 8:00 PM\nFasting window: 8:00 PM - 12:00 PM"
        )
        "18:6" -> FastingInfo(
            title = "18:6 Advanced Protocol",
            description = "For those who have mastered 16:8. This protocol enters deeper stages of autophagy and hormonal optimization.",
            benefits = listOf(
                "Higher rates of autophagy",
                "Maximum fat oxidation",
                "Reduced inflammation markers",
                "Simplifies meal planning (2 meals/day)"
            ),
            schedule = "Eating window: 2:00 PM - 8:00 PM\nFasting window: 8:00 PM - 2:00 PM"
        )
        "20:4" -> FastingInfo(
            title = "20:4 Expert (Warrior Diet)",
            description = "The Warrior Diet mimics early human eating patterns: fasting all day and having a large feast in the evening.",
            benefits = listOf(
                "Peak cognitive performance",
                "Maximum cellular detoxification",
                "Significant calorie control",
                "Ancestral health alignment"
            ),
            schedule = "Eating window: 4:00 PM - 8:00 PM\nFasting window: 8:00 PM - 4:00 PM"
        )
        else -> FastingInfo("Info", "Details not available", emptyList(), "")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = info.title,
            style = typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = info.description,
            style = typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Key Benefits:",
                style = typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            info.benefits.forEach { benefit ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = benefit, style = typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Example Schedule:",
                    style = typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = info.schedule,
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        components.NFButton(
            text = "Got it",
            onButtonClicked = onClose,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class FastingInfo(
    val title: String,
    val description: String,
    val benefits: List<String>,
    val schedule: String
)

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
