package com.example.nutriflex2.home.ui.tabs.diet.info.meals

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.dieta.domain.FatSecretFoodDetails
import com.example.dieta.domain.FatSecretServing
import com.example.nutriflex2.R
import components.HeadingTextComponent
import components.LeftTitleText

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealInfoScreen(
    foodId: String,
    onBack: () -> Unit,
    // Se for NULL, funciona como antes (Log no diário).
    // Se tiver valor, devolve os dados para o ecrã anterior.
    onReturnIngredient: ((FatSecretFoodDetails, FatSecretServing, Double) -> Unit)? = null,
    onAddToMealCompleted: () -> Unit,
    viewModel: MealInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current

    var scrollOffset by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
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
                title = { LeftTitleText("Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        uiState.food?.let { food ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .nestedScroll(nestedScrollConnection)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                HeroImage(
                    imageUrl = food.image,
                    scrollOffset = scrollOffset,
                    density = density,
                    contentDescription = food.nomeEn
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
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeadingTextComponent(food.nomeEn, textSize = 34.sp)

                        Spacer(Modifier.height(16.dp))

                        var countText by remember(uiState.selectedServingIndex, uiState.portionCount) {
                            mutableStateOf(uiState.portionCount.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ServingSelector(
                                servings = uiState.servings,
                                selectedIndex = uiState.selectedServingIndex,
                                onServingSelected = { index ->
                                    if (uiState.servings.getOrNull(index)?.description == "Grams") {
                                        countText = "100"
                                        viewModel.onPortionCountChange(100.0)
                                    } else {
                                        countText = "1"
                                        viewModel.onPortionCountChange(1.0)
                                    }
                                    viewModel.onServingSelected(index)
                                },
                                modifier = Modifier.weight(0.6f)
                            )

                            ServingAmountField(
                                value = countText,
                                onValueChange = {
                                    countText = it
                                    val v = it.replace(",", ".").toDoubleOrNull() ?: 1.0
                                    viewModel.onPortionCountChange(v)
                                },
                                modifier = Modifier.weight(0.4f)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        NutritionCircle(uiState)

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                // LÓGICA HÍBRIDA AQUI
                                if (onReturnIngredient != null) {
                                    // MODO RASCUNHO: Devolve os dados para o SharedViewModel
                                    uiState.food?.let { food ->
                                        val serving = uiState.servings.getOrNull(uiState.selectedServingIndex)
                                        if (serving != null) {
                                            onReturnIngredient(food, serving, uiState.portionCount)
                                            onAddToMealCompleted()
                                        }
                                    }
                                } else {
                                    // MODO NORMAL: Loga no diário (comportamento antigo)
                                    Toast.makeText(context, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
                                    viewModel.addToMeal()
                                    onAddToMealCompleted()
                                }
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
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (onReturnIngredient != null) "Add to Meal" else "Log Meal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            FullNutritionFacts(uiState)
                            NutritionClaimsSection(uiState)
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        } ?: Text(
            text = uiState.error ?: "Error loading info",
            color = colorScheme.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        )
    }
}


@Composable
private fun ServingAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                cursorBrush = SolidColor(colorScheme.primary),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit amount",
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServingSelector(
    servings: List<FatSecretServing>,
    selectedIndex: Int,
    onServingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedServing = servings.getOrNull(selectedIndex)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedServing?.description ?: "Select...",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select serving type",
                    tint = colorScheme.onSurfaceVariant
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colorScheme.surface)
        ) {
            servings.forEachIndexed { index, serving ->
                DropdownMenuItem(
                    text = {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = serving.description,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "${serving.calories.toInt()} cal • ${serving.protein.toInt()}g P",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onServingSelected(index)
                        expanded = false
                    }
                )
            }
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

    val alpha = 1f

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
                    alpha = alpha,
                    scaleX = scale,
                    scaleY = scale
                ),
            error = painterResource(id = R.drawable.nutrilogo)
        )
    }
}

@Composable
private fun FullNutritionFacts(uiState: MealInfoUiState) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Nutrition Facts",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = colorScheme.secondary
        )

        HorizontalDivider(thickness = 3.dp, color = colorScheme.onSurface, modifier = Modifier.padding(vertical = 4.dp))

        NutrientLine("Total Fat", "${uiState.fatTotal.toInt()}g", isBold = true, showDivider = false)
        NutrientLine("Saturated Fat", "${uiState.saturatedFatTotal.toInt()}g", isBold = false, indent = true)
        NutrientLine("Trans Fat", "0g", isBold = false, indent = true)

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
        if (showDivider) {
            HorizontalDivider(thickness = 1.dp, color = colorScheme.outlineVariant)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (indent) 16.dp else 0.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = colorScheme.onSurface,
                fontSize = 16.sp
            )
            Text(
                text = amount,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3C82F6),
                fontSize = 16.sp
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NutritionClaimsSection(uiState: MealInfoUiState) {
    val food = uiState.food ?: return

    // Lista de claims estrita fornecida por ti
    val allowedAllergens = listOf("Egg", "Fish", "Gluten", "Nuts", "Peanuts", "Shellfish", "Soy", "Sesame", "Milk", "Lactose")
    val allowedPreferences = listOf("Vegetarian", "Vegan")

    val validClaims = mutableListOf<Pair<String, Color>>()

    // Preferências: se valor for "1" (Verdadeiro), adiciona.
    food.preferences.forEach { pref ->
        if (pref.value == "1") {
            // Verifica ignorando maiúsculas/minúsculas
            val matchedPref = allowedPreferences.firstOrNull { it.equals(pref.name, ignoreCase = true) }
            if (matchedPref != null) {
                // Vegetarian e Vegan = Verde
                validClaims.add(Pair(matchedPref, Color(0xFF5FC16D)))
            }
        }
    }

    // Alergénios: se valor for "0" (Livre de), adiciona como "{Nome} Free".
    food.allergens.forEach { allergen ->
        if (allergen.value == "0") {
            val matchedAllergen = allowedAllergens.firstOrNull { it.equals(allergen.name, ignoreCase = true) }
            if (matchedAllergen != null) {
                val claimText = "$matchedAllergen Free"
                val color = getAllergenColor(matchedAllergen)
                validClaims.add(Pair(claimText, color))
            }
        }
    }

    if (validClaims.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = "Nutrition Claims",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            validClaims.forEach { (claimText, color) ->
                ClaimChip(text = claimText, backgroundColor = color)
            }
        }
    }
}

// Associa cada alergénio da tua lista estrita a uma cor específica
private fun getAllergenColor(allergen: String): Color {
    return when (allergen.lowercase()) {
        "milk", "lactose" -> Color(0xFF3A82F0) // Azul
        "gluten" -> Color(0xFFEEA245) // Laranja
        "egg" -> Color(0xFFF1D24C) // Amarelo
        "nuts", "peanuts" -> Color(0xFFA3785D) // Castanho
        "soy" -> Color(0xFFEE5661) // Vermelho/Rosa
        "fish", "shellfish" -> Color(0xFF28A2B8) // Teal/Ciano Escuro para marisco/peixe
        "sesame" -> Color(0xFFD4A373) // Cor de semente (Bege/Castanho claro)
        else -> Color.Gray // Fallback de segurança, embora a lista já esteja filtrada
    }
}

@Composable
private fun ClaimChip(text: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NutritionCircle(uiState: MealInfoUiState) {
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

    val colors = colorScheme

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
                val diameter = size.minDimension
                val arcSize = Size(diameter, diameter)
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
                    color = colors.primary
                )
                Text(
                    text = "Calories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
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
private fun MacroLegendRow(
    color: Color,
    label: String,
    grams: Double,
    percent: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .padding(0.dp)
                .then(Modifier)
                .background(color)
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                text = "$percent% $label",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = String.format("%.2fg", grams),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}