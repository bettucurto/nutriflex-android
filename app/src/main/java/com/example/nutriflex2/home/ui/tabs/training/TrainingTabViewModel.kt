package com.example.nutriflex2.home.ui.tabs.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

@HiltViewModel
class TrainingTabViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingTabUiState())
    val uiState: StateFlow<TrainingTabUiState> = _uiState.asStateFlow()

    private var currentUserId: Int = 0

    init {
        observeUserAndPastas()
    }

    private fun observeUserAndPastas() {
        viewModelScope.launch {
            userLocalRepository.getUserLocal()
                .filterNotNull()
                .flatMapLatest { user ->
                    currentUserId = user.userId
                    checkAndCreateDefaultFolder(user.userId)
                    treinoRepository.observePastas(user.userId)
                }
                .onEach { pastas ->
                    _uiState.update { it.copy(pastas = pastas) }
                    refreshSessoesForExpandedPastas(pastas)
                }
                .collect()
        }
    }

    private suspend fun checkAndCreateDefaultFolder(userId: Int) {
        // Esta lógica deve ser idealmente no backend ou no repositório durante o login/sincronização
        // Mas implementamos aqui para garantir o requisito da regra de negócio.
        // A flag is_deletable=false deve ser suportada pela API.
        val pastas = treinoRepository.observePastas(userId).first()
        if (pastas.none { it.nome == "Your Workouts" }) {
            try {
                // Ao criar a pasta default, a API deve saber que is_deletable = false
                // Se a API não suportar no POST, o backend deve ter isto por defeito para este nome.
                treinoRepository.createPasta("Your Workouts", userId, visibilidade = "privada", isDeletable = false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun refreshSessoesForExpandedPastas(pastas: List<Pasta>) {
        pastas.forEach { pasta ->
            if (_uiState.value.expandedPastas.contains(pasta.id)) {
                observeSessoes(pasta.id)
            }
        }
    }

    fun togglePasta(pastaId: Int) {
        val currentlyExpanded = _uiState.value.expandedPastas
        val isExpanding = !currentlyExpanded.contains(pastaId)
        
        _uiState.update { state ->
            val newExpanded = if (isExpanding) {
                currentlyExpanded + pastaId
            } else {
                currentlyExpanded - pastaId
            }
            state.copy(expandedPastas = newExpanded)
        }

        if (isExpanding) {
            observeSessoes(pastaId)
            viewModelScope.launch {
                treinoRepository.refreshSessoes(pastaId)
            }
        }
    }

    private fun observeSessoes(pastaId: Int) {
        viewModelScope.launch {
            treinoRepository.observeSessoes(pastaId).onEach { sessoes ->
                _uiState.update { state ->
                    val newMap = state.sessoesPorPasta.toMutableMap()
                    newMap[pastaId] = sessoes
                    state.copy(sessoesPorPasta = newMap)
                }
            }.collect()
        }
    }

    fun onSessionMenuClick(sessao: Sessao) {
        _uiState.update { it.copy(selectedSessao = sessao, showSessionMenu = true) }
    }

    fun dismissSessionMenu() {
        _uiState.update { it.copy(showSessionMenu = false) }
    }

    fun onDeleteSessionClick() {
        _uiState.update { it.copy(showSessionMenu = false, showDeleteSessionConfirmation = true) }
    }

    fun dismissDeleteSessionConfirmation() {
        _uiState.update { it.copy(showDeleteSessionConfirmation = false) }
    }

    fun confirmDeleteSession() {
        val sessao = _uiState.value.selectedSessao ?: return
        viewModelScope.launch {
            try {
                treinoRepository.deleteSessao(sessao.id, sessao.idPasta)
                _uiState.update { it.copy(showDeleteSessionConfirmation = false, selectedSessao = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error deleting session", showDeleteSessionConfirmation = false) }
            }
        }
    }

    // --- Pasta Menu Logic ---
    fun onAddFolderClick() {
        viewModelScope.launch {
            try {
                // 1. Cria a pasta com nome temporário
                val newId = treinoRepository.createPasta(
                    nome = "Nova Pasta",
                    userId = currentUserId,
                    visibilidade = "privada",
                    isDeletable = true
                )
                
                // 2. Ativa o modo de edição imediatamente
                if (newId != 0) {
                    _uiState.update { state ->
                        state.copy(
                            editingFolderId = newId,
                            editFolderNameInput = "", // Caixa limpa para o utilizador escrever
                            isNewFolderEditing = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error creating folder") }
            }
        }
    }

    fun onPastaMenuClick(pasta: Pasta) {
        _uiState.update { it.copy(selectedPasta = pasta, showPastaMenu = true) }
    }

    fun dismissPastaMenu() {
        _uiState.update { it.copy(showPastaMenu = false) }
    }

    fun onDeletePastaClick() {
        val pasta = _uiState.value.selectedPasta ?: return
        if (!pasta.isDeletable) return // Segurança extra no ViewModel
        
        _uiState.update { it.copy(showPastaMenu = false, showDeletePastaConfirmation = true) }
    }

    fun dismissDeletePastaConfirmation() {
        _uiState.update { it.copy(showDeletePastaConfirmation = false) }
    }

    fun confirmDeletePasta() {
        val pasta = _uiState.value.selectedPasta ?: return
        if (!pasta.isDeletable) return

        viewModelScope.launch {
            try {
                treinoRepository.deletePasta(pasta.id, currentUserId)
                _uiState.update { it.copy(showDeletePastaConfirmation = false, selectedPasta = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error deleting folder", showDeletePastaConfirmation = false) }
            }
        }
    }

    // --- Inline Folder Name Editing ---
    fun onStartEditingFolderName(pasta: Pasta) {
        _uiState.update { it.copy(
            editingFolderId = pasta.id,
            editFolderNameInput = pasta.nome,
            showPastaMenu = false,
            isNewFolderEditing = false // É uma pasta existente
        ) }
    }

    fun onFolderNameChange(newName: String) {
        _uiState.update { it.copy(editFolderNameInput = newName) }
    }

    fun onCancelEditingFolderName() {
        val state = _uiState.value
        val folderIdToDelete = if (state.isNewFolderEditing) state.editingFolderId else null

        _uiState.update { it.copy(
            editingFolderId = null,
            editFolderNameInput = "",
            isNewFolderEditing = false
        ) }

        // Se era uma pasta nova e foi cancelada, apaga da BD
        if (folderIdToDelete != null) {
            viewModelScope.launch {
                try {
                    treinoRepository.deletePasta(folderIdToDelete, currentUserId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun onSaveFolderName() {
        val state = _uiState.value
        val folderId = state.editingFolderId ?: return
        val newName = state.editFolderNameInput

        if (newName.isBlank()) {
            if (state.isNewFolderEditing) {
                // Se é nova e está vazia, cancelamos (o que apaga a pasta)
                onCancelEditingFolderName()
            } else {
                // Se é uma pasta existente e ficou vazia, apenas cancelamos a edição (mantém o nome antigo)
                _uiState.update { it.copy(editingFolderId = null, editFolderNameInput = "") }
            }
            return
        }

        viewModelScope.launch {
            try {
                treinoRepository.updatePasta(folderId, newName, currentUserId)
                _uiState.update { it.copy(
                    editingFolderId = null,
                    editFolderNameInput = "",
                    isNewFolderEditing = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error saving folder name") }
            }
        }
    }

    fun refreshData() {
        if (currentUserId == 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            treinoRepository.refreshPastas(currentUserId)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
