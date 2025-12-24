// app/src/main/java/ui/splash/SplashViewModel.kt
package com.example.nutriflex2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import datastore.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object ToWelcome : SplashDestination()
    object ToHome : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val token = tokenManager.authToken // Flow<String?>
                .firstOrNull()

            _destination.value = if (token.isNullOrBlank()) {
                SplashDestination.ToWelcome       // ou login direto
            } else {
                SplashDestination.ToHome       // ou já home, quando tiveres home
            }
        }
    }
}
