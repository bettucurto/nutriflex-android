
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMainTab(
    state: HomeUiState,
    topBarHeightDp: androidx.compose.ui.unit.Dp,
    scrollState: androidx.compose.foundation.ScrollState,
    selectedRange: WeightRange,
    onRangeChange: (WeightRange) -> Unit,
    onNavigateToSearchMeals: () -> Unit,
    onNavigateToSearchRecipes: () -> Unit,
    onNavigateToTreino: () -> Unit,
    onChangeCurrentWeight: (Float) -> Unit,
    onChangeGoalWeight: (Float) -> Unit,
    onRequestScrollToBottom: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(topBarHeightDp+ 12.dp))

        CaloriesCard(
            remaining = state.remainingCalories,
            dailyTarget = state.dailyCalories,
            progress = state.progress,
            onAddClick = {
                showSheet = true
                scope.launch { sheetState.show() }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        NextWorkoutCard(
            workoutName = state.nextWorkoutName,
            exerciseCount = state.nextWorkoutExercises,
            onStartClick = { onNavigateToTreino() }
        )

        Spacer(modifier = Modifier.height(30.dp))


        BmiCard(
            bmi = state.bmi,
            category = state.bmiCategory
        )

        Spacer(modifier = Modifier.height(30.dp))


        WeightForecastCard(
            weeks = state.weeklyProgressWeeks,
            goalWeight = state.goalWeight,
        )

        Spacer(modifier = Modifier.height(5.dp))


        WeightProgressCard(
            selectedRange = selectedRange,
            history = state.weightHistory,
            onRangeChange = onRangeChange
        )

        Spacer(modifier = Modifier.height(30.dp))

        WeightsCardRow(
            currentWeight = state.currentWeight,
            goalWeight = state.goalWeight,
            onChangeCurrent = onChangeCurrentWeight,
            onChangeGoal = onChangeGoalWeight,
            onRequestScroll = { onRequestScrollToBottom() }
        )

        Spacer(modifier = Modifier.height(20.dp))
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
