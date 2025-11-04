package com.day.mate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.authUiState.AuthUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            delay(1000)
            _state.value = if (email == "test@mail.com" && password == "123456") {
                AuthUiState.Success
            } else {
                AuthUiState.Error("Invalid email or password")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthUiState.Loading
            delay(1000)
            _state.value = if (email.isNotBlank() && password.isNotBlank()) {
                AuthUiState.Success
            } else {
                AuthUiState.Error("Please fill all fields")
            }
        }
    }
}
