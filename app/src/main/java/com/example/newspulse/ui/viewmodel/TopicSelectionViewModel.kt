package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.util.filterMatchingQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TopicSelectionViewModel(private val model: NewsPulseModel) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    val allTopics: List<String> get() = model.getAllInterests().map { it.name }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTopic(topic: String) {
        _selectedTopics.update { current ->
            if (current.contains(topic)) {
                current - topic
            } else {
                current + topic
            }
        }
    }

    fun getFilteredTopics(): List<String> = allTopics.filterMatchingQuery(_searchQuery.value)

    fun saveAndContinue() {
        val ids = model.getAllInterests()
            .filter { it.name in _selectedTopics.value }
            .map { it.id }
            .toSet()
        model.setFollowedInterestIds(ids)
        model.setOnboardingComplete()
    }
}
