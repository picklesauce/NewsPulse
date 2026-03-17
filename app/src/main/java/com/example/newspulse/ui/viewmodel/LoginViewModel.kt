package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    /** Returns true if the credentials match a registered account (from sign up). */
    fun logIn(): Boolean {
        val s = _uiState.value
        return when {
            s.email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter your email address") }
                false
            }
            s.password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter your password") }
                false
            }
            !model.validateLogin(s.email.trim(), s.password) -> {
                _uiState.update { it.copy(errorMessage = "Invalid email or password") }
                false
            }
            else -> true
            }
    }

    fun isOnboardingComplete(): Boolean = model.isOnboardingComplete()
}
