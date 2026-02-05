package com.example.nutriflex2.home.ui

import HomeMainTab
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.NavigationBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutriflex2.home.ui.tabs.diet.DietTabScreen
import com.example.nutriflex2.home.ui.tabs.diet.DietTabViewModel
import com.example.nutriflex2.home.ui.tabs.training.TrainingTabScreen
import components.NFBottomBar
import components.TitleText
import components.WeightRange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToTreino: () -> Unit,
    onNavigateToSearchMeals: () -> Unit,
    onNavigateToSearchRecipes: () -> Unit,
    onNavigateToAccount: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    //LÓGICA DO GRADIENTE
    val gradientColors = listOf(
        colorScheme.secondary,
        colorScheme.primary,
        colorScheme.secondary
    )
    val transition = rememberInfiniteTransition(label = "bg_anim")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_translate"
    )
    val animatedBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 1000f, translateAnim + 1000f),
        tileMode = TileMode.Mirror
    )

    //Viewmodels
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedDrawerItemIndex by remember { mutableIntStateOf(0) }

    val topBarHeightDp = 72.dp
    val density = LocalDensity.current
    val topBarHeightPx = with(density) { topBarHeightDp.toPx() }

    // alvo lógico
    var topBarTargetOffset by remember { mutableFloatStateOf(0f) }
    var lastScroll by remember { mutableFloatStateOf(0f) }

    // valor animado que vai para o graphicsLayer
    val animatedTopBarOffset by animateFloatAsState(
        targetValue = topBarTargetOffset,
        label = "topBarOffsetAnimation"
    )

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Pager para tabs da bottom bar: 0 = Home, 1 = Treino, 2 = Dieta (exemplo)
    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { 3 }
    )

    // Scroll vertical apenas da tab Home
    val homeScrollState = rememberScrollState()
    var pendingScroll by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf(WeightRange.ONE_MONTH) }

    LaunchedEffect(pagerState.currentPage) {
        // anima de onde estiver até 0f
        topBarTargetOffset = 0f
    }


    LaunchedEffect(homeScrollState.value) {
        val current = homeScrollState.value.toFloat()
        val delta = current - lastScroll

        if (delta > 0) {
            topBarTargetOffset = (topBarTargetOffset - delta).coerceAtLeast(-topBarHeightPx)
        }
        if (delta < 0) {
            topBarTargetOffset = (topBarTargetOffset - delta).coerceAtMost(0f)
        }

        lastScroll = current
    }


    LaunchedEffect(pendingScroll) {
        if (pendingScroll) {
            scope.launch {
                delay(50)
                homeScrollState.animateScrollTo(homeScrollState.maxValue)
            }
            pendingScroll = false
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
                            .fillMaxWidth(0.6f)
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
                        selectedIndex = pagerState.currentPage,
                        onTreinoClick = {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onPerfilClick = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onDietaClick = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    )

                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(brush = animatedBrush)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> TrainingTabScreen(
                                onNavigateToTreino = onNavigateToTreino
                            )

                            1 -> {
                                val homeViewModel: HomeViewModel =
                                    hiltViewModel()
                                LaunchedEffect(Unit) { homeViewModel.onMealLogged() }
                                HomeMainTab(
                                    state = state,
                                    topBarHeightDp = topBarHeightDp,
                                    scrollState = homeScrollState,
                                    selectedRange = selectedRange,
                                    onRangeChange = { newRange ->
                                        selectedRange = newRange
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            viewModel.loadWeightHistory(newRange)
                                        }
                                    },
                                    onNavigateToSearchMeals = onNavigateToSearchMeals,
                                    onNavigateToTreino = onNavigateToTreino,
                                    onChangeCurrentWeight = { new ->
                                        viewModel.onChangeCurrentWeight(new, selectedRange)
                                    },
                                    onChangeGoalWeight = { new ->
                                        viewModel.onChangeGoalWeight(new)
                                    },
                                    onRequestScrollToBottom = { pendingScroll = true },
                                    onNavigateToSearchRecipes = onNavigateToSearchRecipes

                                )
                            }

                            2 -> {
                                val dietVm: DietTabViewModel = hiltViewModel()
                                LaunchedEffect(Unit) { dietVm.refreshFromLocal() }
                                DietTabScreen(
                                    onNavigateToSearchMeals = onNavigateToSearchMeals,
                                    onNavigateToSearchRecipes = onNavigateToSearchRecipes,
                                    viewModel = dietVm
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                translationY = animatedTopBarOffset
                                val progress =
                                    1f - (-animatedTopBarOffset / topBarHeightPx).coerceIn(0f, 1f)
                                alpha = progress
                            }
                            .background(NavigationBarDefaults.containerColor)
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
