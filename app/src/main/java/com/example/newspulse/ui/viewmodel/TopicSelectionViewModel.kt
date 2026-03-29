package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.filterMatchingQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            if (current.contains(topic)) current - topic else current + topic
        }
    }

    fun getFilteredTopics(): List<String> = allTopics.filterMatchingQuery(_searchQuery.value)

    /**
     * Returns true if the current search query doesn't match any existing interest,
     * meaning the user can add it as a custom interest.
     */
    fun canAddCustom(): Boolean {
        val q = _searchQuery.value.trim()
        if (q.length < 2) return false
        return allTopics.none { it.equals(q, ignoreCase = true) }
    }

    fun addCustomInterest(type: InterestType) {
        val name = _searchQuery.value.trim()
        if (name.length < 2) return
        viewModelScope.launch {
            if (model.addCustomInterestPersisted(name, type)) {
                _selectedTopics.update { it + name }
                _searchQuery.value = ""
            }
        }
    }

    suspend fun saveAndContinueNow(): Boolean {
        val ids = model.getAllInterests()
            .filter { it.name in _selectedTopics.value }
            .map { it.id }
            .toSet()
        return model.setFollowedInterestIdsSuspend(ids) && model.setOnboardingCompleteSuspend()
    }

    fun saveAndContinue(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (saveAndContinueNow()) onSuccess()
        }
    }
}
