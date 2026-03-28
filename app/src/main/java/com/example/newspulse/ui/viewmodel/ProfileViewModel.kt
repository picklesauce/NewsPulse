package com.example.newspulse.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel

class ProfileViewModel(private val model: NewsPulseModel) : ViewModel() {

    /** Use as [LaunchedEffect] key so Profile refreshes when auth identity changes. */
    fun sessionIdentity(): String? = model.getCurrentUserId()

    /** Backed by state so Profile recomposes after prefs / remote profile updates. */
    var username by mutableStateOf(model.getUsername().ifEmpty { "username123" })
        private set
    var memberSince by mutableStateOf(model.getMemberSince())
        private set

    val interests: Set<String> get() = model.getFollowedInterests().map { it.name }.toSet()
    val readingHistory get() = model.getReadingHistory()

    fun refreshFromModel() {
        username = model.getUsername().ifEmpty { "username123" }
        memberSince = model.getMemberSince()
    }

    fun signOut() {
        model.signOut()
        refreshFromModel()
    }
}
