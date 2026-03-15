package com.example.nutriflex2.home.ui.tabs.training

import com.example.treino.domain.models.Pasta
import com.example.treino.domain.models.Sessao

data class TrainingTabUiState(
    val pastas: List<Pasta> = emptyList(),
    val sessoesPorPasta: Map<Int, List<Sessao>> = emptyMap(),
    val expandedPastas: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Bottom Sheet / Dialog State
    val selectedSessao: Sessao? = null,
    val selectedPasta: Pasta? = null,
    val showSessionMenu: Boolean = false,
    val showPastaMenu: Boolean = false,
    val showDeleteSessionConfirmation: Boolean = false,
    val showDeletePastaConfirmation: Boolean = false,

    // Inline Editing State
    val editingFolderId: Int? = null,
    val editFolderNameInput: String = "",
    val isNewFolderEditing: Boolean = false
)
