package com.example.nutriflex2.home.ui.tabs.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchExerciseViewModel @Inject constructor(
    private val treinoRepository: TreinoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchExerciseUiState())
    val uiState: StateFlow<SearchExerciseUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        triggerSearch()
    }

    fun onBodyPartFilterSelected(bodyPartName: String) {
        _uiState.update { it.copy(selectedBodyPart = if (it.selectedBodyPart == bodyPartName) null else bodyPartName) }
        triggerSearch()
    }

    fun onEquipmentFilterSelected(equipmentName: String) {
        _uiState.update { it.copy(selectedEquipment = if (it.selectedEquipment == equipmentName) null else equipmentName) }
        triggerSearch()
    }

    private fun triggerSearch() {
        searchJob?.cancel()
        
        val state = _uiState.value
        // Se tudo estiver vazio, limpamos os resultados (ou podemos carregar recomendados)
        if (state.searchQuery.isBlank() && state.selectedBodyPart == null && state.selectedEquipment == null) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = treinoRepository.searchExercises(
                    name = if (state.searchQuery.isBlank()) null else state.searchQuery,
                    bodyParts = state.selectedBodyPart,
                    equipments = state.selectedEquipment
                )
                if (response.success) {
                    _uiState.update { it.copy(searchResults = response.data, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = "Search failed", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
