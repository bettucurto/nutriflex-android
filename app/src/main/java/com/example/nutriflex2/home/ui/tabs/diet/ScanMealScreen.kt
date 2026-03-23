package com.example.nutriflex2.home.ui.tabs.diet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class WhatsAppSheetValue { HandleOnly, Recents, Full }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScanMealScreen(
    viewModel: ScanMealViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // --- ESTADO DO SCAFFOLD (WHATSAPP STYLE) ---
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val isExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
    val clipBottomOffset by animateDpAsState(targetValue = if (isExpanded) 0.dp else 170.dp) // 170.dp = Distância do fundo até ao topo do botão
    val cameraButtonsAlpha by animateFloatAsState(targetValue = if (isExpanded) 0f else 1f)

    val peekHeight = 250.dp // Estado Fechado (apenas pega visível)
    val halfExpandedHeight = 330.dp // Referência para Meio-Termo
    var targetPeekHeight by remember { mutableStateOf(halfExpandedHeight) }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            targetPeekHeight = halfExpandedHeight
        }
    }

    val animatedPeekHeight by animateDpAsState(
        targetValue = targetPeekHeight,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "peekHeightAnimation"
    )
    val buttonAreaHeight = 130.dp

    // Cálculo do progresso entre PartiallyExpanded (0f) e Expanded (1f)
    val progress by remember {
        derivedStateOf {
            val offset = try { scaffoldState.bottomSheetState.requireOffset() } catch (e: Exception) { screenHeightPx }
            val expandedOffset = 0f
            val peekHeightPx = with(density) { peekHeight.toPx() }
            val partiallyExpandedOffset = screenHeightPx - peekHeightPx
            ((partiallyExpandedOffset - offset) / (partiallyExpandedOffset - expandedOffset)).coerceIn(0f, 1f)
        }
    }

    val sheetOffset by remember {
        derivedStateOf {
            try { scaffoldState.bottomSheetState.requireOffset() } catch (e: Exception) { screenHeightPx }
        }
    }

    // CameraX State
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    val imageCapture = remember { ImageCapture.Builder().setFlashMode(flashMode).build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Permissions State
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES))
    }

    if (!hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required", color = Color.White)
        }
        return
    }

    // --- FASE 1: RESTRUTURAÇÃO DE CAMADAS (O TRUQUE DO BOX SOBREPOSTO) ---
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = animatedPeekHeight,
            sheetContainerColor = Color.Transparent,
            sheetContentColor = Color.White,
            sheetShadowElevation = 0.dp,
            sheetTonalElevation = 0.dp,
            sheetDragHandle = {}, // DragHandle agora é gerido internamente no content para não ser cortado
            sheetContent = {
                val isExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
                GallerySheetContent(
                    state = state,
                    onAlbumSelected = viewModel::onAlbumSelected,
                    onImageSelected = { uri -> viewModel.onGalleryImageSelected(uri, context) },
                    progress = progress,
                    sheetOffset = sheetOffset,
                    screenHeightPx = screenHeightPx,
                    buttonAreaHeightPx = with(density) { buttonAreaHeight.toPx() },
                    handleHeightPx = with(density) { 32.dp.toPx() },
                    onPeekHeightChange = { newHeight -> targetPeekHeight = newHeight },
                    isExpanded = isExpanded,
                    scaffoldState = scaffoldState,
                    clipBottomOffset = clipBottomOffset
                )
            }
        ) { paddingValues ->
            // --- CONTEÚDO BASE (CÂMARA) ---
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                            } catch (exc: Exception) { Log.e("CameraX", "Binding failed", exc) }
                        }, ContextCompat.getMainExecutor(context))
                    }
                )

                // Overlay Frame
                Box(modifier = Modifier.size(280.dp).align(Alignment.Center).border(2.dp, Color.Green.copy(alpha = 0.5f), RoundedCornerShape(12.dp)))

                // TopBar
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    IconButton(
                        onClick = { flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff, "Flash",
                            tint = if (flashMode == ImageCapture.FLASH_MODE_ON) Color.Yellow else Color.White)
                    }
                }
            }
        }

        // --- FASE 1.4: CONTROLOS FLUTUANTES (Z-INDEX MAIS ALTO) ---
        // Transparente com gradiente subtil para clipping real
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                        startY = 0f
                    )
                )
                .navigationBarsPadding()
                .height(buttonAreaHeight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 40.dp)
                    .alpha(cameraButtonsAlpha),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Galeria */ }) { Icon(Icons.Default.PhotoLibrary, "Gallery", tint = Color.White, modifier = Modifier.size(28.dp)) }

                Box(modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        takePhoto(context, imageCapture, cameraExecutor, onImageCaptured = { uri -> viewModel.onImageCaptured(uri, context) }, onError = { })
                    }
                )

                IconButton(onClick = { lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK }) {
                    Icon(Icons.Default.Cameraswitch, "Flip", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Green)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Detecting Food...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySheetContent(
    state: ScanMealUiState,
    onAlbumSelected: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    progress: Float,
    sheetOffset: Float,
    screenHeightPx: Float,
    buttonAreaHeightPx: Float,
    handleHeightPx: Float,
    onPeekHeightChange: (androidx.compose.ui.unit.Dp) -> Unit,
    isExpanded: Boolean,
    scaffoldState: androidx.compose.material3.BottomSheetScaffoldState,
    clipBottomOffset: androidx.compose.ui.unit.Dp
) {
    val backgroundColor = if (isExpanded) Color.Black.copy(alpha = (progress * 0.95f).coerceIn(0f, 0.95f)) else Color.Transparent

    Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
        // 1. DragHandle (Sempre visível no topo da gaveta)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount > 10) {
                            onPeekHeightChange(250.dp)
                        } else if (dragAmount < -10) {
                            onPeekHeightChange(330.dp)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }

        // 2. Container de Imagens com Clipping Transparente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .drawWithContent {
                    // try-catch garante que não há crash no 1º frame antes de o Compose calcular os tamanhos
                    val offset = try { scaffoldState.bottomSheetState.requireOffset() } catch (e: Exception) { 0f }
                    
                    // Fórmula mágica: AlturaTotal - DeslocamentoGaveta - AlturaDoBotão
                    // Isto ancora a linha de corte exatamente no ecrã do telemóvel, imune à altura da gaveta!
                    val clipTop = size.height - offset - clipBottomOffset.toPx()
                    
                    clipRect(bottom = clipTop) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            // Imagens Recentes
            AnimatedVisibility(visible = !isExpanded) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(85.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.recentImages) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            null,
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageSelected(uri) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Header do Álbum
            if (progress > 0.1f) {
                var showAlbumMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).alpha(progress), contentAlignment = Alignment.Center) {
                    Row(modifier = Modifier.clickable { showAlbumMenu = true }, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = state.selectedAlbum, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                    }
                    DropdownMenu(expanded = showAlbumMenu, onDismissRequest = { showAlbumMenu = false }, modifier = Modifier.background(Color.DarkGray)) {
                        state.albums.keys.forEach { album ->
                            DropdownMenuItem(text = { Text(album, color = Color.White) }, onClick = { onAlbumSelected(album); showAlbumMenu = false })
                        }
                    }
                }
            }

            // Grelha de Galeria
            if (progress > 0.01f) {
                val images = state.albums[state.selectedAlbum] ?: emptyList()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).alpha(progress),
                    contentPadding = PaddingValues(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(images) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            null,
                            modifier = Modifier.aspectRatio(1f).clickable(enabled = progress > 0.8f) { onImageSelected(uri) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

private fun takePhoto(context: Context, imageCapture: ImageCapture, executor: ExecutorService, onImageCaptured: (Uri) -> Unit, onError: (ImageCaptureException) -> Unit) {
    val photoFile = File(context.externalCacheDir, "scan_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exc: ImageCaptureException) { onError(exc) }
        override fun onImageSaved(output: ImageCapture.OutputFileResults) { onImageCaptured(Uri.fromFile(photoFile)) }
    })
}
