package components

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.BreakfastDining
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.components.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
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
@Composable
fun CaloriesCard(
    remainingCalories: Int,
    dailyCalories: Int,
    eatenCalories: Int,

    eatenProtein: Int,
    dailyProtein: Int,

    eatenCarbs: Int,
    dailyCarbs: Int,

    eatenFat: Int,
    dailyFat: Int,

    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Cores dos Macros (Mantemos fixas pois são cores de dados/identidade visual)
    // Elas são brilhantes o suficiente para funcionar bem no Dark e Light mode
    val greenColor = Color(0xFF5BC895)
    val carbColor = Color(0xFF3896C7)
    val fatColor = Color(0xFFFBB040)
    val primaryColor = colorScheme.primary


    // Cores Dinâmicas (Mudam com o tema)
    // Surface: Branco no Light, Cinza escuro/Preto no Dark
    val cardContainerColor = MaterialTheme.colorScheme.surface
    // OnSurface: Preto no Light, Branco no Dark
    val titleTextColor = MaterialTheme.colorScheme.onSurface
    // OnSurfaceVariant: Cinza no Light, Cinza claro no Dark
    val subtitleTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    // OutlineVariant: Cor sutil para bordas e trilhos
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy()
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy()

    // Cálculo do progresso
    val calorieProgress = if (dailyCalories > 0) {
        eatenCalories.toFloat() / dailyCalories.toFloat()
    } else 0f

    val animatedCalorieProgress by animateFloatAsState(
        targetValue = calorieProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "mainProgress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth(0.90f)
            .height(IntrinsicSize.Min)
            .clickable { onAddClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. DECORAÇÃO DE FUNDO (Lado Esquerdo)
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                val decorationWidth = width * 0.45f // Ocupa aprox 40% da largura

                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(decorationWidth, 0f)
                    // Curva suave (Bézier quadrática) para criar o formato orgânico
                    quadraticBezierTo(
                        decorationWidth * 1.25f, // Ponto de controle puxa para a esquerda
                        height / 2f,
                        decorationWidth,        // Volta para a largura original
                        height
                    )
                    lineTo(0f, height)
                    close()
                }

                // Desenha a forma com opacidade muito baixa
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.4f) // Bem suave
                )
            }

            // 2. CONTEÚDO PRINCIPAL
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Círculo de Calorias (Esquerda) ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(0.45f)
                ) {
                    val strokeWidth = 12.dp

                    Canvas(modifier = Modifier.size(140.dp)) {
                        // Círculo "Glow" atrás do anel (agora branco ou surface, ou levemente verde)
                        // Como já temos o fundo decorativo verde, este círculo pode ser
                        // a cor do container para "limpar" a área ou um brilho extra.
                        // Vamos usar um brilho branco/surface com alpha para destacar o gráfico do fundo decorativo
                        drawCircle(
                            color = cardContainerColor.copy(alpha = 0.6f),
                            radius = size.minDimension / 2.0f,
                            center = center
                        )

                        // Trilho
                        drawArc(
                            color = titleTextColor.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                        // Progresso
                        drawArc(
                            color = greenColor,
                            startAngle = -90f,
                            sweepAngle = 360 * animatedCalorieProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$remainingCalories",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            // Usa Primary no light mode (geralmente azul/roxo) ou OnSurface no dark
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "kcal remaining",
                            fontSize = 12.sp,
                            color = subtitleTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // --- Lista de Macros (Direita) ---
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.End
                ) {
                    MacroRow(
                        label = "Protein",
                        value = "${eatenProtein}g",
                        max = dailyProtein,
                        current = eatenProtein,
                        color = greenColor,
                        icon = Icons.Outlined.FitnessCenter,
                        textColor = titleTextColor,
                        valueColor = subtitleTextColor,
                        trackColor = trackColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MacroRow(
                        label = "Carbs",
                        value = "${eatenCarbs}g",
                        max = dailyCarbs,
                        current = eatenCarbs,
                        color = carbColor,
                        icon = Icons.Outlined.BreakfastDining,
                        textColor = titleTextColor,
                        valueColor = subtitleTextColor,
                        trackColor = trackColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MacroRow(
                        label = "Fats",
                        value = "${eatenFat}g",
                        max = dailyFat,
                        current = eatenFat,
                        color = fatColor,
                        icon = Icons.Outlined.WaterDrop,
                        textColor = titleTextColor,
                        valueColor = subtitleTextColor,
                        trackColor = trackColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroRow(
    label: String,
    value: String,
    max: Int,
    current: Int,
    color: Color,
    icon: ImageVector,
    textColor: Color,
    valueColor: Color,
    trackColor: Color
) {
    val progress = if (max > 0) current.toFloat() / max.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "macroProgress"
    )

    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color, // Ícone colorido mantém identidade visual
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor // Cor dinâmica (Preto/Branco)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor // Cor dinâmica (Cinza/Cinza Claro)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = trackColor, // Track dinâmico
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
fun NutrientCircle(
    eaten: Int,
    target: Int,
    label: String,
    color: Color, // Cor base para o cartão e o progresso
    modifier: Modifier = Modifier
) {
    // Calcula o progresso (evita divisão por zero)
    val progress = if (target > 0) eaten.toFloat() / target.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "nutrientProgress"
    )

    Column(
        modifier = modifier
            .width(100.dp) // Largura fixa para ficarem iguais
            .clip(RoundedCornerShape(16.dp)) // Borda arredondada do cartão
            .background(color.copy(alpha = 0.1f)) // Fundo suave baseado na cor
            .padding(vertical = 18.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Círculo com Texto dentro
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
        ) {
            // AQUI ESTÁ A CORREÇÃO:
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthPx = 6.dp.toPx()

                // 1. Definimos o estilo EXATAMENTE igual para ambos
                val strokeStyle = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

                // 2. Definimos a posição e o tamanho para que as pontas redondas não sejam cortadas
                val arcOffset = Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

                // Trilho de fundo
                drawArc(
                    color = color.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcOffset,
                    size = arcSize,
                    style = strokeStyle
                )

                // Arco de Progresso
                drawArc(
                    color = color,
                    startAngle = -90f, // Começa do topo
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcOffset,
                    size = arcSize,
                    style = strokeStyle
                )
            }

            // Textos centrais
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gramas Comidos (Grande)
                Text(
                    text = "${eaten}g",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                // Total (Pequeno)
                Text(
                    text = "/${target}g",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rótulo (Protein, Carbs, Fat)
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
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

    val proteinEaten = (proteinTarget - proteinRemaining).coerceAtLeast(0)
    val carbsEaten = (carbsTarget - carbsRemaining).coerceAtLeast(0)
    val fatEaten = (fatTarget - fatRemaining).coerceAtLeast(0)

    val contentColor = colorScheme.secondary

    val cardShape = RoundedCornerShape(32.dp)

    // Animação suave do progresso
    val animatedCaloriesProgress by animateFloatAsState(
        targetValue = caloriesProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "dietCaloriesProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.90f)
            .height(520.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = cardShape,
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            color = colorScheme.surface,
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 35.dp) // Espaço para o botão flutuante não cortar
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HeadingTextComponent("Calories", textSize = 32.sp, textColor = colorScheme.primary)


                // --- INÍCIO DO GAUGE (ARCO) ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp) // Tamanho do arco geral
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Configurações do Arco
                        val strokeWidth = 25.dp.toPx()
                        val arcRadius = size.minDimension / 2 - strokeWidth
                        val startAngle = 140f // Começa as 8h
                        val sweepAngle = 260f // Percorre até as 4h

                        // 1. Desenhar o fundo tracejado (Track)
                        // Desenhamos várias linhas pequenas radialmente
                        val dashCount = 60 // Quantidade de tracinhos
                        val angleStep = sweepAngle / dashCount
                        val dashLength = 15.dp.toPx()

                        // Opcional: Se preferir arco contínuo tracejado, descomente abaixo e remova o loop
                        /*
                        drawArc(
                            color = contentColor.copy(alpha = 0.3f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f))
                        )
                        */

                        // Desenhando estilo "Réguas" como parece ser na imagem (fundo)
                        for (i in 0..dashCount) {
                            val currentAngle = startAngle + (i * angleStep)
                            val angleRad = Math.toRadians(currentAngle.toDouble())

                            val innerRadius = arcRadius - (strokeWidth / 2)
                            val outerRadius = arcRadius + (strokeWidth / 2)

                            val startX = center.x + innerRadius * kotlin.math.cos(angleRad)
                            val startY = center.y + innerRadius * kotlin.math.sin(angleRad)
                            val endX = center.x + outerRadius * kotlin.math.cos(angleRad)
                            val endY = center.y + outerRadius * kotlin.math.sin(angleRad)

                            drawLine(
                                color = contentColor.copy(alpha = 0.2f),
                                start = Offset(startX.toFloat(), startY.toFloat()),
                                end = Offset(endX.toFloat(), endY.toFloat()),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // 2. Desenhar o Progresso Sólido (Branco)
                        drawArc(
                            color = contentColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle * animatedCaloriesProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                            size = Size(arcRadius * 2, arcRadius * 2)
                        )

                        // 3. (Opcional) Desenhar a "tampinha" no final do progresso se quiser aquele traço perpendicular
                        val progressAngleRad = Math.toRadians((startAngle + sweepAngle * animatedCaloriesProgress).toDouble())
                        val endX = center.x + arcRadius * kotlin.math.cos(progressAngleRad).toFloat()
                        val endY = center.y + arcRadius * kotlin.math.sin(progressAngleRad).toFloat()

                        // Se quiser desenhar o marcador branco no final:
                        drawLine(
                            color = contentColor,
                            start = Offset(
                                x = endX + (strokeWidth/1.8f) * kotlin.math.cos(progressAngleRad).toFloat(),
                                y = endY + (strokeWidth/1.8f) * kotlin.math.sin(progressAngleRad).toFloat()
                            ),
                            end = Offset(
                                x = endX - (strokeWidth/1.8f) * kotlin.math.cos(progressAngleRad).toFloat(),
                                y = endY - (strokeWidth/1.8f) * kotlin.math.sin(progressAngleRad).toFloat()
                            ),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Conteúdo de Texto no Meio do Arco
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Número Grande (Calorias Consumidas ou Restantes)
                        // A imagem mostra "545", assumindo que é caloriesProgress ou similar
                        Text(
                            text = (dailyTargetCalories * animatedCaloriesProgress).toInt().toString(), // Ou use 'remainingCalories' se preferir o inverso
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            // fontFamily = FontFamily(Font(R.font.audiowide)), // Se tiver a fonte
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Linha de baixo: Ícone + Texto
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment, // Ícone de fogo
                                contentDescription = null,
                                tint = Color(0xFFFFC107), // Cor amarela/laranja do fogo
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$remainingCalories kcal left",
                                fontSize = 16.sp,
                                color = contentColor.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // --- FIM DO GAUGE ---

                // Row with 3 macro circles
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Espalha os cards
                    verticalAlignment = Alignment.Top
                ) {
                    // Proteína (Usando Primary)
                    NutrientCircle(
                        eaten = proteinEaten,
                        target = proteinTarget,
                        label = "Protein",
                        color = colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )

                    // Carboidratos (Usando Secondary)
                    NutrientCircle(
                        eaten = carbsEaten,
                        target = carbsTarget,
                        label = "Carbs",
                        color = colorScheme.secondary,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )

                    // Gordura (Usando Tertiary ou uma variação se Tertiary não existir)
                    NutrientCircle(
                        eaten = fatEaten,
                        target = fatTarget,
                        label = "Fat",
                        color = colorScheme.tertiary, // Certifique-se que seu tema tem Tertiary, ou use colorScheme.error/outline
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }
            }
        }
            Spacer(modifier = Modifier.height(18.dp))
        // Botão + sobreposto no centro em baixo
        Surface(
            shape = CircleShape,
            shadowElevation = 8.dp,
            color = colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 25.dp)
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
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Gradiente do botão (Azul para Verde)
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0062BD), // Azul
            Color(0xFF5BC895)  // Verde
        )
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth(0.90f)
            .height(220.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Imagem de Fundo com BLUR
            Image(
                painter = painterResource(id = R.drawable.gym),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 4.dp) // Adiciona o efeito de desfoque
            )

            // 2. Overlay Escuro (Scrim)
            // Aumentei ligeiramente a opacidade (0.6f) para contrastar melhor com o blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            // 3. Conteúdo de Texto
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = "Next Workout:",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = workoutName,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$exerciseCount exercises",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // 4. Botão "Start Workout" com Ícone
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp) // Um pouco mais alto para acomodar bem o ícone
                    .background(brush = buttonGradient, shape = CircleShape)
                    .clip(CircleShape)
            ) {
                // Row para alinhar Ícone + Texto no centro
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircleOutline, // Ícone similar à imagem
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Workout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
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

    // Row contendo os dois cartões
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(0.90f)
    ) {
        // Coluna 1: Peso Atual
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SingleWeightCard(
                title = "Current Weight:",
                text = "${currentWeight}kg",
                onClick = { showCurrentDialog = true }
            )
        }

        // Coluna 2: Peso Meta
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SingleWeightCard(
                title = "Goal Weight:",
                text = "${goalWeight}kg",
                onClick = { showGoalDialog = true }
            )
        }
    }

    // Dialog para Peso Atual
    if (showCurrentDialog) {
        WeightPickerDialog(
            initialWeight = currentWeight,
            title = "Your Current Weight",
            onDismiss = { showCurrentDialog = false },
            onConfirm = { newWeight ->
                onChangeCurrent(newWeight)
                showCurrentDialog = false
            }
        )
    }

    // Dialog para Peso Meta
    if (showGoalDialog) {
        WeightPickerDialog(
            initialWeight = goalWeight,
            title = "Your Goal Weight",
            onDismiss = { showGoalDialog = false },
            onConfirm = { newWeight ->
                onChangeGoal(newWeight)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun WeightPickerDialog(
    initialWeight: Float,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var currentWeight by remember { mutableFloatStateOf(initialWeight) }
    var showManualInput by remember { mutableStateOf(false) }
    var manualValue by remember { mutableStateOf(currentWeight.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (showManualInput) {
                    OutlinedTextField(
                        value = manualValue,
                        onValueChange = { manualValue = it },
                        label = { Text("Enter Weight (kg)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = colorScheme.primary
                        )
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showManualInput = false }) {
                            Text("Back to scale", color = colorScheme.primary)
                        }
                    }
                } else {
                    // Régua/Escala
                    WeightScalePicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        initialValue = currentWeight,
                        onValueChange = { currentWeight = it }
                    )

                    // Texto do valor selecionado (Clicável para input manual)
                    Surface(
                        onClick = { 
                            manualValue = String.format("%.1f", currentWeight).replace(",", ".")
                            showManualInput = true 
                        },
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = String.format("%.1f kg", currentWeight),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit manual",
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botões de Ação
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botão OK
                    Button(
                        onClick = {
                            val finalVal = if (showManualInput) {
                                manualValue.replace(",", ".").toFloatOrNull() ?: currentWeight
                            } else {
                                currentWeight
                            }
                            val rounded = (finalVal * 10).roundToInt() / 10f
                            onConfirm(rounded)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = "OK", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botão Cancel
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightScalePicker(
    modifier: Modifier = Modifier,
    initialValue: Float,
    range: ClosedFloatingPointRange<Float> = 30f..200f,
    onValueChange: (Float) -> Unit
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { 15.dp.toPx() } // Espaço entre traços (100g)
    
    val scrollOffset = remember { Animatable(initialValue * 10f * spacingPx) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialValue) {
        val targetOffset = initialValue * 10f * spacingPx
        if (kotlin.math.abs(scrollOffset.value - targetOffset) > 1f && !scrollOffset.isRunning) {
            scrollOffset.snapTo(targetOffset)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .background(Color.White)
            .pointerInput(Unit) {
                val velocityTracker = VelocityTracker()
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            val newOffset = (scrollOffset.value - dragAmount)
                                .coerceIn(range.start * 10f * spacingPx, range.endInclusive * 10f * spacingPx)
                            scrollOffset.snapTo(newOffset)
                            onValueChange(newOffset / (10f * spacingPx))
                        }
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().x
                        scope.launch {
                            // REDUZIDA A FRICÇÃO: de 1.2f para 0.5f para deslizar mais tempo
                            val decay = exponentialDecay<Float>(frictionMultiplier = 0.5f)
                            scrollOffset.animateDecay(-velocity, decay)
                            
                            val finalWeight = scrollOffset.value / (10f * spacingPx)
                            val snappedWeight = (finalWeight * 10).roundToInt() / 10f
                            scrollOffset.animateTo(
                                snappedWeight * 10f * spacingPx,
                                spring(stiffness = Spring.StiffnessLow)
                            )
                            onValueChange(snappedWeight)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val centerX = width / 2f

        // TextPaint para os números
        val textPaint = remember {
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.CENTER
                textSize = with(density) { 14.sp.toPx() }
                isAntiAlias = true
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Linha central fixa (Vermelha)
            drawLine(
                color = Color(0xFFFF5252),
                start = Offset(centerX, 10.dp.toPx()),
                end = Offset(centerX, height - 20.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Valor atual em tempo real
            val currentWeight = scrollOffset.value / (10f * spacingPx)
            
            // Desenhar os traços num intervalo visível ao redor do centro
            val startWeight = (currentWeight - (centerX / spacingPx / 10f)).toInt().coerceAtLeast(range.start.toInt())
            val endWeight = (currentWeight + (centerX / spacingPx / 10f)).toInt().coerceAtMost(range.endInclusive.toInt())

            for (w in (startWeight * 10)..(endWeight * 10)) {
                val tickWeight = w / 10f
                if (tickWeight !in range) continue

                // Posição X: Centro + (Diferença de peso * pixels por kg)
                val x = centerX + (tickWeight - currentWeight) * 10f * spacingPx

                val isMajor = w % 10 == 0
                val isHalf = w % 5 == 0 && !isMajor
                
                val tickHeight = when {
                    isMajor -> 45.dp.toPx()
                    isHalf -> 30.dp.toPx()
                    else -> 20.dp.toPx()
                }
                
                val alpha = (1f - (kotlin.math.abs(x - centerX) / centerX)).coerceIn(0f, 1f)

                drawLine(
                    color = (if (isMajor) Color.Black else Color.Gray).copy(alpha = alpha),
                    start = Offset(x, (height - tickHeight) / 2f),
                    end = Offset(x, (height + tickHeight) / 2f),
                    strokeWidth = (if (isMajor) 2.dp.toPx() else 1.dp.toPx())
                )

                if (isMajor) {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawText(
                            tickWeight.toInt().toString(),
                            x,
                            (height + tickHeight) / 2f + 20.dp.toPx(),
                            textPaint.apply { this.alpha = (alpha * 255).toInt() }
                        )
                    }
                }
            }
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
        modifier = modifier
            .height(140.dp)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = colorScheme.primary,
                spotColor = colorScheme.primary
            )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeadingTextComponent(title, textSize = 24.sp, textColor = colorScheme.primary)
            HeadingTextComponent(text, textSize = 20.sp)

            NFButton(
                text = "Change",
                onButtonClicked = onClick,
                modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f)
            )
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
            .fillMaxWidth(0.90f)
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
                textSize = 26.sp,
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
                textSize = 16.sp
            )
        }
    }
}

@Composable
fun BmiCard(
    bmi: Float,
    category: String // Mantido para compatibilidade
) {
    val cardShape = RoundedCornerShape(32.dp)

    Surface(
        shape = cardShape,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(0.90f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BMI Result",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(46.dp))

            BmiGauge(
                bmi = bmi,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }
    }
}

@Composable
fun BmiGauge(
    bmi: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val needleColor = colorScheme.onSurface

    // Intervalos visuais fixos (45 graus cada)
    val sliceSize = 45f

    // Limites reais do IMC
    val minBmi = 10f
    val limitUnder = 18.5f
    val limitHealthy = 25f
    val limitOver = 30f
    val maxBmi = 40f

    val animatedBmi by animateFloatAsState(
        targetValue = bmi.coerceIn(minBmi, maxBmi),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "bmiAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // --- CONFIGURAÇÃO GEOMÉTRICA ---
                val strokeWidth = 40.dp.toPx() // Um pouco mais grosso para destacar
                val arcPadding = strokeWidth
                val arcDiameter = width - (arcPadding * 2)
                val radius = arcDiameter / 2
                val centerOffset = Offset(width / 2, height - 10.dp.toPx())

                // --- NOVAS CORES (Mais contraste) ---
                val colorUnder = Color(0xFFFDD835) // Amarelo Vibrante
                val colorHealthy = Color(0xFF4CAF50) // Verde
                val colorOver = Color(0xFFFF5722)    // "Deep Orange" (Bem diferente do amarelo)
                val colorObese = Color(0xFFC62828)   // Vermelho Escuro

                val slices = listOf(
                    Triple(colorUnder, "Underweight", 180f),
                    Triple(colorHealthy, "Healthy", 225f),
                    Triple(colorOver, "Overweight", 270f),
                    Triple(colorObese, "Obese", 315f)
                )

                // --- LOOP DE DESENHO (ARCOS + TEXTO CENTRALIZADO) ---
                slices.forEach { (color, label, startAngle) ->

                    // 1. Desenhar o Arco Colorido
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sliceSize, // 45 graus
                        useCenter = false,
                        topLeft = Offset(width / 2 - radius, centerOffset.y - radius),
                        size = Size(arcDiameter, arcDiameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )

                    // 2. Desenhar a Legenda (Matematicamente Centralizada)
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            this.color = textColor
                            this.textSize = with(density) { 12.sp.toPx() } // Tamanho legível
                            this.textAlign = Paint.Align.LEFT // Importante: Desenhamos do inicio do offset calculado
                            this.typeface = Typeface.DEFAULT_BOLD
                            this.alpha = 200
                        }

                        // Criar um caminho específico APENAS para esta fatia
                        val path = android.graphics.Path()
                        // Raio do texto: logo abaixo da barra colorida
                        val textRadius = radius - (strokeWidth / 2) - 12.dp.toPx()

                        path.addArc(
                            centerOffset.x - textRadius,
                            centerOffset.y - textRadius,
                            centerOffset.x + textRadius,
                            centerOffset.y + textRadius,
                            startAngle, // Começa no angulo da fatia
                            sliceSize   // Tem o tamanho da fatia (45)
                        )

                        // Medir o texto para centralizar
                        val textWidth = paint.measureText(label)
                        // Calcular o comprimento do arco desta fatia específica
                        val pathLength = (Math.PI * textRadius * sliceSize / 180).toFloat()
                        // Offset horizontal: (Tamanho do Arco - Tamanho do Texto) / 2
                        val hOffset = (pathLength - textWidth) / 2

                        canvas.nativeCanvas.drawTextOnPath(label, path, hOffset, 0f, paint)
                    }
                }

                // --- AGULHA (Mantida a lógica que funciona) ---
                val needleAngleFrom180 = when {
                    animatedBmi < limitUnder -> {
                        val ratio = (animatedBmi - minBmi) / (limitUnder - minBmi)
                        ratio * sliceSize
                    }
                    animatedBmi < limitHealthy -> {
                        val ratio = (animatedBmi - limitUnder) / (limitHealthy - limitUnder)
                        (ratio * sliceSize) + sliceSize
                    }
                    animatedBmi < limitOver -> {
                        val ratio = (animatedBmi - limitHealthy) / (limitOver - limitHealthy)
                        (ratio * sliceSize) + (sliceSize * 2)
                    }
                    else -> {
                        val ratio = (animatedBmi - limitOver) / (maxBmi - limitOver)
                        val finalRatio = ratio.coerceAtMost(1f)
                        (finalRatio * sliceSize) + (sliceSize * 3)
                    }
                }

                val finalNeedleAngle = 180f + needleAngleFrom180
                val needleRad = Math.toRadians(finalNeedleAngle.toDouble())

                val needleLength = radius - 10.dp.toPx()
                val needleEnd = Offset(
                    x = centerOffset.x + needleLength * cos(needleRad).toFloat(),
                    y = centerOffset.y + needleLength * sin(needleRad).toFloat()
                )

                drawLine(
                    color = needleColor,
                    start = centerOffset,
                    end = needleEnd,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawCircle(color = needleColor, radius = 6.dp.toPx(), center = centerOffset)
            }
        }

        HorizontalDivider(Modifier.fillMaxWidth(0.95f), color =colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))

        // --- TEXTO EM BAIXO ---
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(colorScheme.primary.copy(0.4f), RoundedCornerShape(26.dp))
                .fillMaxWidth()
                .padding(6.dp)
                ) {
            Text(
                text = String.format("%.1f", bmi),
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface
            )

            val (label, color) = when {
                bmi < 18.5 -> "Underweight" to Color(0xFFFDD835)
                bmi < 25.0 -> "Healthy" to Color(0xFF4CAF50)
                bmi < 30.0 -> "Overweight" to Color(0xFFFF5722) // Laranja Forte
                else -> "Obese" to Color(0xFFC62828)
            }

            Text(
                modifier=  Modifier
                    .background(color, RoundedCornerShape(26.dp))
                    .padding(vertical = 6.dp, horizontal = 20.dp),
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
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
    val baseRadiusPx = with(density) { 5.dp.toPx() } // Ligeiramente maior
    val glowRadiusPx = with(density) { 10.dp.toPx() } // Raio do brilho
    val selectedRadiusPx = with(density) { 8.dp.toPx() }

    // Animações de seleção
    val radiusAnim = remember { Animatable(baseRadiusPx) }
    val alphaAnim = remember { Animatable(0f) }
    val colorAnim = remember { Animatable(pointColor) }

    // Progresso da linha 0..1
    val drawProgress = remember(sorted) {
        Animatable(if (sorted.size == 1) 1f else 0f)
    }

    // Paths reutilizáveis
    val pathMeasure = remember { android.graphics.PathMeasure() }
    val fullAndroidPath = remember { android.graphics.Path() }
    val animatedAndroidPath = remember { android.graphics.Path() }

    // Path para o gradiente de preenchimento
    val fillPath = remember { android.graphics.Path() }

    // 1) Limpar seleção
    LaunchedEffect(clearSelectionSignal) {
        selectedPoint = null
        selectedOffset = null
        alphaAnim.snapTo(0f)
        radiusAnim.snapTo(baseRadiusPx)
        colorAnim.snapTo(pointColor)
    }

    // 2) Animar ao carregar
    LaunchedEffect(sorted) {
        if (sorted.size > 1) {
            drawProgress.snapTo(0f)
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        } else {
            drawProgress.snapTo(1f)
        }
    }

    val scope = rememberCoroutineScope()

    Canvas(
        modifier = modifier.pointerInput(sorted) {
            detectTapGestures(
                onTap = { tapOffset ->
                    scope.launch {
                        val hitRadius = with(density) { 30.dp.toPx() }
                        // Recalcular offsets para hit-test (mesma lógica do draw)
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

        val yLabelWidth = 32.dp.toPx()
        val leftPadding = yLabelWidth + 12.dp.toPx()
        val rightPadding = 16.dp.toPx()
        val topPadding = 24.dp.toPx()
        val bottomPadding = 40.dp.toPx()

        val usableWidth = canvasWidth - leftPadding - rightPadding
        val usableHeight = canvasHeight - topPadding - bottomPadding

        val progress = drawProgress.value.coerceIn(0f, 1f)

        // --- GRID ---
        repeat(4) { i ->
            val y = topPadding + usableHeight * (i / 3f)
            drawLine(
                color = gridColor.copy(alpha = 0.3f), // Grid mais sutil
                start = Offset(leftPadding, y),
                end = Offset(canvasWidth - rightPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = if (sorted.size == 1) 0f else usableWidth / (sorted.size - 1)
        val circleCenters = mutableListOf<Pair<Offset, WeightHistoryPoint>>()
        val pointsCoordinates = mutableListOf<Offset>()

        // Calcular coordenadas
        sorted.forEachIndexed { index, point ->
            val x = leftPadding + stepX * index
            val normalizedY = (point.weight - minWeight) / weightRange
            val y = topPadding + usableHeight * (1f - normalizedY)
            val offset = Offset(x, y)
            circleCenters.add(offset to point)
            pointsCoordinates.add(offset)
        }

        // --- CONSTRUIR PATH SUAVE (CUBIC BÉZIER) ---
        fullAndroidPath.reset()
        if (pointsCoordinates.isNotEmpty()) {
            fullAndroidPath.moveTo(pointsCoordinates.first().x, pointsCoordinates.first().y)

            for (i in 0 until pointsCoordinates.size - 1) {
                val p1 = pointsCoordinates[i]
                val p2 = pointsCoordinates[i + 1]

                // Lógica simples de suavização: Pontos de controle no meio do caminho horizontalmente
                val controlPoint1X = (p1.x + p2.x) / 2f
                val controlPoint1Y = p1.y
                val controlPoint2X = (p1.x + p2.x) / 2f
                val controlPoint2Y = p2.y

                fullAndroidPath.cubicTo(
                    controlPoint1X, controlPoint1Y,
                    controlPoint2X, controlPoint2Y,
                    p2.x, p2.y
                )
            }
        }

        // --- CALCULAR PATH PARCIAL (ANIMAÇÃO) ---
        animatedAndroidPath.reset()
        pathMeasure.setPath(fullAndroidPath, false)
        val length = pathMeasure.length
        val stop = length * progress
        pathMeasure.getSegment(0f, stop, animatedAndroidPath, true)

        // Descobrir a posição X atual da animação para fechar o gradiente corretamente
        val currentEndPos = FloatArray(2)
        val currentTan = FloatArray(2)
        pathMeasure.getPosTan(stop, currentEndPos, currentTan)
        val currentX = if (progress < 1f) currentEndPos[0] else (leftPadding + usableWidth)

        // --- DESENHAR PREENCHIMENTO (GRADIENTE) ---
        // Criar um path fechado para o gradiente
        fillPath.set(animatedAndroidPath)
        fillPath.lineTo(currentX, canvasHeight - bottomPadding) // desce até o eixo X
        fillPath.lineTo(leftPadding, canvasHeight - bottomPadding) // volta para o início
        fillPath.close()

        val fillBrush = Brush.verticalGradient(
            colors = listOf(
                lineColor.copy(alpha = 0.4f), // Cor forte no topo
                lineColor.copy(alpha = 0.05f), // Quase transparente no meio
                Color.Transparent            // Transparente em baixo
            ),
            startY = topPadding,
            endY = canvasHeight - bottomPadding
        )

        drawPath(
            path = fillPath.asComposePath(),
            brush = fillBrush
        )

        // --- DESENHAR LINHA ---
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                color = lineColor.toArgb()
                strokeWidth = 3.dp.toPx() // Linha um pouco mais grossa
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
                // Sombra suave na linha para dar destaque
                setShadowLayer(4.dp.toPx(), 0f, 2.dp.toPx(), lineColor.copy(alpha = 0.3f).toArgb())
            }
            canvas.nativeCanvas.drawPath(animatedAndroidPath, paint)
        }

        // --- DESENHAR PONTOS ---
        // Só desenha pontos que já foram cobertos pela animação da linha
        val currentMaxX = leftPadding + usableWidth * progress

        circleCenters.forEach { (center, point) ->
            if (center.x <= currentMaxX + 1f) { // +1f margem de erro float
                val isSelected = point == selectedPoint

                // Valores de animação ou padrão
                val radius = if (isSelected) radiusAnim.value else baseRadiusPx
                val color = if (isSelected) colorAnim.value else pointColor

                // 1. "Glow" atrás do ponto (sempre desenha um brilho suave)
                drawCircle(
                    color = lineColor.copy(alpha = 0.3f), // Brilho da mesma cor da linha
                    radius = if(isSelected) selectedRadiusPx * 2 else glowRadiusPx,
                    center = center
                )

                // 2. O Ponto sólido central
                drawCircle(
                    color = if(isSelected) color else Color.White, // Centro branco (comum em gráficos clean) ou cor selecionada
                    radius = radius,
                    center = center
                )

                // 3. Borda do ponto (se o centro for branco)
                if (!isSelected) {
                    drawCircle(
                        color = lineColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // --- EIXO Y (Min/Max Labels) ---
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

        // --- EIXO X (Datas: Início, Meio, Fim) ---
        if (sorted.isNotEmpty()) {
            val first = sorted.first()
            val last = sorted.last()

            val firstLabel = formatDateShort(first.date)
            val lastLabel = formatDateShort(last.date)

            val centerX = leftPadding + usableWidth / 2f

            // Lógica para encontrar a data do meio
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
                    // Encontra os pontos vizinhos ao meio geométrico
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
                        middleDate(before.date, after.date)
                    } else {
                        middleDate(first.date, last.date)
                    }
                }
            }

            val firstLayout = textMeasurer.measure(firstLabel, labelTextStyle)
            val middleLayout = textMeasurer.measure(centerLabel, labelTextStyle)
            val lastLayout = textMeasurer.measure(lastLabel, labelTextStyle)

            val yLabel = topPadding + usableHeight + 12.dp.toPx()
            val xLabelColor = labelColor.copy(alpha = progress) // Labels aparecem com a linha

            drawText(
                textLayoutResult = firstLayout,
                color = xLabelColor,
                topLeft = Offset(
                    x = leftPadding - firstLayout.size.width / 2f,
                    y = yLabel
                )
            )

            // Só desenha o meio se não sobrepor
            if (centerX - middleLayout.size.width/2 > leftPadding + firstLayout.size.width &&
                centerX + middleLayout.size.width/2 < canvasWidth - rightPadding - lastLayout.size.width) {
                drawText(
                    textLayoutResult = middleLayout,
                    color = xLabelColor,
                    topLeft = Offset(
                        x = centerX - middleLayout.size.width / 2f,
                        y = yLabel
                    )
                )
            }

            drawText(
                textLayoutResult = lastLayout,
                color = xLabelColor,
                topLeft = Offset(
                    x = canvasWidth - rightPadding - lastLayout.size.width / 2f,
                    y = yLabel
                )
            )
        }

        // --- TOOLTIP ---
        val selPoint = selectedPoint
        val selOffset = selectedOffset
        if (selPoint != null && selOffset != null && alphaAnim.value > 0f) {
            drawPointTooltip(
                point = selPoint,
                center = selOffset,
                textMeasurer = textMeasurer,
                labelTextStyle = labelTextStyle,
                background = Color.White,
                textAlpha = alphaAnim.value,
                // Passando o tamanho do canvas para o tooltip calcular limites
                canvasSize = size
            )
        }
    }
}

// --- FUNÇÕES AUXILIARES MANTIDAS/ADAPTADAS ---

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
        ""
    }
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

// Nota: A função buildCircleCentersRaw original estava correta, mas certifique-se
// de que os paddings passados correspondem aos usados dentro do Canvas.
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

// Ligeira adaptação no Tooltip para receber o canvasSize
private fun DrawScope.drawPointTooltip(
    point: WeightHistoryPoint,
    center: Offset,
    textMeasurer: TextMeasurer,
    labelTextStyle: TextStyle,
    background: Color,
    textAlpha: Float,
    canvasSize: Size
) {
    val weightText = String.format("%.1f kg", point.weight)
    val dateText = formatDateShort(point.date)

    val weightLayout = textMeasurer.measure(weightText, labelTextStyle.copy(fontWeight = FontWeight.Bold))
    val dateLayout = textMeasurer.measure(dateText, labelTextStyle.copy(color = Color.Gray))

    val paddingH = 8.dp.toPx()
    val paddingV = 6.dp.toPx()
    val arrowWidth = 12.dp.toPx()
    val arrowHeight = 8.dp.toPx()

    val boxWidth = max(weightLayout.size.width.toFloat(), dateLayout.size.width.toFloat()) + paddingH * 2
    val boxHeight = weightLayout.size.height.toFloat() + dateLayout.size.height.toFloat() + paddingV * 2.5f

    val margin = 6.dp.toPx()
    val topSafeMargin = 8.dp.toPx()
    val bottomSafeMargin = canvasSize.height - 8.dp.toPx()

    val drawAbove = center.y > (topSafeMargin + boxHeight + arrowHeight)

    val boxLeft = (center.x - boxWidth / 2f)
        .coerceAtLeast(margin)
        .coerceAtMost(canvasSize.width - boxWidth - margin)

    val boxTop = if (drawAbove) {
        (center.y - boxHeight - arrowHeight - margin).coerceAtLeast(topSafeMargin)
    } else {
        (center.y + arrowHeight + margin).coerceAtMost(bottomSafeMargin - boxHeight)
    }

    val boxRect = Rect(Offset(boxLeft, boxTop), Size(boxWidth, boxHeight))

    val arrowX = center.x.coerceIn(
        boxRect.left + arrowWidth,
        boxRect.right - arrowWidth
    )

    val path = Path().apply {
        addRoundRect(RoundRect(rect = boxRect, cornerRadius = CornerRadius(8.dp.toPx())))

        if (drawAbove) {
            moveTo(arrowX, boxRect.bottom + arrowHeight)
            lineTo(arrowX - arrowWidth / 2f, boxRect.bottom)
            lineTo(arrowX + arrowWidth / 2f, boxRect.bottom)
        } else {
            moveTo(arrowX, boxRect.top - arrowHeight)
            lineTo(arrowX - arrowWidth / 2f, boxRect.top)
            lineTo(arrowX + arrowWidth / 2f, boxRect.top)
        }
        close()
    }

    // Sombra do Tooltip
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.2f * textAlpha),
        style = Fill,
        colorFilter = null,
        blendMode = BlendMode.SrcOver
    )
    // Pequeno offset para "levantar" a tooltip visualmente não é fácil sem blur,
    // então desenhamos a sombra levemente deslocada se desejado, ou apenas confiamos na cor.

    drawPath(path = path, color = background.copy(alpha = textAlpha))
    drawPath(path = path, color = Color.LightGray.copy(alpha = 0.5f * textAlpha), style = Stroke(width = 1f))

    val textX = boxRect.left + paddingH
    var textY = boxRect.top + paddingV

    drawText(
        textLayoutResult = weightLayout,
        color = Color.Black.copy(alpha = textAlpha),
        topLeft = Offset(textX, textY)
    )
    textY += weightLayout.size.height + paddingV / 2f
    drawText(
        textLayoutResult = dateLayout,
        color = Color.Black.copy(alpha = textAlpha),
        topLeft = Offset(textX, textY)
    )
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
            .fillMaxWidth(0.90f)
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = colorScheme.primary,
                spotColor = colorScheme.primary
            )

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {

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

            Spacer(Modifier.height(16.dp)) // Espaço um pouco maior entre o gráfico e os botões

            // ROW DOS BOTÕES COM ESPAÇAMENTO UNIFORME
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Cria o espaço exato entre eles
            ) {
                WeightRangeButton(
                    text = "2W", // Abreviações como na imagem (1W, 1M, 3M, All)
                    selected = selectedRange == WeightRange.TWO_WEEKS,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.TWO_WEEKS)
                    },
                    modifier = Modifier.weight(1f)
                )
                WeightRangeButton(
                    text = "1M",
                    selected = selectedRange == WeightRange.ONE_MONTH,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.ONE_MONTH)
                    },
                    modifier = Modifier.weight(1f)
                )
                WeightRangeButton(
                    text = "3M",
                    selected = selectedRange == WeightRange.THREE_MONTHS,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.THREE_MONTHS)
                    },
                    modifier = Modifier.weight(1f)
                )
                WeightRangeButton(
                    text = "All",
                    selected = selectedRange == WeightRange.ALL_TIME,
                    onClick = {
                        clearSelectionSignal++
                        onRangeChange(WeightRange.ALL_TIME)
                    },
                    modifier = Modifier.weight(1f)
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
    // Cores baseadas no tema: Primária para selecionado, Cinza suave para não selecionado
    val primaryColor = colorScheme.primary
    val unselectedBgColor = colorScheme.onSurface.copy(alpha = 0.08f)

    Box(
        modifier = modifier
            .height(36.dp)
            .then(
                if (selected) {
                    // Efeito de "Glow" (Sombra verde ao redor)
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = primaryColor,
                        spotColor = primaryColor
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .background(if (selected) primaryColor else unselectedBgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) colorScheme.onPrimary else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

