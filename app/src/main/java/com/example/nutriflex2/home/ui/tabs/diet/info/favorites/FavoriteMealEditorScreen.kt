package com.example.nutriflex2.home.ui.tabs.diet.favorites

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutriflex2.R
import components.LeftTitleText

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMealEditorScreen(
    mealId: Int? = null,
    onBack: () -> Unit,
    onAddFoodClick: () -> Unit, // Navega para Search
    onMealSaved: () -> Unit,
    viewModel: FavoriteMealEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current

    // Carregar a refeição se o mealId for fornecido
    LaunchedEffect(mealId) {
        mealId?.let { viewModel.loadMeal(it) }
    }

    // Efeito para mostrar erros ou sucesso
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Meal Saved Successfully!", Toast.LENGTH_SHORT).show()
            onMealSaved()
        }
    }

    // Scroll Logic para o Parallax
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = -available.y * 0.6f
                val newOffset = (scrollOffset + delta).coerceIn(-50f, 600f)
                scrollOffset = newOffset
                return Offset(0f, delta * 0.2f)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText(if (uiState.mealId != null) "Edit Favorite Meal" else "Create Meal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Image dinâmica
            HeroImage(
                imageUrl = uiState.coverImage ?: "https://m.ftscrt.com/static/generic-food.jpg",
                scrollOffset = scrollOffset,
                density = density,
                contentDescription = "Meal Cover"
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(colorScheme.primary, colorScheme.secondary)
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Input do Nome da Refeição
                    MealNameInput(
                        name = uiState.mealName,
                        onNameChange = { viewModel.onNameChange(it) }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Círculo de Nutrição
                    NutritionCircle(uiState)

                    Spacer(Modifier.height(32.dp))

                    // Lista de Ingredientes Selecionados
                    if (uiState.ingredients.isNotEmpty()) {
                        Text(
                            "Ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.secondary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(Modifier.height(8.dp))

                        uiState.ingredients.forEach { ingredient ->
                            IngredientRow(
                                ingredient = ingredient,
                                onRemove = { viewModel.removeIngredient(ingredient.uniqueId) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        Text(
                            "No foods added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    // Botão Adicionar Mais Alimentos
                    OutlinedButton(
                        onClick = onAddFoodClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Food")
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botão Salvar Refeição
                    Button(
                        onClick = {
                            viewModel.saveMeal()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (uiState.mealId != null) "Update Meal" else "Save Meal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))

                    // Tabela Nutricional Completa
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        FullNutritionFacts(uiState)
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun MealNameInput(name: String, onNameChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val titleColor = Color(0xFF0288D1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (name.isEmpty() && !isFocused) {
                Text(
                    text = "Name your meal",
                    style = TextStyle(
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }

            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                textStyle = TextStyle(
                    color = titleColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                ),
                singleLine = true,
                cursorBrush = SolidColor(titleColor),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onFocusChanged { focusState -> isFocused = focusState.isFocused }
            )
        }

        if (name.isNotEmpty() && !isFocused) {
            Spacer(Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = titleColor.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        } else {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun IngredientRow(
    ingredient: DraftIngredient,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ingredient.food.image,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.nutrilogo)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ingredient.food.nomeEn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${ingredient.quantity} x ${ingredient.selectedServing.description} (${(ingredient.selectedServing.calories * ingredient.quantity).toInt()} kcal)",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                tint = colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeroImage(
    imageUrl: String?,
    scrollOffset: Float,
    density: Density,
    contentDescription: String
) {
    val maxHeightPx = with(density) { 290.dp.toPx() }
    val minHeightPx = with(density) { 70.dp.toPx() }
    val minScale = 0.3f

    val heightProgress = (scrollOffset / 400f).coerceIn(0f, 1.5f)
    val currentHeight = (maxHeightPx - (heightProgress * (maxHeightPx * 0.6f)))
        .coerceAtLeast(minHeightPx)

    val scaleProgress = (scrollOffset / 350f).coerceIn(0f, 1f)
    val scale = 1f - (scaleProgress * 0.7f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(density) { currentHeight.toDp() })
            .padding(vertical = 18.dp)
            .offset(y = with(density) { (scrollOffset / 8f).toDp() }),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(scale.coerceAtLeast(minScale))
                .clip(RoundedCornerShape(20.dp))
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            error = painterResource(id = R.drawable.nutrilogo)
        )
    }
}

@Composable
private fun NutritionCircle(uiState: FavoriteMealEditorUiState) {
    val totalCals = uiState.caloriesTotal.coerceAtLeast(0.0)
    val kcalCarb = uiState.carbsTotal * 4
    val kcalFat = uiState.fatTotal * 9
    val kcalProt = uiState.proteinTotal * 4

    fun pctFrom(kcal: Double): Int =
        if (totalCals <= 0.0) 0 else ((kcal * 100) / totalCals).toInt()

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
        Spacer(Modifier.width(16.dp))

        Box(
            modifier = Modifier.size(150.dp).weight(0.45f),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize(0.95f)) {
                val strokeWidth = 12.dp.toPx()
                var startAngle = -90f

                fun sweep(pct: Int) = 360f * (pct / 100f)

                drawArc(
                    color = Color(0xFFE0E0E0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val fatSweep = sweep(fatPct)
                drawArc(
                    color = Color(0xFFFFB74D),
                    startAngle = startAngle,
                    sweepAngle = fatSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += fatSweep

                val carbSweep = sweep(carbPct)
                drawArc(
                    color = Color(0xFF4FC3F7),
                    startAngle = startAngle,
                    sweepAngle = carbSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += carbSweep

                val protSweep = sweep(protPct)
                drawArc(
                    color = Color(0xFF81C784),
                    startAngle = startAngle,
                    sweepAngle = protSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = totalCals.toInt().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.primary
                )
                Text(
                    text = "Calories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.weight(0.05f))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(0.45f)
        ) {
            MacroLegendRow(Color(0xFFFFB74D), "Fat",    uiState.fatTotal,    fatPct)
            MacroLegendRow(Color(0xFF4FC3F7), "Carbs",  uiState.carbsTotal,  carbPct)
            MacroLegendRow(Color(0xFF81C784), "Protein",uiState.proteinTotal,protPct)
        }
    }
}

@Composable
private fun MacroLegendRow(color: Color, label: String, grams: Double, percent: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text("$percent% $label", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurface)
            Text(String.format("%.2fg", grams), style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FullNutritionFacts(uiState: FavoriteMealEditorUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Nutrition Facts",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = colorScheme.secondary
        )

        HorizontalDivider(thickness = 3.dp, color = colorScheme.onSurface, modifier = Modifier.padding(vertical = 4.dp))

        NutrientLine("Total Fat", "${uiState.fatTotal.toInt()}g", isBold = true, showDivider = false)
        NutrientLine("Saturated Fat", "${uiState.saturatedFatTotal.toInt()}g", isBold = false, indent = true)

        NutrientLine("Cholesterol", "${uiState.cholesterolTotal.toInt()}mg", isBold = true)
        NutrientLine("Sodium", "${uiState.sodiumTotal.toInt()}mg", isBold = true)

        NutrientLine("Total Carbohydrate", "${uiState.carbsTotal.toInt()}g", isBold = true)
        NutrientLine("Dietary Fiber", "${uiState.fiberTotal.toInt()}g", isBold = false, indent = true)
        NutrientLine("Total Sugars", "${uiState.sugarsTotal.toInt()}g", isBold = false, indent = true)

        NutrientLine("Protein", "${uiState.proteinTotal.toInt()}g", isBold = true)

        HorizontalDivider(thickness = 3.dp, color = colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun NutrientLine(
    label: String,
    amount: String,
    isBold: Boolean = false,
    indent: Boolean = false,
    showDivider: Boolean = true
) {
    Column {
        if (showDivider) HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (indent) 16.dp else 0.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = colorScheme.onSurface, fontSize = 16.sp)
            Text(amount, fontWeight = FontWeight.Bold, color = Color(0xFF3C82F6), fontSize = 16.sp)
        }
    }
}
