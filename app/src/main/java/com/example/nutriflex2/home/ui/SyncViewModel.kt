package com.example.nutriflex2.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dieta.domain.DietaRepository
import com.example.treino.domain.repository.TreinoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import local.UserLocalRepository
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val dietaRepository: DietaRepository,
    private val treinoRepository: TreinoRepository,
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private var hasSyncedInThisSession = false

    fun performInitialSync() {
        if (hasSyncedInThisSession) return
        
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val user = userLocalRepository.getUserLocal().firstOrNull()
                user?.let {
                    // Sincroniza tudo do remoto apenas uma vez (no login/entrada na home)
                    launch {
                        dietaRepository.refreshMealsFromRemote(it.userId)
                    }
                    launch {
                        dietaRepository.refreshFavoriteRecipesFromRemote(it.userId)
                    }
                    launch {
                        treinoRepository.refreshPastas(it.userId)
                    }
                    hasSyncedInThisSession = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
