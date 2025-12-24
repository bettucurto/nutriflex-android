package com.example.nutriflex2.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import local.UserLocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HomeUiState(
    val dailyCalories: Int = 0,
    val eatenCaloriesToday: Int = 0
) {
    val remainingCalories: Int get() = (dailyCalories - eatenCaloriesToday).coerceAtLeast(0)
    val progress: Float
        get() = if (dailyCalories <= 0) 0f
        else (eatenCaloriesToday.toFloat() / dailyCalories.toFloat()).coerceIn(0f, 1f)
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userLocalRepository: UserLocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userLocalRepository.getUserLocal()
            if (user != null) {
                _uiState.value = HomeUiState(
                    dailyCalories = user.dailyCalories,
                    eatenCaloriesToday = user.eatenCaloriesToday
                )
            }
        }
    }
}
