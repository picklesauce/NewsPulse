package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel

class ProfileViewModel(private val model: NewsPulseModel) : ViewModel() {
    val username: String get() = model.getUsername().ifEmpty { "username123" }
    val memberSince: String get() = model.getMemberSince()
    val interests: Set<String> get() = model.getFollowedInterests().map { it.name }.toSet()
    val readingHistory get() = model.getReadingHistory()
}
