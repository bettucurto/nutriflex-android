import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.components.R
import com.example.nutriflex2.home.ui.HomeUiState
import com.example.nutriflex2.home.ui.tabs.diet.LogMealSheetContent
import components.BmiCard
import components.CaloriesCard
import components.NextWorkoutCard
import components.WeightForecastCard
import components.WeightProgressCard
import components.WeightRange
import components.WeightsCardRow
import kotlinx.coroutines.launch
import theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainTab(
    state: HomeUiState,
    topBarHeightDp: Dp,
    scrollState: ScrollState,
    selectedRange: WeightRange,
    onRangeChange: (WeightRange) -> Unit,
    onNavigateToSearchMeals: () -> Unit,
    onNavigateToSearchRecipes: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onChangeCurrentWeight: (Float) -> Unit,
    onChangeGoalWeight: (Float) -> Unit,
    onRequestScrollToBottom: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    // Com Formula Condensed, 0.18f da largura do ecrã aproxima-se de 90% da width.
    val dynamicFontSize = (screenWidth * 0.28f).sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- CABEÇALHO ---
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
                    modifier = Modifier.size(AppTheme.dimens.iconMedium)
                )
            }
        }

        Text(
            text = "NUTRIFLEX",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.formulacondensedbold)),
            fontSize = dynamicFontSize,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = AppTheme.dimens.smallPadding)
        )

        CaloriesCard(
            remainingCalories = state.dailyCalories - state.eatenCaloriesToday,
            dailyCalories = state.dailyCalories,
            eatenCalories = state.eatenCaloriesToday,
            eatenProtein = state.eatenProteinGrams,
            dailyProtein = state.dailyProteinGrams,
            eatenCarbs = state.eatenCarbsGrams,
            dailyCarbs = state.dailyCarbsGrams,
            eatenFat = state.eatenFatGrams,
            dailyFat = state.dailyFatGrams,
            onAddClick = {
                showSheet = true
                scope.launch { sheetState.show() }
            }
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.largePadding))

        NextWorkoutCard(
            workoutName = state.nextWorkoutName,
            exerciseCount = state.nextWorkoutExercises,
            onStartClick = { onNavigateToTraining() }
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.largePadding))

        BmiCard(
            bmi = state.bmi,
            category = state.bmiCategory
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.largePadding))

        WeightForecastCard(
            weeks = state.weeklyProgressWeeks,
            goalWeight = state.goalWeight,
        )

        WeightProgressCard(
            selectedRange = selectedRange,
            history = state.weightHistory,
            onRangeChange = onRangeChange
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.largePadding))

        WeightsCardRow(
            currentWeight = state.currentWeight,
            goalWeight = state.goalWeight,
            onChangeCurrent = onChangeCurrentWeight,
            onChangeGoal = onChangeGoalWeight,
            onRequestScroll = { onRequestScrollToBottom() }
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.mediumPadding))
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
                onPhotoClick = { /* TODO */ },
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