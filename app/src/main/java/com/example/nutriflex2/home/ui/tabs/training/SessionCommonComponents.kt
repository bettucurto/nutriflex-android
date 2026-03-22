package com.example.nutriflex2.home.ui.tabs.training

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SessionExercisesRow(
    exercises: List<ExerciseUiModel>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAddNew: () -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(exercises) { index, exercise ->
            ExerciseSelectorIcon(
                name = exercise.name,
                imageUrl = exercise.imageUrl,
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
        item {
            AddNewExerciseButton(onClick = onAddNew)
        }
    }
}

@Composable
fun ExerciseSelectorIcon(
    name: String,
    imageUrl: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color(0xFF4CAF50) else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp)
        ) {
            AsyncImage(
                model = imageUrl ?: "https://via.placeholder.com/150",
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF4CAF50) else Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun AddNewExerciseButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp).clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawCircle(color = Color.LightGray, style = stroke)
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "ADD NEW",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExerciseDetailCard(
    exercise: ExerciseUiModel,
    onNotesChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, Double?, Int?, Int?) -> Unit,
    onRemoveExercise: () -> Unit,
    onSetClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = exercise.imageUrl ?: "https://via.placeholder.com/150",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )
                Text(
                    text = "${exercise.name} (${exercise.muscleGroup})",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(
                    onClick = onRemoveExercise,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Exercise", tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "EXERCISE NOTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    BasicTextField(
                        value = exercise.notes,
                        onValueChange = onNotesChange,
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (exercise.notes.isEmpty()) {
                                Text("Add coaching tips or session focus...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            WorkoutSetTable(
                sets = exercise.sets,
                onUpdateSet = onUpdateSet,
                onSetClick = onSetClick
            )

            Button(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD SET", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun WorkoutSetTable(
    sets: List<SetUiModel>,
    onUpdateSet: (Int, Double?, Int?, Int?) -> Unit,
    onSetClick: (Int) -> Unit
) {
    val headerColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text("SET", modifier = Modifier.weight(0.15f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Text("WEIGHT", modifier = Modifier.weight(0.25f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Text("MIN (REPS)", modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
            Text("MAX (REPS)", modifier = Modifier.weight(0.3f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = headerColor)
        }

        sets.forEachIndexed { index, set ->
            val setDisplayText = if (set.type == SetType.WARMUP) "W" else (sets.take(index + 1).count { it.type == SetType.REGULAR }).toString()
            WorkoutSetRow(
                index = index,
                set = set,
                setDisplayText = setDisplayText,
                onUpdateSet = onUpdateSet,
                onSetClick = { onSetClick(index) }
            )
        }
    }
}

@Composable
fun WorkoutSetRow(
    index: Int,
    set: SetUiModel,
    setDisplayText: String,
    onUpdateSet: (Int, Double?, Int?, Int?) -> Unit,
    onSetClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = if (index == 0) 1.dp else 0.dp,
                brush = if (index == 0) Brush.horizontalGradient(listOf(primaryColor, secondaryColor)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(0.15f)
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onSetClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = setDisplayText,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (setDisplayText == "W") secondaryColor else if (index == 0) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
        }

        SetInputField(
            value = if (set.weightKg == 0.0) "" else set.weightKg.toString(),
            onValueChange = { onUpdateSet(index, it.toDoubleOrNull(), null, null) },
            modifier = Modifier.weight(0.25f)
        )

        SetInputField(
            value = if (set.repsMin == 0) "" else set.repsMin.toString(),
            onValueChange = { onUpdateSet(index, null, it.toIntOrNull(), null) },
            modifier = Modifier.weight(0.3f)
        )

        SetInputField(
            value = if (set.repsMax == 0) "" else set.repsMax.toString(),
            onValueChange = { onUpdateSet(index, null, null, it.toIntOrNull()) },
            modifier = Modifier.weight(0.3f)
        )
    }
}

@Composable
fun SetInputField(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun FinishWorkoutButton(onClick: () -> Unit, visible: Boolean, text: String = "Finish Workout") {
    if (!visible) return
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp))
            .background(
                brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor)),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun EmptySessionContent(onAddFirstExercise: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp).background(Brush.radialGradient(colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent))),
            contentAlignment = Alignment.Center
        ) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(48.dp), tint = primaryColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Your session is empty", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Ready to crush it? Add your first exercise to start building your custom routine.", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onAddFirstExercise, modifier = Modifier.height(56.dp).fillMaxWidth(0.85f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Exercise", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
