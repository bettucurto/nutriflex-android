
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

        Text(text = "WELCOME",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.formulacondensedbold)),
            fontSize = 115.sp,
            modifier = Modifier.fillMaxWidth()
                .padding(top = 100.dp),
            color = colorScheme.surface,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.95f)
                .padding(vertical = 4.dp),
            color = colorScheme.surface,
            thickness = 1.dp
        )

        CaloriesCard(
            remainingCalories = state.dailyCalories - state.eatenCaloriesToday, // ou state.remaining se tiver essa lógica
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
