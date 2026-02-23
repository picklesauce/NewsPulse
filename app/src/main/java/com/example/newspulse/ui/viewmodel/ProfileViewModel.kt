package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.Model

class ProfileViewModel(private val model: Model) : ViewModel() {
    val username: String get() = model.getUsername().ifEmpty { "username123" }
    val memberSince: String get() = model.getMemberSince()
    val interests: Set<String> get() = model.getSelectedInterests()
    val readingHistory get() = model.getReadingHistory()
}
