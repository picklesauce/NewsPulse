package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Interest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FiltersViewModel(private val model: NewsPulseModel) : ViewModel() {
    val allInterests: List<Interest> get() = model.getAllInterests()

    private val _selectedIds = MutableStateFlow(model.getFollowedInterestIds())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    fun toggleInterest(id: String) {
        _selectedIds.update { current ->
            if (current.contains(id)) {
                current - id
            } else {
                current + id
            }
        }
    }

    fun apply() {
        model.setFollowedInterestIds(_selectedIds.value)
    }

    fun reset() {
        _selectedIds.value = emptySet()
    }
}
