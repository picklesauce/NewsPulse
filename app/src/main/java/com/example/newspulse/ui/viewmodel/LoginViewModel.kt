package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(private val model: NewsPulseModel) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(username = model.getUsername())
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun saveLogin() {
        model.setUsername(_uiState.value.username)
        model.setMemberSinceIfFirstTime()
    }
}
