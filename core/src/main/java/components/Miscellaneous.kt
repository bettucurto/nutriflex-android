package components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.components.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.sqrt

@Composable
fun DividerTextComponent(){
    Row(modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically){
        HorizontalDivider(modifier = Modifier.fillMaxWidth()
            .weight(1f),
            color = colorScheme.onSurface,
            thickness = 1.dp
        )

        Text(modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.divider),
            fontSize = 18.sp,
            color = colorScheme.onSurface
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth()
            .weight(1f),
            color = colorScheme.onSurface,
            thickness = 1.dp
        )
    }
}

@Composable
fun StepIndicators(
    currentStep: Int, // 0-based ou 1-based, já explico
) {
    val totalSteps = 6
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 15.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep   // se currentStep for 0-based
            Box(
                modifier = Modifier
                    .weight(5F, true)
                    .height(20.dp)
                    .clip(RoundedCornerShape(30)) // “cilindro”
                    .background(
                        if (index <= currentStep) colorScheme.primary
                        else colorScheme.outlineVariant)
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDateField(
    label: String,
    value: String?,               // "YYYY-MM-DD" ou null
    error: String? = null,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var openDialog by remember { mutableStateOf(false) }

    // Campo “só leitura” que abre o date picker
    OutlinedTextField(
        value = value ?: "",
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorScheme.primary,
            unfocusedLeadingIconColor = colorScheme.onSurface,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            unfocusedBorderColor = colorScheme.onSurface,
            focusedLeadingIconColor = colorScheme.primary,
            unfocusedLabelColor = colorScheme.onSurface,
            focusedTrailingIconColor = colorScheme.primary

        ),
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        modifier = modifier,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error, color = colorScheme.error)
            }
        },
        trailingIcon = {
            IconButton(onClick = { openDialog = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            }
        },
        visualTransformation = VisualTransformation.None,
        enabled = true,

    )

    if (openDialog) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            // converte para "YYYY-MM-DD"
                            val formatted = localDate.toString()
                            onDateSelected(formatted)
                        }
                        openDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

enum class Gender { MALE, FEMALE }
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CaloriesCard(
    remaining: Int,
    dailyTarget: Int,
    progress: Float,
    onAddClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(32.dp)

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "caloriesProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(200.dp)
    ) {
        Surface(
            shape = cardShape,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorScheme.primary,
                        colorScheme.secondary
                    )
                )
            ),
            modifier = Modifier.matchParentSize()
                .shadow(
                    elevation = 8.dp,
                    shape = cardShape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(0.5f),
                    horizontalAlignment = Alignment.Start
                ) {
                    LeftTitleText("Calories", textSize = 25.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(120.dp)
                    ) {
                        val strokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
                        val thickStroke = remember(strokeWidth) {
                            Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        }

                        CircularWavyProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            stroke = thickStroke,
                            trackStroke = thickStroke,
                            wavelength = 40.dp,           // maior = menos ondas visíveis
                            waveSpeed = 20.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            HeadingTextComponent(value=("$remaining\nremaining"), textSize = 15.sp)
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(0.5f)
                ) {
                    HeadingTextComponent("Daily Goal:", textSize = 20.sp)
                    HeadingTextComponent("$dailyTarget")
                }
            }
        }

        // Botão + sobreposto no canto inferior direito
        Surface(
            shape = CircleShape,
            shadowElevation = 8.dp,
            color = colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 0.dp, y = 20.dp)
                .size(70.dp)
        ) {
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add meal",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NutrientCircle(
    progress: Float,
    labelTop: String,
    valueText: String,
    unitText: String,
    showTextInside: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "nutrientProgress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (labelTop.isNotEmpty()) {
            Text(
                text = labelTop,
                fontSize = 18.sp,
                color = colorScheme.secondary,
                fontFamily = FontFamily(Font(R.font.audiowide)),
                textAlign = TextAlign.Center
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(94.dp)
        ) {
            val strokeWidthPx = with(LocalDensity.current) { 5.dp.toPx() }
            val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

            CircularWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(74.dp),
                stroke = stroke,
                trackStroke = stroke,
                wavelength = 24.dp,
                waveSpeed = 18.dp,
                color = colorScheme.primary,
            )

            if (showTextInside) {
                Text(
                    text = valueText,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center

                )
            }
        }

        if (unitText.isNotEmpty()) {
            Text(
                text = unitText,
                fontSize = 12.sp,
                color = colorScheme.primary,
                textAlign = TextAlign.Center

            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DietCaloriesCard(
    remainingCalories: Int,
    dailyTargetCalories: Int,
    caloriesProgress: Float,
    proteinRemaining: Int,
    proteinTarget: Int,
    proteinProgress: Float,
    carbsRemaining: Int,
    carbsTarget: Int,
    carbsProgress: Float,
    fatRemaining: Int,
    fatTarget: Int,
    fatProgress: Float,
    onAddClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(32.dp)

    val animatedCaloriesProgress by animateFloatAsState(
        targetValue = caloriesProgress.coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "dietCaloriesProgress"
    )

    val percentage = (animatedCaloriesProgress * 100).toInt().coerceIn(0, 100)

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(450.dp) // ajusta a altura se precisares de mais espaço
    ) {
        Surface(
            shape = cardShape,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp,
            color = colorScheme.surface,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorScheme.primary,
                        colorScheme.secondary
                    )
                )
            ),
            modifier = Modifier.matchParentSize()
                .padding(vertical = 6.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = cardShape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top: calories big number
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TitleText(
                        remainingCalories.toString()
                    )
                    HeadingTextComponent(
                        "Calories Remaining",
                        textColor = colorScheme.primary,
                        textSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big circle progress for calories
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    val strokeWidthPx = with(LocalDensity.current) { 8.dp.toPx() }
                    val thickStroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

                    CircularWavyProgressIndicator(
                        progress = { animatedCaloriesProgress },
                        modifier = Modifier.fillMaxSize(),
                        stroke = thickStroke,
                        trackStroke = thickStroke,
                        wavelength = 40.dp,
                        waveSpeed = 20.dp,
                        color = colorScheme.primary,
                    )

                    Text(
                        text = "$percentage%",
                        fontSize = 24.sp,
                        color = colorScheme.primary,
                        fontFamily = FontFamily(Font(R.font.audiowide)),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row with 3 macro circles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NutrientCircle(
                        progress = proteinProgress,
                        labelTop = "Protein",
                        valueText = "${proteinRemaining}g",
                        unitText = "Remaining",
                        modifier = Modifier.weight(1f)
                    )
                    NutrientCircle(
                        progress = fatProgress,
                        labelTop = "Fat",
                        valueText = "${fatRemaining}g",
                        unitText = "Remaining",
                        modifier = Modifier.weight(1f)
                    )
                    NutrientCircle(
                        progress = carbsProgress,
                        labelTop = "Carbs",
                        valueText = "${carbsRemaining}g",
                        unitText = "Remaining",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

        }

        // Botão + sobreposto no centro em baixo
        Surface(
            shape = CircleShape,
            shadowElevation = 8.dp,
            color = colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 35.dp)
                .size(90.dp)
        ) {
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add meal",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize(0.7f)
                )
            }
        }
    }
}



@Composable
fun NextWorkoutCard(
    workoutName: String,
    exerciseCount: Int,
    onStartClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(32.dp)

    Surface(
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    colorScheme.primary,
                    colorScheme.secondary
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(200.dp)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeadingTextComponent("Next Workout: ", textColor = colorScheme.primary)
            HeadingTextComponent(workoutName)
            HeadingTextComponent("$exerciseCount exercices")

            NFButton(
                text = "Start!",
                onButtonClicked = onStartClick,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun WeightsCardRow(
    currentWeight: Float,
    goalWeight: Float,
    onChangeCurrent: (Float) -> Unit,
    onChangeGoal: (Float) -> Unit,
    onRequestScroll: () -> Unit
) {
    var showCurrentDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    var currentText by remember(currentWeight) {
        mutableStateOf(currentWeight.toString())
    }
    var goalText by remember(goalWeight) {
        mutableStateOf(goalWeight.toString())
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth(0.95f)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleWeightCard(
                title = "Current Weight:",
                text = "${currentWeight}kg",
                onClick = {
                    showCurrentDialog = true      // 1) abre diálogo
                    onRequestScroll()             // 2) pede scroll (Home trata)
                }
            )

            EditWeightDialog(
                visible = showCurrentDialog,
                title = "New Weight",
                text = currentText,
                onTextChange = { currentText = it },
                onDismiss = { showCurrentDialog = false },
                onConfirm = {
                    onChangeCurrent(it)
                    showCurrentDialog = false
                }
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SingleWeightCard(
                title = "Goal Weight:",
                text = "${goalWeight}kg",
                onClick = {
                    showGoalDialog = true
                    onRequestScroll()
                }
            )

            EditWeightDialog(
                visible = showGoalDialog,
                title = "New Goal Weight",
                text = goalText,
                onTextChange = { goalText = it },
                onDismiss = { showGoalDialog = false },
                onConfirm = {
                    onChangeGoal(it)
                    showGoalDialog = false
                }
            )
        }
    }
}



@Composable
private fun SingleWeightCard(
    title: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(32.dp)

    Surface(
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    colorScheme.primary,
                    colorScheme.secondary
                )
            )
        ),
        modifier = modifier.height(140.dp)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeadingTextComponent(title, textSize = 14.sp, textColor = colorScheme.primary)
            HeadingTextComponent(text, textSize = 14.sp)

            NFButton(
                text = "Change",
                onButtonClicked = onClick,
                modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f)
            )
        }
    }
}

@Composable
fun EditWeightDialog(
    visible: Boolean,
    title: String,
    text: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(215.dp)

        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeadingTextComponent(title,textSize = 18.sp, textColor = colorScheme.primary)

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    label = { Text("Weight (kg)") },
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(0.7f)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    TextButton(onClick = {
                        text.toFloatOrNull()?.let(onConfirm)
                    }) {
                        Text("Save", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}



@Composable
fun WeightForecastCard(
    weeks: Int?,
    goalWeight: Float
) {
    val cardShape = RoundedCornerShape(32.dp)
    Surface(
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    colorScheme.primary,
                    colorScheme.secondary
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            HeadingTextComponent(
                "Your Progress",
                textSize = 16.sp,
                textColor = colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            val mainText = when {
                weeks == null -> "Not able to calculate the estimated time till goal :("
                weeks == 0 -> "Congrats! You've achieved your Weight Goal!"
                weeks == 1 -> "At this rate, you can reach ${goalWeight}kg in about 1 week!."
                else -> "At this rate, you can reach ${goalWeight}kg in about ${weeks} weeks!"
            }

            HeadingTextComponent(
                mainText,
                textSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BmiCard(
    bmi: Float,
    category: String
) {
    val cardShape = RoundedCornerShape(32.dp)

    Surface(
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 4.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    colorScheme.primary,
                    colorScheme.secondary
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),

        ) {
            LeftTitleText("Your BMI", textSize = 18.sp)

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.extraExtraLarge,
                border = BorderStroke(1.dp, colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your weight is: $category",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 14.sp,
                    color = colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))

            HeadingTextComponent(
                if (bmi > 0f) String.format("%.1f", bmi) else "--",
                textSize = 28.sp
            )

            Spacer(Modifier.height(12.dp))

            BmiColorBar(bmi = bmi)
        }
    }
}

@Composable
fun BmiColorBar(
    bmi: Float,
    minBmi: Float = 15f,
    maxBmi: Float = 35f
) {
    val clampedBmi = bmi.coerceIn(minBmi, maxBmi)
    val range = maxBmi - minBmi

    val underStart = 15f
    val underEnd   = 18.5f
    val normalEnd  = 25f
    val overEnd    = 30f
    val obeseEnd   = 35f

    val underWidth  = (underEnd - underStart) / range
    val normalWidth = (normalEnd - underEnd) / range
    val overWidth   = (overEnd - normalEnd) / range
    val obeseWidth  = (obeseEnd - overEnd) / range

    val underColor  = Color(0xFFFFF59D)
    val normalColor = Color(0xFFA5D6A7)
    val overColor   = Color(0xFFFFCC80)
    val obeseColor  = Color(0xFFFF0000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        // barra de cores com larguras proporcionais
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(7.dp))
                .border(BorderStroke(1.dp, colorScheme.outline), RoundedCornerShape(7.dp))
        ) {
            Row(Modifier.matchParentSize()) {
                Box(
                    Modifier
                        .weight(underWidth)
                        .fillMaxHeight()
                        .background(underColor)
                )
                Box(
                    Modifier
                        .weight(normalWidth)
                        .fillMaxHeight()
                        .background(normalColor)
                )
                Box(
                    Modifier
                        .weight(overWidth)
                        .fillMaxHeight()
                        .background(overColor)
                )
                Box(
                    Modifier
                        .weight(obeseWidth)
                        .fillMaxHeight()
                        .background(obeseColor)
                )
            }
        }

        // indicador na posição correta
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            val barWidthPx = constraints.maxWidth.toFloat()
            val fraction = (clampedBmi - minBmi) / range
            val xOffsetPx = fraction * barWidthPx
            val xOffsetDp = with(LocalDensity.current) { xOffsetPx.toDp() }

            Box(
                modifier = Modifier
                    .height(26.dp)
                    .width(2.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = xOffsetDp)
                    .background(colorScheme.onSurface)
            )
        }

        // marcas numéricas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labels = listOf(15, 20, 25, 30, 35)
            labels.forEach { value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(13.dp)
                            .width(1.dp)
                            .background(Color.Black)
                    )
                    Text(
                        text = value.toString(),
                        fontSize = 13.sp,
                        color = colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeightLineChart(
    points: List<WeightHistoryPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    pointColor: Color,
    selectedPointColor: Color,
    gridColor: Color,
    labelColor: Color,
    clearSelectionSignal: Int,
    labelTextStyle: TextStyle = MaterialTheme.typography.labelSmall
) {
    if (points.isEmpty()) return

    val sorted = remember(points) { points.sortedBy { it.date } }

    val minWeight = sorted.minOf { it.weight }
    val maxWeight = sorted.minOf { it.weight }.let { minW ->
        max(sorted.maxOf { it.weight }, minW + 0.1f)
    }
    val weightRange = (maxWeight - minWeight).takeIf { it > 0f } ?: 1f

    val textMeasurer = rememberTextMeasurer()

    var selectedPoint by remember { mutableStateOf<WeightHistoryPoint?>(null) }
    var selectedOffset by remember { mutableStateOf<Offset?>(null) }

    val density = LocalDensity.current
    val baseRadiusPx = with(density) { 4.dp.toPx() }
    val selectedRadiusPx = with(density) { 10.dp.toPx() }

    // animações de seleção
    val radiusAnim = remember { Animatable(baseRadiusPx) }
    val alphaAnim = remember { Animatable(0f) }
    val colorAnim = remember { Animatable(pointColor) }

    // progresso da linha 0..1, começa em 1f para mostrar logo ao abrir
    val drawProgress = remember(sorted) {
        Animatable(if (sorted.size == 1) 1f else 0f)
    }

    // PathMeasure e Paths Android reutilizáveis
    val pathMeasure = remember { android.graphics.PathMeasure() }
    val fullAndroidPath = remember { android.graphics.Path() }
    val animatedAndroidPath = remember { android.graphics.Path() }

    // 1) limpar seleção quando clearSelectionSignal muda
    LaunchedEffect(clearSelectionSignal) {
        selectedPoint = null
        selectedOffset = null
        alphaAnim.snapTo(0f)
        radiusAnim.snapTo(baseRadiusPx)
        colorAnim.snapTo(pointColor)
    }

    // 2) animar sempre que os pontos mudam (range novo carregado)
    LaunchedEffect(sorted) {
        // evita animar se só mudou a ordem ou algo mínimo
        if (sorted.size > 1) {
            drawProgress.snapTo(0f)
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = LinearEasing
                )
            )
        } else {
            // com 1 ponto só, mostra logo
            drawProgress.snapTo(1f)
        }
    }

    val scope = rememberCoroutineScope()

    Canvas(
        modifier = modifier.pointerInput(sorted) {
            detectTapGestures(
                onTap = { tapOffset ->
                    scope.launch {
                        val hitRadius = with(density) { 24.dp.toPx() }

                        val yLabelWidth = 32.dp.toPx()
                        val leftPaddingPx = yLabelWidth + 12.dp.toPx()
                        val rightPaddingPx = 16.dp.toPx()
                        val topPaddingPx = 24.dp.toPx()
                        val bottomPaddingPx = 40.dp.toPx()


                        val centers = buildCircleCentersRaw(
                            density = density,
                            totalSize = size,
                            sorted = sorted,
                            minWeight = minWeight,
                            weightRange = weightRange,
                            leftPadding = leftPaddingPx,
                            rightPadding = rightPaddingPx,
                            topPadding = topPaddingPx,
                            bottomPadding = bottomPaddingPx
                        )

                        val nearest = centers.minByOrNull { (center, _) ->
                            center.distanceTo(tapOffset)
                        }

                        if (nearest != null && nearest.first.distanceTo(tapOffset) <= hitRadius) {
                            coroutineScope {
                                launch { alphaAnim.animateTo(0f, tween(80)) }
                                launch { radiusAnim.animateTo(baseRadiusPx, tween(80)) }
                                launch { colorAnim.animateTo(pointColor, tween(80)) }
                            }

                            selectedOffset = nearest.first
                            selectedPoint = nearest.second

                            coroutineScope {
                                launch { alphaAnim.animateTo(1f, tween(180)) }
                                launch { radiusAnim.animateTo(selectedRadiusPx, tween(180)) }
                                launch { colorAnim.animateTo(selectedPointColor, tween(180)) }
                            }
                        } else {
                            coroutineScope {
                                launch { alphaAnim.animateTo(0f, tween(120)) }
                                launch { radiusAnim.animateTo(baseRadiusPx, tween(120)) }
                                launch { colorAnim.animateTo(pointColor, tween(120)) }
                            }
                            selectedOffset = null
                            selectedPoint = null
                        }
                    }
                }
            )
        }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val yLabelWidth = 32.dp.toPx() // ajusta conforme precisares
        val leftPadding = yLabelWidth + 12.dp.toPx()
        val rightPadding = 16.dp.toPx()
        val topPadding = 24.dp.toPx()
        val bottomPadding = 40.dp.toPx()

        val usableWidth = canvasWidth - leftPadding - rightPadding
        val usableHeight = canvasHeight - topPadding - bottomPadding

        val progress = drawProgress.value.coerceIn(0f, 1f)

        // grid
        repeat(4) { i ->
            val y = topPadding + usableHeight * (i / 3f)
            drawLine(
                color = gridColor.copy(alpha = 0.4f),
                start = Offset(leftPadding, y),
                end = Offset(canvasWidth - rightPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = if (sorted.size == 1) 0f
        else usableWidth / (sorted.size - 1)

        val circleCenters = mutableListOf<Pair<Offset, WeightHistoryPoint>>()

        // path completo
        fullAndroidPath.reset()
        sorted.forEachIndexed { index, point ->
            val x = leftPadding + stepX * index
            val normalizedY = (point.weight - minWeight) / weightRange
            val y = topPadding + usableHeight * (1f - normalizedY)

            val offset = Offset(x, y)
            circleCenters.add(offset to point)

            if (index == 0) {
                fullAndroidPath.moveTo(x, y)
            } else {
                fullAndroidPath.lineTo(x, y)
            }
        }

        // path parcial
        animatedAndroidPath.reset()
        pathMeasure.setPath(fullAndroidPath, false)
        val length = pathMeasure.length
        val stop = length * progress
        pathMeasure.getSegment(0f, stop, animatedAndroidPath, true)

        // desenhar linha animada
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                style = android.graphics.Paint.Style.STROKE
                color = lineColor.toArgb()
                strokeWidth = 2.dp.toPx()
                strokeCap = android.graphics.Paint.Cap.ROUND
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawPath(animatedAndroidPath, paint)
        }

        // pontos (até à posição atual)
        val currentMaxX = leftPadding + usableWidth * progress

        circleCenters.forEach { (center, point) ->
            if (center.x <= currentMaxX) {
                val isSelected = point == selectedPoint
                val radius = if (isSelected) radiusAnim.value else baseRadiusPx
                val color = if (isSelected) colorAnim.value else pointColor

                drawCircle(
                    color = color,
                    radius = radius,
                    center = center
                )
            }
        }

        // eixo Y
        val minLabel = String.format("%.1f", minWeight)
        val maxLabel = String.format("%.1f", maxWeight)
        val minLayout = textMeasurer.measure(minLabel, labelTextStyle)
        val maxLayout = textMeasurer.measure(maxLabel, labelTextStyle)

        drawText(
            textLayoutResult = minLayout,
            color = labelColor,
            topLeft = Offset(
                x = yLabelWidth - minLayout.size.width,
                y = topPadding + usableHeight - (minLayout.size.height / 2f)
            )
        )

        drawText(
            textLayoutResult = maxLayout,
            color = labelColor,
            topLeft = Offset(
                x = yLabelWidth - maxLayout.size.width,
                y = topPadding - (maxLayout.size.height / 2f)
            )
        )

        // eixo X (labels com alpha animado)
        if (sorted.isNotEmpty()) {
            val first = sorted.first()
            val last = sorted.last()

            val firstLabel = formatDateShort(first.date)
            val lastLabel = formatDateShort(last.date)

            val centerX = leftPadding + usableWidth / 2f
            val centerLabel = run {
                val tolerance = 1f
                val exact = circleCenters.minByOrNull { (offset, _) ->
                    kotlin.math.abs(offset.x - centerX)
                }
                val exactOffset = exact?.first
                val exactPoint = exact?.second

                if (exactOffset != null && kotlin.math.abs(exactOffset.x - centerX) <= tolerance) {
                    formatDateShort(exactPoint!!.date)
                } else {
                    var before: WeightHistoryPoint? = null
                    var after: WeightHistoryPoint? = null

                    for (i in 0 until circleCenters.size - 1) {
                        val (o1, p1) = circleCenters[i]
                        val (o2, p2) = circleCenters[i + 1]
                        if (o1.x <= centerX && centerX <= o2.x) {
                            before = p1
                            after = p2
                            break
                        }
                    }

                    if (before != null && after != null) {
                        middleDate(before!!.date, after!!.date)
                    } else {
                        middleDate(first.date, last.date)
                    }
                }
            }

            val firstLayout = textMeasurer.measure(firstLabel, labelTextStyle)
            val middleLayout = textMeasurer.measure(centerLabel, labelTextStyle)
            val lastLayout = textMeasurer.measure(lastLabel, labelTextStyle)

            val yLabel = topPadding + usableHeight + 8.dp.toPx()
            val xLabelColor = labelColor.copy(alpha = progress)

            drawText(
                textLayoutResult = firstLayout,
                color = xLabelColor,
                topLeft = Offset(
                    x = leftPadding - firstLayout.size.width / 2f,
                    y = yLabel
                )
            )
            drawText(
                textLayoutResult = middleLayout,
                color = xLabelColor,
                topLeft = Offset(
                    x = centerX - middleLayout.size.width / 2f,
                    y = yLabel
                )
            )
            drawText(
                textLayoutResult = lastLayout,
                color = xLabelColor,
                topLeft = Offset(
                    x = canvasWidth - rightPadding - lastLayout.size.width / 2f,
                    y = yLabel
                )
            )
        }

        // tooltip
        val selPoint = selectedPoint
        val selOffset = selectedOffset
        if (selPoint != null && selOffset != null && alphaAnim.value > 0f) {
            drawPointTooltip(
                point = selPoint,
                center = selOffset,
                textMeasurer = textMeasurer,
                labelTextStyle = labelTextStyle,
                background = Color.White,
                textAlpha = alphaAnim.value
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
private fun middleDate(start: String, end: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(start, formatter)
        val endDate = LocalDate.parse(end, formatter)
        val days = ChronoUnit.DAYS.between(startDate, endDate)
        val midDate = startDate.plusDays(days / 2)
        val outFormatter = DateTimeFormatter.ofPattern("dd/MM")
        midDate.format(outFormatter)
    } catch (e: Exception) {
        "" // fallback se algo correr mal
    }
}

private fun buildCircleCentersRaw(
    density: Density,
    totalSize: IntSize,
    sorted: List<WeightHistoryPoint>,
    minWeight: Float,
    weightRange: Float,
    leftPadding: Float,
    rightPadding: Float,
    topPadding: Float,
    bottomPadding: Float
): List<Pair<Offset, WeightHistoryPoint>> = with(density) {
    val usableWidth = totalSize.width.toFloat() - leftPadding - rightPadding
    val usableHeight = totalSize.height.toFloat() - topPadding - bottomPadding

    val stepX = if (sorted.size == 1) 0f
    else usableWidth / (sorted.size - 1)

    sorted.mapIndexed { index, point ->
        val x = leftPadding + stepX * index
        val normalizedY = (point.weight - minWeight) / weightRange
        val y = topPadding + usableHeight * (1f - normalizedY)
        Offset(x, y) to point
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}

private fun DrawScope.drawPointTooltip(
    point: WeightHistoryPoint,
    center: Offset,
    textMeasurer: TextMeasurer,
    labelTextStyle: TextStyle,
    background: Color,
    textAlpha: Float
) {
    val weightText = String.format("%.1f kg", point.weight)
    val dateText = formatDateShort(point.date)

    val weightLayout = textMeasurer.measure(weightText, labelTextStyle)
    val dateLayout = textMeasurer.measure(dateText, labelTextStyle)

    val paddingH = 8.dp.toPx()
    val paddingV = 4.dp.toPx()
    val arrowWidth = 10.dp.toPx()
    val arrowHeight = 8.dp.toPx()

    val boxWidth = max(weightLayout.size.width, dateLayout.size.width) + paddingH * 2
    val boxHeight = weightLayout.size.height + dateLayout.size.height + paddingV * 3

    val margin = 4.dp.toPx()
    val horizontalMargin = 4.dp.toPx()
    val topSafeMargin = 8.dp.toPx()
    val bottomSafeMargin = size.height - 8.dp.toPx()

    // decide se desenha acima (true) ou abaixo (false)
    val drawAbove = center.y > (topSafeMargin + boxHeight + arrowHeight)

    // centro na horizontal em relação ao ponto, mas sem sair do gráfico
    val boxLeft = (center.x - boxWidth / 2f)
        .coerceAtLeast(horizontalMargin)
        .coerceAtMost(size.width - boxWidth - horizontalMargin)

    val boxTop = if (drawAbove) {
        (center.y - boxHeight - arrowHeight - margin).coerceAtLeast(topSafeMargin)
    } else {
        (center.y + arrowHeight + margin).coerceAtMost(bottomSafeMargin - boxHeight)
    }

    val boxRect = Rect(Offset(boxLeft, boxTop), Size(boxWidth, boxHeight))

    // clamp da posição horizontal da seta para não sair da caixa
    val arrowX = center.x.coerceIn(
        boxRect.left + arrowWidth / 2f,
        boxRect.right - arrowWidth / 2f
    )

    // path da caixa + seta (com arrowX clamped)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = boxRect,
                cornerRadius = CornerRadius(6.dp.toPx())
            )
        )
        if (drawAbove) {
            // seta a apontar para baixo (ponta abaixo da caixa)
            moveTo(arrowX, boxRect.bottom + arrowHeight)
            lineTo(arrowX - arrowWidth / 2f, boxRect.bottom)
            lineTo(arrowX + arrowWidth / 2f, boxRect.bottom)
        } else {
            // seta a apontar para cima (ponta acima da caixa)
            moveTo(arrowX, boxRect.top - arrowHeight)
            lineTo(arrowX - arrowWidth / 2f, boxRect.top)
            lineTo(arrowX + arrowWidth / 2f, boxRect.top)
        }
        close()
    }

    // sombra suave (deslocada) — versão correta
    val shadowOffset = Offset(1.5.dp.toPx(), 1.5.dp.toPx())
    val shadowPath = Path().apply {
        // adiciona o path original já deslocado
        addPath(path, shadowOffset)
    }
    drawPath(
        path = shadowPath,
        color = Color.Black.copy(alpha = 0.18f * textAlpha)
    )

    // fundo da tooltip
    drawPath(
        path = path,
        color = background
    )

    // texto (alinhado à esquerda dentro da caixa)
    val textX = boxRect.left + paddingH
    var textY = boxRect.top + paddingV

    val textColor = Color.Black.copy(alpha = textAlpha)

    drawText(
        textLayoutResult = weightLayout,
        color = textColor,
        topLeft = Offset(textX, textY)
    )
    textY += weightLayout.size.height + paddingV / 2f
    drawText(
        textLayoutResult = dateLayout,
        color = textColor,
        topLeft = Offset(textX, textY)
    )
}

private fun formatDateShort(date: String): String {
    return try {
        val parts = date.split("-")
        val day = parts[2]
        val month = parts[1]
        "$day/$month"
    } catch (e: Exception) {
        date
    }
}


@Composable
fun WeightProgressCard(
    selectedRange: WeightRange,
    history: List<WeightHistoryPoint>,
    onRangeChange: (WeightRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(32.dp)

    val pointCount = history.size
    var clearSelectionSignal by remember { mutableStateOf(0) }

    Surface(
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    colorScheme.primary,
                    colorScheme.secondary
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth(0.95f)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            HeadingTextComponent("Your Weight Progress", textSize = 16.sp)

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(
                        width = 2.dp,
                        color = colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (pointCount == 0) {
                    Text("No data yet, lets start!", color = colorScheme.onSurface)
                } else {
                    WeightLineChart(
                        points = history,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(12.dp),
                        lineColor = colorScheme.primary,
                        pointColor = colorScheme.primary,
                        selectedPointColor = colorScheme.secondary,
                        gridColor = colorScheme.onSurface,
                        labelColor = colorScheme.onSurface,
                        clearSelectionSignal = clearSelectionSignal
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeightRangeButton(
                    text = "2 weeks",
                    selected = selectedRange == WeightRange.TWO_WEEKS,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.TWO_WEEKS)
                    },
                    modifier = Modifier.weight(1f, fill = false)
                )
                WeightRangeButton(
                    text = "1 month",
                    selected = selectedRange == WeightRange.ONE_MONTH,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.ONE_MONTH)
                    },
                     modifier = Modifier.weight(1f, fill = false)
                )
                WeightRangeButton(
                    text = "3 months",
                    selected = selectedRange == WeightRange.THREE_MONTHS,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.THREE_MONTHS)
                    },
                     modifier = Modifier.weight(1f, fill = false)
                )
                WeightRangeButton(
                    text = "All Time",
                    selected = selectedRange == WeightRange.ALL_TIME,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.ALL_TIME)
                    },
                     modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}



@Composable
private fun WeightRangeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            2.dp,
            if (selected) colorScheme.primary else colorScheme.outline
        ),
        modifier = modifier
            .height(36.dp)
            .padding(horizontal = 2.dp)
            .clickable(onClick = onClick)   // garante click aqui
    ) {
        BoxWithConstraints(
            modifier = modifier
                .heightIn(min = 36.dp)
                .padding(horizontal = 2.dp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val dynamicFontSize = when {
                maxWidth < 60.dp -> 9.sp
                maxWidth < 80.dp -> 10.sp
                else -> 12.sp
            }

            Text(
                modifier = Modifier
                    .padding(horizontal = 5.dp),
                text = text,
                fontSize = dynamicFontSize,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (selected) colorScheme.primary else colorScheme.onSurface
            )
        }
    }
}


enum class WeightRange { TWO_WEEKS, ONE_MONTH, THREE_MONTHS, ALL_TIME }

data class WeightHistoryPoint(
    val date: String,
    val weight: Float
)


@Composable
fun MacroCard(
    label: String,
    remainingGrams: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(2.dp, colorScheme.outline),
        modifier = modifier
            .fillMaxWidth(0.4f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeadingTextComponent(
                value = "${remainingGrams}g",
                textSize = 18.sp
            )
            HeadingTextComponent(
                value = label,
                textSize = 14.sp
            )
            HeadingTextComponent(
                value = "Remaining",
                textSize = 12.sp
            )
        }
    }
}

