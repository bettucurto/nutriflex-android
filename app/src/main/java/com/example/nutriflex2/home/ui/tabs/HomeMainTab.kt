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
    onRequestScrollToBottom: () -> Unit,
    onOpenDrawer: () -> Unit // <-- NOVO PARÂMETRO AQUI
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

        // --- CABEÇALHO ---
        // 1. Botão Hambúrguer no topo, alinhado à esquerda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp) // Reduzi ligeiramente o top para equilibrar
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

        // 2. Título ao centro, por baixo do botão
        Text(
            text = "NUTRIFLEX",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.formulacondensedbold)),
            fontSize = 115.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp) // Espaçamento entre o ícone e o texto
        )
        // -----------------------------------

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