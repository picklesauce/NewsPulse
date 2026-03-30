package com.example.newspulse.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import kotlinx.coroutines.launch

class ProfileViewModel(private val model: NewsPulseModel) : ViewModel() {

    fun sessionIdentity(): String? = model.getCurrentUserId()

    var username by mutableStateOf(model.getUsername())
        private set
    var memberSince by mutableStateOf(model.getMemberSince())
        private set

    val interests: Set<String> get() = model.getFollowedInterests().map { it.name }.toSet()

    val groupedInterests: List<Pair<InterestType, List<Interest>>>
        get() = model.getFollowedInterests()
            .groupBy { it.type }
            .toSortedMap(compareBy { TYPE_ORDER.indexOf(it) })
            .map { (type, list) -> type to list }

    val readingHistory get() = model.getReadingHistory()

    fun refreshFromModel() {
        username = model.getUsername()
        memberSince = model.getMemberSince()
    }

    fun reloadProfileFromRemote() {
        viewModelScope.launch {
            model.refreshProfileDisplayFromRemote()
            refreshFromModel()
        }
    }

    fun updateUsername(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed == username) return
        model.setUsername(trimmed)
        username = trimmed
    }

    fun signOut() {
        model.signOut()
        refreshFromModel()
    }

    private companion object {
        val TYPE_ORDER = listOf(
            InterestType.Topic,
            InterestType.Country,
            InterestType.Person,
            InterestType.Company
        )
    }
}
