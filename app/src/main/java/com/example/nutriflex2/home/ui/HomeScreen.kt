package com.example.nutriflex2.home.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
    onNavigateToAccount: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var pendingScroll by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(WeightRange.ONE_MONTH) }

    val drawerItems = listOf("Account", "Settings")
    var selectedDrawerItemIndex by remember { mutableIntStateOf(0) }

    val topBarHeightDp = 72.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val topBarHeightPx = with(density) { topBarHeightDp.toPx() }

    var topBarOffset by remember { mutableFloatStateOf(0f) }
    var lastScroll by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(scrollState.value) {
        val current = scrollState.value.toFloat()
        val delta = current - lastScroll

        if (delta > 0) {
            topBarOffset = (topBarOffset - delta).coerceAtLeast(-topBarHeightPx)
        }
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

    // Ouvir eventos de UI (toasts)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AppTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = com.example.nutriflex2.R.string.app_name),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = colorScheme.onSurface
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            color = colorScheme.onSurface.copy(alpha = 0.2f)
                        )

                        NavigationDrawerItem(
                            label = { Text("Account") },
                            selected = selectedDrawerItemIndex == 0,
                            onClick = {
                                selectedDrawerItemIndex = 0
                                scope.launch { drawerState.close() }
                                onNavigateToAccount()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = "Account"
                                )
                            },
                            modifier = Modifier.padding(
                                NavigationDrawerItemDefaults.ItemPadding
                            )
                        )

                        NavigationDrawerItem(
                            label = { Text("Settings") },
                            selected = selectedDrawerItemIndex == 1,
                            onClick = {
                                selectedDrawerItemIndex = 1
                                scope.launch { drawerState.close() }
                                // TODO: abrir ecrã de definições
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            modifier = Modifier.padding(
                                NavigationDrawerItemDefaults.ItemPadding
                            )
                        )
                    }
                }
            }
        ) {
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

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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

                        val context = LocalContext.current

                        WeightsCardRow(
                            currentWeight = state.currentWeight,
                            goalWeight = state.goalWeight,
                            onChangeCurrent = { new ->
                                viewModel.onChangeCurrentWeight(new, selectedRange)
                            },
                            onChangeGoal = {
                                viewModel.onChangeGoalWeight(it)
                            },
                            onRequestScroll = { pendingScroll = true }
                        )


                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                translationY = topBarOffset
                                val progress =
                                    1f - (-topBarOffset / topBarHeightPx).coerceIn(0f, 1f)
                                alpha = progress
                            }
                            .background(colorScheme.background)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shadowElevation = 8.dp,
                                shape = CircleShape,
                                color = colorScheme.outlineVariant,
                                modifier = Modifier.size(42.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) {
                                                drawerState.open()
                                            } else {
                                                drawerState.close()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Menu",
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(modifier = Modifier.weight(0.4f)) {
                                TitleText(
                                    value = stringResource(
                                        id = com.example.nutriflex2.R.string.app_name
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(42.dp))
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
}
