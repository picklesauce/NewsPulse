package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TopicSelectionViewModel(private val model: Model) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    val allTopics: List<String> get() = model.getAvailableTopics()

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

    fun getFilteredTopics(): List<String> {
        val query = _searchQuery.value.trim().lowercase()
        return if (query.isEmpty()) {
            allTopics
        } else {
            allTopics.filter { it.lowercase().contains(query) }
        }
    }

    fun saveAndContinue() {
        model.setSelectedInterests(_selectedTopics.value)
        model.setOnboardingComplete()
    }
}
