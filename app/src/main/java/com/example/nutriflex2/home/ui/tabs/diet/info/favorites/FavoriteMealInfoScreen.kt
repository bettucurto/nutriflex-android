package com.example.nutriflex2.home.ui.tabs.diet.info.favorites

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.dieta.domain.IngredienteRefeicao
import com.example.nutriflex2.R
import components.HeadingTextComponent
import components.LeftTitleText

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMealInfoScreen(
    mealId: String,
    onBack: () -> Unit,
    onLogMeal: () -> Unit,
    onIngredientClick: (String) -> Unit,
    viewModel: FavoriteMealInfoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val context = LocalContext.current

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
                title = { LeftTitleText("Custom Meal Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        uiState.meal?.let { meal ->
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
                    imageUrl = meal.image,
                    scrollOffset = scrollOffset,
                    density = density,
                    contentDescription = meal.nome
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
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HeadingTextComponent(meal.nome, textSize = 34.sp)

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
                                Toast.makeText(context, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
                                viewModel.logMeal()
                                onLogMeal()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Log Meal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(24.dp))

                        // INGREDITENTES
                        ExpandableIngredientsSection(
                            title = "Ingredients",
                            ingredients = uiState.ingredients,
                            onIngredientClick = onIngredientClick
                        )

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        } ?: Text(
            text = uiState.error ?: "Error loading info",
            color = colorScheme.error,
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        )
    }
}

@Composable
private fun ExpandableIngredientsSection(
    title: String,
    ingredients: List<IngredienteRefeicao>,
    onIngredientClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
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
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (ingredients.isEmpty()) {
                    Text("No ingredients found.", color = colorScheme.onSurfaceVariant)
                } else {
                    ingredients.forEach { ing ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onIngredientClick(ing.alimentoApiId) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${ing.quantidadePorcoes} x ${ing.tipoPorcao}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "(ID: ${ing.alimentoApiId})",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reutilização dos componentes internos (HeroImage, NutritionCircle, etc.)
// Como são idênticos ao RecipeInfoScreen, no ideal seriam extraídos para 'components'.
// Para este passo, vou colar as versões simplificadas aqui.

@Composable
private fun ServingAmountField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(56.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.surfaceVariant).padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(color = colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                cursorBrush = SolidColor(colorScheme.primary),
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.Edit, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun HeroImage(imageUrl: String?, scrollOffset: Float, density: Density, contentDescription: String) {
    val maxHeightPx = with(density) { 290.dp.toPx() }
    val minHeightPx = with(density) { 70.dp.toPx() }
    val heightProgress = (scrollOffset / 400f).coerceIn(0f, 1.5f)
    val currentHeight = (maxHeightPx - (heightProgress * (maxHeightPx * 0.6f))).coerceAtLeast(minHeightPx)
    val scale = 1f - ((scrollOffset / 350f).coerceIn(0f, 1f) * 0.7f)

    Box(
        modifier = Modifier.fillMaxWidth().height(with(density) { currentHeight.toDp() }).padding(vertical = 8.dp).offset(y = with(density) { (scrollOffset / 8f).toDp() }),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl, contentDescription = contentDescription, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(scale.coerceAtLeast(0.3f)).clip(RoundedCornerShape(20.dp)).graphicsLayer(scaleX = scale, scaleY = scale),
            error = painterResource(id = R.drawable.nutrilogo)
        )
    }
}

@Composable
private fun NutritionCircle(uiState: FavoriteMealInfoUiState) {
    val totalCals = uiState.caloriesTotal.coerceAtLeast(0.0)
    val carbPct = uiState.meal?.carbsPct ?: 0
    val fatPct = uiState.meal?.fatPct ?: 0
    val protPct = uiState.meal?.proteinPct ?: 0

    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize(0.95f)) {
            val strokeWidth = 12.dp.toPx()
            var startAngle = -90f
            fun sweep(pct: Int) = 360f * (pct / 100f)

            drawArc(color = Color(0xFFE0E0E0), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeWidth))
            
            val fatSweep = sweep(fatPct)
            drawArc(color = Color(0xFFFFB74D), startAngle = startAngle, sweepAngle = fatSweep, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            startAngle += fatSweep
            
            val carbSweep = sweep(carbPct)
            drawArc(color = Color(0xFF4FC3F7), startAngle = startAngle, sweepAngle = carbSweep, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            startAngle += carbSweep
            
            val protSweep = sweep(protPct)
            drawArc(color = Color(0xFF81C784), startAngle = startAngle, sweepAngle = protSweep, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = totalCals.toInt().toString(), style = MaterialTheme.typography.headlineMedium, color = colorScheme.primary)
            Text(text = "Calories", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
        }
    }
}
