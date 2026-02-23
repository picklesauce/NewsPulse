package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TopicSelectionViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    val allTopics = listOf(
        "USA",
        "Canada",
        "United Kingdom",
        "Germany",
        "Japan",
        "Donald Trump",
        "Elon Musk",
        "Taylor Swift",
        "Tesla",
        "Apple",
        "Google",
        "Microsoft",
        "Technology",
        "Business",
        "Sports",
        "Entertainment",
        "Politics",
        "Science",
        "Health",
        "Climate"
    )

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
}
