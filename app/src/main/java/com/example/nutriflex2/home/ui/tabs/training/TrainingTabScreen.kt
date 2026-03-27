package com.example.nutriflex2.home.ui.tabs.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.components.R
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao
import theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingTabScreen(
    scrollState: ScrollState,
    onOpenDrawer: () -> Unit,
    navController: androidx.navigation.NavController? = null,
    viewModel: TrainingTabViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    
    // LazyListState para scroll automático
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Refresh data when screen is composed
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    // Scroll automático quando uma nova pasta entra em modo de edição
    LaunchedEffect(state.editingFolderId, state.isNewFolderEditing) {
        if (state.editingFolderId != null && state.isNewFolderEditing) {
            val index = state.pastas.indexOfFirst { it.id == state.editingFolderId }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Header Section (Background + Toolbar + Title)
            TrainingHeaderSection(
                onOpenDrawer = onOpenDrawer,
                onNewFolderClick = {
                    viewModel.onAddFolderClick()
                }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Espaço para compensar o Header (Background + Title)
                Spacer(modifier = Modifier.height(260.dp))

                // Lista de Pastas e Sessões
                if (state.isLoading && state.pastas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-40).dp) // Sobreposição com o background convexo
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
                    ) {
                        items(state.pastas, key = { it.id }) { pasta ->
                            val isEditing = state.editingFolderId == pasta.id
                            WorkoutFolderItem(
                                pasta = pasta,
                                isExpanded = state.expandedPastas.contains(pasta.id),
                                sessoes = state.sessoesPorPasta[pasta.id] ?: emptyList(),
                                onToggle = { if (!isEditing) viewModel.togglePasta(pasta.id) },
                                onSessionMenuClick = { viewModel.onSessionMenuClick(it) },
                                onAddSessionClick = { 
                                    navController?.navigate("create_session/${pasta.id}")
                                },
                                onFolderMenuClick = { viewModel.onPastaMenuClick(pasta) },
                                onStartWorkout = { sessionId ->
                                    navController?.navigate("active_workout/$sessionId")
                                },
                                isEditing = isEditing,
                                editNameInput = state.editFolderNameInput,
                                onNameChange = { viewModel.onFolderNameChange(it) },
                                onSaveName = { viewModel.onSaveFolderName() },
                                onCancelEdit = { viewModel.onCancelEditingFolderName() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Overlay Components (Bottom Sheets, Dialogs)
    TrainingOverlays(
        state = state,
        viewModel = viewModel,
        sheetState = sheetState,
        navController = navController
    )
}

@Composable
private fun TrainingHeaderSection(
    onOpenDrawer: () -> Unit,
    onNewFolderClick: () -> Unit
) {
    // --- GRADIENTE ANIMADO ---
    val gradientColors = listOf(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.primary
    )
    val transition = rememberInfiniteTransition(label = "workout_bg_anim")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "workout_bg_translate"
    )
    val animatedBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 1000f, translateAnim + 1000f),
        tileMode = TileMode.Mirror
    )

    // --- FORMA CONVEXA ---
    val density = LocalDensity.current
    val convexShape = remember(density) {
        GenericShape { size, _ ->
            val curveHeight = with(density) { 30.dp.toPx() }
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - curveHeight)
            quadraticBezierTo(
                size.width / 2f, size.height + curveHeight,
                0f, size.height - curveHeight
            )
            close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(convexShape)
            .background(brush = animatedBrush)
    ) {
        Column {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = AppTheme.dimens.extraLargePadding,
                        start = AppTheme.dimens.mediumPadding,
                        end = AppTheme.dimens.mediumPadding
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = onNewFolderClick) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "New Folder",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            val configuration = LocalConfiguration.current

            val screenWidth = configuration.screenWidthDp
            // Com Formula Condensed, 0.18f da largura do ecrã aproxima-se de 90% da width.
            val dynamicFontSize = (screenWidth * 0.28f).sp

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title
                Text(
                    text = "WORKOUT",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.formulacondensedbold)),
                    fontSize = dynamicFontSize,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = AppTheme.dimens.smallPadding)
                )
            }
        }
    }
}

@Composable
fun WorkoutFolderItem(
    pasta: Pasta,
    isExpanded: Boolean,
    sessoes: List<Sessao>,
    onToggle: () -> Unit,
    onSessionMenuClick: (Sessao) -> Unit,
    onAddSessionClick: () -> Unit,
    onFolderMenuClick: () -> Unit,
    onStartWorkout: (Int) -> Unit,
    isEditing: Boolean = false,
    editNameInput: String = "",
    onNameChange: (String) -> Unit = {},
    onSaveName: () -> Unit = {},
    onCancelEdit: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(enabled = !isEditing) { onToggle() },
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp,
        shadowElevation = 5.dp,
        color = colorScheme.surface
    ) {
        Column {
            // Cabeçalho da Pasta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = onNameChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        singleLine = true
                    )
                    IconButton(onClick = onSaveName) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.Green)
                    }
                    IconButton(onClick = onCancelEdit) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red)
                    }
                } else {
                    Text(
                        text = pasta.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Ações da Pasta
                    IconButton(onClick = onAddSessionClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add Session", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onFolderMenuClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Folder Menu", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Sessões (Lista Expansível)
            AnimatedVisibility(visible = isExpanded && !isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (sessoes.isEmpty()) {
                        Text(
                            "No sessions in this folder",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        sessoes.forEach { sessao ->
                            SessionItem(
                                sessao = sessao,
                                onMenuClick = { onSessionMenuClick(sessao) },
                                onStartClick = { onStartWorkout(sessao.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItem(
    sessao: Sessao,
    onMenuClick: () -> Unit,
    onStartClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sessao.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Button(
                onClick = onStartClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start", fontSize = 12.sp)
            }
            
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Session Menu")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingOverlays(
    state: TrainingTabUiState,
    viewModel: TrainingTabViewModel,
    sheetState: androidx.compose.material3.SheetState,
    navController: androidx.navigation.NavController?
) {
    // Bottom Sheet de Opções da Sessão
    if (state.showSessionMenu && state.selectedSessao != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissSessionMenu() },
            sheetState = sheetState
        ) {
            SessionMenuContent(
                onDelete = { viewModel.onDeleteSessionClick() },
                onEdit = { 
                    viewModel.dismissSessionMenu()
                    navController?.navigate("edit_session/${state.selectedSessao!!.id}")
                },
                onDuplicate = { viewModel.onDuplicateSessionClick() }
            )
        }
    }

    // Bottom Sheet de Opções da Pasta
    if (state.showPastaMenu && state.selectedPasta != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissPastaMenu() },
            sheetState = sheetState
        ) {
            FolderMenuContent(
                pasta = state.selectedPasta,
                onEditName = { viewModel.onStartEditingFolderName(state.selectedPasta!!) },
                onDelete = { viewModel.onDeletePastaClick() }
            )
        }
    }

    // Diálogo de Confirmação de Delete (Sessão)
    if (state.showDeleteSessionConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteSessionConfirmation() },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this workout session?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteSession() }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteSessionConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Diálogo de Confirmação de Delete (Pasta)
    if (state.showDeletePastaConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeletePastaConfirmation() },
            title = { Text("Delete Folder") },
            text = { Text("Are you sure you want to delete this folder and all its sessions?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeletePasta() }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeletePastaConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FolderMenuContent(
    pasta: Pasta,
    onEditName: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Folder Options: ${pasta.nome}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        HorizontalDivider()
        
        MenuOption(
            icon = Icons.Default.Edit,
            text = "Edit Name",
            onClick = onEditName
        )

        // Só mostrar a opção de eliminar se a pasta for eliminável
        if (pasta.isDeletable) {
            MenuOption(
                icon = Icons.Default.Delete,
                text = "Delete Folder",
                onClick = onDelete,
                textColor = Color.Red
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SessionMenuContent(
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Session Options",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        HorizontalDivider()
        
        MenuOption(icon = Icons.Default.Edit, text = "Edit Session", onClick = onEdit)
        MenuOption(icon = Icons.Default.ContentCopy, text = "Duplicate Session", onClick = onDuplicate)
        MenuOption(icon = Icons.Default.Delete, text = "Delete Session", onClick = onDelete, textColor = Color.Red)
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MenuOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = if(textColor != Color.Unspecified) textColor else colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, color = textColor)
    }
}
