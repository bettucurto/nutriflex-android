package com.example.nutriflex2.diet.detail

// Assumindo que NFButton está em components (como nos outros designs)
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutriflex2.home.ui.tabs.diet.info.FoodDetailViewModel
import components.LeftTitleText
import components.TitleText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FoodDetailScreen(
    foodId: String,
    onBack: () -> Unit,
    onAddToMeal: () -> Unit,
    viewModel: FoodDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current // Não usado, mas mantido

    Scaffold(
        topBar = {
            TopAppBar(
                title = { LeftTitleText("Dieta") }, // Ajustado para "Dieta" como na imagem
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            uiState.food?.let { food ->
                // 1. Container principal centralizado como outros cards (0.8f width)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(500.dp) // Altura aproximada para comportar tudo
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. NOME DO ALIMENTO (grande, centralizado)
                        TitleText(
                            food.nomeEn ?: "Filé de Frango Grelhado", // Exato da imagem
                        )

                        // 2. CAMPOS QUANTIDADE E PORÇÃO (exatamente igual imagem)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            var qtyText by remember { mutableStateOf(uiState.quantity.toString()) }
                            var gramsText by remember { mutableStateOf(uiState.portionGrams.toString()) }

                            // "Quantidade de porções" - largura 0.7f como imagem
                            OutlinedTextField(
                                value = qtyText,
                                onValueChange = {
                                    qtyText = it
                                    viewModel.updateQuantity(it.toIntOrNull() ?: 1)
                                },
                                label = { Text("Quantidade de porções") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .padding(bottom = 8.dp)
                            )

                            // "Tamanho da Porção" - 100g (full width)
                            OutlinedTextField(
                                value = gramsText,
                                onValueChange = {
                                    gramsText = it
                                    viewModel.updatePortionGrams(it.toDoubleOrNull() ?: 100.0)
                                },
                                label = { Text("Tamanho da Porção") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. CIRCLE NUTRIÇÃO + 3% ABAIXO (SIMPLE como imagem, não wavy)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Circle principal: "159 cal" - Surface branca com shadow
                            val totalCals = uiState.caloriesTotal
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                tonalElevation = 2.dp,
                                shadowElevation = 8.dp,
                                modifier = Modifier.size(100.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = totalCals.toInt().toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 28.sp
                                    )
                                    Text(
                                        text = "cal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3 colunas %: "0%" "15%" "85%" com C/G/P - spacedBy 24.dp
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Carbs 0% C
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${((uiState.carbsTotal * 4 * 100) / totalCals.coerceAtLeast(1)).toInt()}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "C",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Fat 15% G (ajuste cálculo se fatTotal usar 9kcal/g)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${((uiState.fatTotal * 9 * 100) / totalCals.coerceAtLeast(1)).toInt()}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "G",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Protein 85% P
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${((uiState.proteinTotal * 4 * 100) / totalCals.coerceAtLeast(1)).toInt()}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "P",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // 4. BOTÃO "Adicionar" com NFButton style (consistente com designs)
                        Button(
                            onClick = {
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Adicionar")
                            }
                        }
                    }
                }
            } ?: run {
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
