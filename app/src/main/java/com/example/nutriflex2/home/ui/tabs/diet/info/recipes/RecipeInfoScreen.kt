package com.example.nutriflex2.home.ui.tabs.diet.info.recipes

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutriflex2.R
import components.HeadingTextComponent
import components.LeftTitleText

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeInfoScreen(
    recipeId: String,
    onBack: () -> Unit,
    onAddToMeal: () -> Unit,
    onIngredientClick: (String) -> Unit, // <- ADICIONADO AQUI
    viewModel: RecipeInfoViewModel = hiltViewModel()
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
                title = { LeftTitleText("Recipe Info") },
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

        uiState.recipe?.let { recipe ->
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
                    imageUrl = recipe.images.firstOrNull(),
                    scrollOffset = scrollOffset,
                    density = density,
                    contentDescription = recipe.name
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
                        HeadingTextComponent(recipe.name, textSize = 34.sp)

                        Spacer(Modifier.height(16.dp))

                        var countText by remember(uiState.portionCount) {
                            mutableStateOf(uiState.portionCount.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Number of Servings",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface
                            )
                            ServingAmountField(
                                value = countText,
                                onValueChange = {
                                    countText = it
                                    val v = it.replace(",", ".").toDoubleOrNull() ?: 1.0
                                    viewModel.onPortionCountChange(v)
                                },
                                modifier = Modifier.width(120.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        NutritionCircle(uiState)

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Recipe Logged Successfully!", Toast.LENGTH_SHORT).show()
                                viewModel.addToMeal()
                                onAddToMeal()
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
                            Text("Log Recipe", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(24.dp))

                        // INGREDITENTES
                        val ingredientsList = recipe.ingredients.map {
                            // No FatSecret, um foodId de "0" significa que é um ingrediente que não tem página detalhada
                            val fId = if (it.foodId == "0" || it.foodId.isBlank()) null else it.foodId
                            ExpandableItemData(text = it.ingredientDescription, clickId = fId)
                        }
                        ExpandableSection(
                            title = "Ingredients",
                            items = ingredientsList,
                            onItemClick = onIngredientClick
                        )

                        Spacer(Modifier.height(16.dp))

                        // DIREÇÕES/PASSOS
                        val directionsList = recipe.directions.map {
                            ExpandableItemData(text = it.description, clickId = null) // Passos nunca são clicáveis
                        }
                        ExpandableSection(
                            title = "Directions",
                            items = directionsList,
                            isNumbered = true
                        )

                        Spacer(Modifier.height(24.dp))

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

// Secção Expansível para Ingredientes e Passos
// Importação nova necessária no topo do ficheiro (caso não tenhas):
// import androidx.compose.ui.text.style.TextDecoration

// Nova classe para guardar a informação da lista
data class ExpandableItemData(
    val text: String,
    val clickId: String? = null // Se for null, não é clicável
)

// Secção Expansível Atualizada
@Composable
private fun ExpandableSection(
    title: String,
    items: List<ExpandableItemData>,
    isNumbered: Boolean = false,
    onItemClick: ((String) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand/Collapse",
                tint = colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (items.isEmpty()) {
                    Text(
                        text = "No details available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                } else {
                    items.forEachIndexed { index, item ->
                        val isClickable = item.clickId != null && onItemClick != null

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                // Torna a linha clicável se tiver um ID válido
                                .then(
                                    if (isClickable) Modifier.clickable { onItemClick!!(item.clickId!!) }
                                    else Modifier
                                )
                                .padding(vertical = if (isClickable) 4.dp else 0.dp) // Dá um bocadinho mais de espaço para ser mais fácil de clicar com o dedo
                        ) {
                            if (isNumbered) {
                                Text(
                                    text = "${index + 1}. ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                // Se for clicável, usa a cor principal e sublinha o texto (como um link)
                                color = if (isClickable) colorScheme.primary else colorScheme.onSurface,
                                textDecoration = if (isClickable) androidx.compose.ui.text.style.TextDecoration.Underline else null
                            )
                        }
                    }
                }
            }
        }
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

@Composable
private fun FullNutritionFacts(uiState: RecipeInfoUiState) {
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

@Composable
private fun NutritionCircle(uiState: RecipeInfoUiState) {
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