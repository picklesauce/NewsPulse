package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val errorMessage: String? = null
)

class SignUpViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateUsername(value: String) = _uiState.update { it.copy(username = value, errorMessage = null) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleConfirmPasswordVisible() = _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }

    // Basic sign up validation
    fun signUp(onResult: (Boolean) -> Unit) {
        val s = _uiState.value
        when {
            s.username.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter a username") }
                onResult(false)
            }
            s.email.isBlank() || !s.email.contains("@") -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
                onResult(false)
            }
            s.password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
                onResult(false)
            }
            s.password != s.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match") }
                onResult(false)
            }
            else -> {
                viewModelScope.launch {
                    val result = model.signUp(s.email.trim(), s.password)
                    if (!result.success) {
                        _uiState.update { it.copy(errorMessage = result.errorMessage ?: "Sign up failed") }
                        onResult(false)
                    } else {
                        model.setUsername(s.username)
                        model.setMemberSinceIfFirstTime()
                        onResult(true)
                    }
                }
            }
        }
    }
}
