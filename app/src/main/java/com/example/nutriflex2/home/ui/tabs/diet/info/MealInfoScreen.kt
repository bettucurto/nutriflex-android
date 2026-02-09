package com.example.nutriflex2.home.ui.tabs.diet.info

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.dieta.domain.FatSecretFoodDetails
import com.example.dieta.domain.FatSecretServing
import com.example.nutriflex2.R
import com.example.nutriflex2.home.ui.tabs.diet.info.MealInfoUiState
import com.example.nutriflex2.home.ui.tabs.diet.info.MealInfoViewModel
import components.HeadingTextComponent
import components.LeftTitleText
import components.TitleText

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealInfoScreen(
    foodId: String,
    onBack: () -> Unit,
    onAddToMeal: () -> Unit,
    viewModel: MealInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current

    var scrollOffset by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = -available.y * 0.6f
                val newOffset = (scrollOffset + delta).coerceIn(-50f, 600f)
                scrollOffset = newOffset
                return androidx.compose.ui.geometry.Offset(0f, delta * 0.2f)
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Diet") },
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
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleText(food.nomeEn)

                        Spacer(Modifier.height(12.dp))
                        var countText by remember(uiState.selectedServingIndex, uiState.portionCount) {
                            mutableStateOf(uiState.portionCount.toInt().toString())
                        }
                        ServingSelector(
                            servings = uiState.servings,
                            selectedIndex = uiState.selectedServingIndex,
                            onServingSelected = { index ->
                                if (uiState.servings.getOrNull(index)?.description == "Grams") {
                                    countText = "100"
                                    viewModel.onPortionCountChange(100.0)
                                }else{
                                    countText = "1"
                                    viewModel.onPortionCountChange(1.0)
                                }
                                viewModel.onServingSelected(index)
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = countText,
                            onValueChange = {
                                countText = it
                                val v = it.replace(",", ".").toDoubleOrNull() ?: 1.0
                                viewModel.onPortionCountChange(v)
                            },
                            label = { Text("Serving Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorScheme.primary,
                                unfocusedLeadingIconColor = colorScheme.onSurface,
                                unfocusedContainerColor = colorScheme.surfaceVariant,
                                unfocusedBorderColor = colorScheme.onSurface,
                                focusedLeadingIconColor = colorScheme.primary,
                                unfocusedLabelColor = colorScheme.onSurface

                            )
                        )

                        Spacer(Modifier.height(20.dp))

                        NutritionCircle(uiState)

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
                                viewModel.addToMeal()
                                onAddToMeal()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Log Meal", fontSize = 20.sp)
                        }

                        Spacer(Modifier.height(24.dp))

                        FullNutritionFacts(uiState)

                        Spacer(Modifier.height(24.dp))

                        NutritionClaimsSection(uiState)


                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        } ?: Text(
            text = uiState.error ?: "Error loading info",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        )
    }
}

@Composable
private fun NutritionClaimsSection(uiState: MealInfoUiState) {
    val food = uiState.food ?: return
    val colors = MaterialTheme.colorScheme

    fun prefValue(name: String): String? =
        food.preferences.firstOrNull { it.name.equals(name, ignoreCase = true) }?.value

    val vegValue = prefValue("Vegetarian")
    val veganValue = prefValue("Vegan")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Nutrition Claims", style = MaterialTheme.typography.titleMedium, color = colors.onSurface)

        Spacer(Modifier.height(8.dp))

        Text("This food is free from:", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)

        Spacer(Modifier.height(4.dp))

        AllergensBlock(food)

        Spacer(Modifier.height(8.dp))

        Text("This food is suitable for", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (vegValue) {
                "1"  -> ClaimRow(label = "Vegetarian", inline = true)
                "0"  -> NotSuitableRow(label = "Vegetarian")
                "-1", null -> Text(
                    text = "Vegetarian: info unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            when (veganValue) {
                "1"  -> ClaimRow(label = "Vegan diets", inline = true)
                "0"  -> NotSuitableRow(label = "Vegan diets")
                "-1", null -> Text(
                    text = "Vegan: info unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
private fun NotSuitableRow(label: String) {
    val colors = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.error),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", color = colors.onError, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$label",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface
        )
    }
}
@Composable
private fun AllergensBlock(food: FatSecretFoodDetails) {
    val colors = MaterialTheme.colorScheme

    if (food.allergens.isEmpty()) {
        Text(
            text = "No allergen info available",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
        return
    }

    food.allergens.forEach { allergen ->
        when (allergen.value) {
            "0" -> ClaimRow(label = allergen.name)
            "1" -> NotSuitableRow(label = allergen.name)
            "-1" -> Text(
                text = "${allergen.name}: info unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClaimRow(label: String, inline: Boolean = false) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (inline) Modifier else Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = colors.onPrimary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.width(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface
        )
    }
}

@Composable
private fun HeroImage(
    imageUrl: String?,
    scrollOffset: Float,
    density: androidx.compose.ui.unit.Density,
    contentDescription: String
) {
    val maxHeightPx = with(density) { 290.dp.toPx() }
    val minHeightPx = with(density) { 70.dp.toPx() }
    val minScale = 0.3f

    val heightProgress = (scrollOffset / 400f).coerceIn(0f, 1.5f)
    val currentHeight = (maxHeightPx - (heightProgress * (maxHeightPx * 0.6f)))
        .coerceAtLeast(minHeightPx)

    val scaleProgress = (scrollOffset / 350f).coerceIn(0f, 1f)
    val scale = 1f - (scaleProgress * 0.7f)  // ← Máx 70% redução

    val alphaProgress = (scrollOffset / 300f).coerceIn(0f, 1f)
    val alpha = 1f - alphaProgress

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(density) { currentHeight.toDp() })
            .padding(vertical = 8.dp)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServingSelector(
    servings: List<FatSecretServing>,
    selectedIndex: Int,
    onServingSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedServing = servings.getOrNull(selectedIndex)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth( )
    ) {
        OutlinedTextField(
            value = selectedServing?.description ?: "Select... (${servings.size} options)",
            onValueChange = { },
            readOnly = true,
            label = { Text("Serving Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedLeadingIconColor = colorScheme.onSurface,
                unfocusedContainerColor = colorScheme.surfaceVariant,
                unfocusedBorderColor = colorScheme.onSurface,
                focusedLeadingIconColor = colorScheme.primary,
                unfocusedLabelColor = colorScheme.onSurface

            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            servings.forEachIndexed { index, serving ->
                DropdownMenuItem(
                    text = {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = serving.description,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${serving.calories.toInt()} cal • ${serving.protein.toInt()}g P",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun FullNutritionFacts(uiState: MealInfoUiState) {
    val serving = uiState.servings.getOrNull(uiState.selectedServingIndex)

    Column(modifier = Modifier.fillMaxWidth()) {
        HeadingTextComponent("Nutrition Facts")

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Calories", color = colorScheme.secondary)
            Text("${uiState.caloriesTotal.toInt()}", color = colorScheme.secondary)
        }

        Divider(Modifier.padding(vertical = 4.dp))

        NutrientLine("Total Fat", "${uiState.fatTotal.toInt()}g")
        NutrientLine("  Saturated Fat", "${uiState.saturatedFatTotal.toInt()}g", true)
        NutrientLine("  Trans Fat", "0g", true)

        NutrientLine("Cholesterol", "${uiState.cholesterolTotal.toInt()}mg")
        NutrientLine("Sodium", "${uiState.sodiumTotal.toInt()}mg")

        NutrientLine("Total Carbohydrate", "${uiState.carbsTotal.toInt()}g")
        NutrientLine("  Dietary Fiber", "${uiState.fiberTotal.toInt()}g", true)
        NutrientLine("  Total Sugars", "${uiState.sugarsTotal.toInt()}g", true)

        NutrientLine("Protein", "${uiState.proteinTotal.toInt()}g")
    }
}

@Composable
private fun NutrientLine(label: String, amount: String, indent: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indent) 32.dp else 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = colorScheme.secondary)
        Text(amount, color = colorScheme.secondary)
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format("%.2fg", grams),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
