package com.example.nutriflex2.home.ui.tabs.diet

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ScanMealViewModel @Inject constructor(
    application: Application,
    private val dietaRepository: DietaRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ScanMealUiState())
    val uiState: StateFlow<ScanMealUiState> = _uiState.asStateFlow()

    init {
        loadGalleryImages()
    }

    private fun loadGalleryImages() {
        viewModelScope.launch {
            val (recent, albums) = withContext(Dispatchers.IO) {
                fetchGalleryImages(getApplication<Application>().contentResolver)
            }
            _uiState.update { 
                it.copy(
                    recentImages = recent,
                    albums = albums,
                    // Garante que "All" tenha todas as imagens se necessário, 
                    // ou mantemos separado. Aqui vamos assumir que 'albums' tem tudo.
                )
            }
        }
    }

    private fun fetchGalleryImages(contentResolver: ContentResolver): Pair<List<Uri>, Map<String, List<Uri>>> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        val recentImages = mutableListOf<Uri>()
        val albums = mutableMapOf<String, MutableList<Uri>>()
        // Adiciona um álbum "All" manualmente
        albums["All"] = mutableListOf()

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val bucketName = it.getString(bucketColumn) ?: "Camera"
                val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

                // Preencher recentes (top 20)
                if (recentImages.size < 20) {
                    recentImages.add(contentUri)
                }

                // Preencher álbuns
                if (!albums.containsKey(bucketName)) {
                    albums[bucketName] = mutableListOf()
                }
                albums[bucketName]?.add(contentUri)
                albums["All"]?.add(contentUri)
            }
        }

        return Pair(recentImages, albums)
    }

    fun onAlbumSelected(albumName: String) {
        _uiState.update { it.copy(selectedAlbum = albumName) }
    }

    fun onImageCaptured(uri: Uri, context: Context) {
        _uiState.update { it.copy(isLoading = true, capturedImageUri = uri) }
        uploadImage(uri, context)
    }
    
    fun onGalleryImageSelected(uri: Uri, context: Context) {
        _uiState.update { it.copy(isLoading = true, capturedImageUri = uri) }
        uploadImage(uri, context)
    }

    private fun uploadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val file = uriToFile(uri, context)
                if (file != null) {
                    val resultJson = dietaRepository.recognizeMeal(file)
                    Log.d("ScanMealViewModel", "API Response: $resultJson")
                    
                    if (resultJson != null) {
                         _uiState.update { it.copy(isLoading = false, successMessage = "Comida Detetada com Sucesso!") }
                    } else {
                         _uiState.update { it.copy(isLoading = false, error = "Falha na deteção.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Erro ao processar imagem.") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    private suspend fun uriToFile(uri: Uri, context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("scan_meal", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
