package com.example.nutriflex2.home.ui.tabs.diet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dieta.domain.FoodRecognitionUseCase
import com.example.nutriflex2.R
import components.LeftTitleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanConfirmScreen(
    initialIngredients: List<FoodRecognitionUseCase.ProcessedIngredient>,
    onBack: () -> Unit,
    onAddMore: () -> Unit,
    onCompleted: () -> Unit,
    navController: NavController,
    viewModel: ScanConfirmViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val addedFoodSelection by savedStateHandle?.getStateFlow<com.example.dieta.domain.FatSecretIngredientSelection?>(
        "added_food_selection", null
    )?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(addedFoodSelection) {
        addedFoodSelection?.let { selection ->
            viewModel.addIngredientFromFatSecret(selection.food, selection.serving, selection.quantity)
            savedStateHandle?.remove<com.example.dieta.domain.FatSecretIngredientSelection>("added_food_selection")
        }
    }

    LaunchedEffect(Unit) {
        if (state.ingredients.isEmpty() && initialIngredients.isNotEmpty()) {
            viewModel.setInitialIngredients(initialIngredients)
        }
    }

    LaunchedEffect(state.isLogged) {
        if (state.isLogged) onCompleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Confirm Detection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagem do Scan
            item {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.nutrilogo)
                )
            }

            // Resumo Nutricional (Círculo de Macros)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        NutritionCircle(state)
                    }
                }
            }

            // Cabeçalho da Lista
            item {
                Text(
                    text = "Detected Ingredients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lista de Ingredientes
            items(state.ingredients, key = { it.id }) { item ->
                IngredientRow(
                    item = item,
                    onWeightChange = { viewModel.updateWeight(item.id, it) },
                    onDelete = { viewModel.removeIngredient(item.id) }
                )
            }
            
            // Botão Add Food
            item {
                OutlinedButton(
                    onClick = onAddMore,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Food")
                }
            }

            item {
                Button(
                    onClick = { viewModel.logMeal() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Meal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Tabela Nutrition Facts e Botão Final Log Meal
            item {
                Spacer(Modifier.height(16.dp))
                FullNutritionFacts(state)
                
                Spacer(Modifier.height(24.dp))
                

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NutritionCircle(state: ScanConfirmUiState) {
    val totalCals = state.ingredients.sumOf { it.calories }.coerceAtLeast(0.0)
    val totalCarbs = state.ingredients.sumOf { it.carbs }
    val totalFat = state.ingredients.sumOf { it.fat }
    val totalProt = state.ingredients.sumOf { it.protein }

    val kcalCarb = totalCarbs * 4
    val kcalFat = totalFat * 9
    val kcalProt = totalProt * 4

    fun pctFrom(kcal: Double): Int = if (totalCals <= 0.0) 0 else ((kcal * 100) / totalCals).toInt()

    val rawCarb = pctFrom(kcalCarb)
    val rawFat  = pctFrom(kcalFat)
    val rawProt = pctFrom(kcalProt)

    val sum = rawCarb + rawFat + rawProt
    val carbPct = if (sum == 0) 0 else (rawCarb * 100f / sum).toInt()
    val fatPct  = if (sum == 0) 0 else (rawFat  * 100f / sum).toInt()
    val protPct = if (sum == 0) 0 else (rawProt * 100f / sum).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize(0.95f)) {
                val strokeWidth = 10.dp.toPx()
                var startAngle = -90f
                fun sweep(pct: Int) = 360f * (pct / 100f)

                drawArc(Color(0xFFE0E0E0), 0f, 360f, false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                
                val fatSweep = sweep(fatPct)
                drawArc(Color(0xFFFFB74D), startAngle, fatSweep, false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                startAngle += fatSweep

                val carbSweep = sweep(carbPct)
                drawArc(Color(0xFF4FC3F7), startAngle, carbSweep, false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                startAngle += carbSweep

                val protSweep = sweep(protPct)
                drawArc(Color(0xFF81C784), startAngle, protSweep, false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(totalCals.toInt().toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("kcal", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroLegendRow(Color(0xFFFFB74D), "Fat", totalFat, fatPct)
            MacroLegendRow(Color(0xFF4FC3F7), "Carbs", totalCarbs, carbPct)
            MacroLegendRow(Color(0xFF81C784), "Protein", totalProt, protPct)
        }
    }
}

@Composable
private fun MacroLegendRow(color: Color, label: String, grams: Double, percent: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text("$percent% $label", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text("${grams.toInt()}g", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FullNutritionFacts(state: ScanConfirmUiState) {
    val fat = state.ingredients.sumOf { it.fat }
    val carbs = state.ingredients.sumOf { it.carbs }
    val prot = state.ingredients.sumOf { it.protein }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Text("Nutrition Facts", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)
        NutrientLine("Total Fat", "${fat.toInt()}g", true)
        NutrientLine("Total Carbohydrate", "${carbs.toInt()}g", true)
        NutrientLine("Protein", "${prot.toInt()}g", true)
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun NutrientLine(label: String, amount: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(amount, fontWeight = FontWeight.Bold, color = Color(0xFF3C82F6))
    }
}

@Composable
fun IngredientRow(
    item: ScanConfirmIngredient,
    onWeightChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var isEditingWeight by remember { mutableStateOf(false) }
    var weightText by remember { mutableStateOf(item.weight.toInt().toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl ?: "https://m.ftscrt.com/static/generic-food.jpg",
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.nutrilogo)
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${item.weight.toInt()}g - ${item.calories.toInt()} kcal | P:${item.protein.toInt()}g C:${item.carbs.toInt()}g F:${item.fat.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { isEditingWeight = true }) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit Weight",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }

    if (isEditingWeight) {
        AlertDialog(
            onDismissRequest = { isEditingWeight = false },
            title = { Text("Edit Weight (g)") },
            text = {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val w = weightText.toDoubleOrNull() ?: item.weight
                    onWeightChange(w)
                    isEditingWeight = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingWeight = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
