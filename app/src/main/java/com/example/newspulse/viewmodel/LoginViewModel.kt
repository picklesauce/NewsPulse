package com.example.newspulse.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.model.LoginState

class LoginViewModel : ViewModel() {
    val uiState = LoginState()
}
