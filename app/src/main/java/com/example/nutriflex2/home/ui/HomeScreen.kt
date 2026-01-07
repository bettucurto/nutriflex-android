package com.example.nutriflex2.home.ui

import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import components.BmiCard
import components.CaloriesCard
import components.NFBottomBar
import components.NextWorkoutCard
import components.TitleText
import components.WeightForecastCard
import components.WeightProgressCard
import components.WeightRange
import components.WeightsCardRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theme.AppTheme

@Composable
fun HomeScreen(
    onNavigateToTreino: () -> Unit,
    onNavigateToDieta: () -> Unit,
    onAddCaloriesClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var pendingScroll by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(WeightRange.ONE_MONTH) }

    // altura real da barra (ajusta se necessário)
    val topBarHeightDp = 72.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val topBarHeightPx = with(density) { topBarHeightDp.toPx() }

    var topBarOffset by remember { mutableStateOf(0f) }
    var lastScroll by remember { mutableStateOf(0f) }

    // atualiza offset conforme o scroll (efeito progressivo)
    LaunchedEffect(scrollState.value) {
        val current = scrollState.value.toFloat()
        val delta = current - lastScroll

        // a descer -> esconder até -topBarHeightPx
        if (delta > 0) {
            topBarOffset = (topBarOffset - delta).coerceAtLeast(-topBarHeightPx)
        }
        // a subir -> mostrar até 0
        if (delta < 0) {
            topBarOffset = (topBarOffset - delta).coerceAtMost(0f)
        }

        lastScroll = current
    }

    LaunchedEffect(pendingScroll) {
        if (pendingScroll) {
            scope.launch {
                delay(50)
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            pendingScroll = false
        }
    }

    AppTheme {
        Scaffold(
            bottomBar = {
                NFBottomBar(
                    onTreinoClick = onNavigateToTreino,
                    onPerfilClick = {},
                    onDietaClick = onNavigateToDieta
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                // Conteúdo scrollável
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // espaço para não ficar tapado quando a barra estiver visível
                    Spacer(modifier = Modifier.height(topBarHeightDp))

                    CaloriesCard(
                        remaining = state.remainingCalories,
                        dailyTarget = state.dailyCalories,
                        progress = state.progress,
                        onAddClick = onAddCaloriesClick
                    )

                    NextWorkoutCard(
                        workoutName = state.nextWorkoutName,
                        exerciseCount = state.nextWorkoutExercises,
                        onStartClick = { onNavigateToTreino() }
                    )

                    BmiCard(
                        bmi = state.bmi,
                        category = state.bmiCategory
                    )

                    WeightForecastCard(
                        weeks = state.weeklyProgressWeeks,
                        goalWeight = state.goalWeight,
                    )

                    WeightProgressCard(
                        selectedRange = selectedRange,
                        history = state.weightHistory,
                        onRangeChange = { newRange ->
                            selectedRange = newRange
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                viewModel.loadWeightHistory(newRange)
                            }
                        }
                    )

                    WeightsCardRow(
                        currentWeight = state.currentWeight,
                        goalWeight = state.goalWeight,
                        onChangeCurrent = { new ->
                            viewModel.onChangeCurrentWeight(new, selectedRange)
                        },
                        onChangeGoal = { viewModel.onChangeGoalWeight(it) },
                        onRequestScroll = { pendingScroll = true }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Top bar sobreposta, com fundo e alpha
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .graphicsLayer {
                            translationY = topBarOffset
                            // 0 quando escondida, 1 quando visível
                            val progress =
                                1f - (-topBarOffset / topBarHeightPx).coerceIn(0f, 1f)
                            alpha = progress
                        }
                        .background(colorScheme.background) // cor de fundo da barra
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(42.dp))

                        Box(modifier = Modifier.weight(0.4f)) {
                            TitleText(
                                value = stringResource(
                                    id = com.example.nutriflex2.R.string.app_name
                                )
                            )
                        }

                        Surface(
                            shadowElevation = 8.dp,
                            shape = CircleShape,
                            color = colorScheme.outlineVariant,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Definições",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(200.dp)
                            .padding(vertical = 4.dp),
                        color = colorScheme.onSurface,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}
