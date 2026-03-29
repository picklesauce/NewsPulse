package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isGoogleLoading: Boolean = false,
    /** Set true once after Google OAuth completes and session is synced; UI consumes and navigates. */
    val oauthNavigateHome: Boolean = false
)

class LoginViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var awaitingGoogleCompletion = false

    init {
        viewModelScope.launch {
            model.observeSupabaseAuthUserId().collect { uid ->
                if (uid != null && awaitingGoogleCompletion) {
                    awaitingGoogleCompletion = false
                    model.syncSupabaseAuthSessionToApp()
                    _uiState.update {
                        it.copy(
                            isGoogleLoading = false,
                            errorMessage = null,
                            oauthNavigateHome = true
                        )
                    }
                }
            }
        }
    }

    fun consumeOAuthNavigation() {
        _uiState.update { it.copy(oauthNavigateHome = false) }
    }

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    /** Runs sign-in off the UI thread; invokes [onResult] on the main thread. */
    fun logIn(onResult: (Boolean) -> Unit) {
        val s = _uiState.value
        when {
            s.email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter your email or username") }
                onResult(false)
            }
            s.password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter your password") }
                onResult(false)
            }
            else -> {
                viewModelScope.launch {
                    val result = model.logIn(s.email.trim(), s.password)
                    if (!result.success) {
                        _uiState.update { it.copy(errorMessage = result.errorMessage ?: "Invalid email or password") }
                        onResult(false)
                    } else {
                        onResult(true)
                    }
                }
            }
        }
    }

    fun continueWithGoogle() {
        awaitingGoogleCompletion = true
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleLoading = true, errorMessage = null) }
            val result = model.signInWithGoogle()
            if (!result.success) {
                awaitingGoogleCompletion = false
                _uiState.update {
                    it.copy(isGoogleLoading = false, errorMessage = result.errorMessage ?: "Google sign-in failed")
                }
            }
        }
    }

    fun isOnboardingComplete(): Boolean = model.isOnboardingComplete()
}
