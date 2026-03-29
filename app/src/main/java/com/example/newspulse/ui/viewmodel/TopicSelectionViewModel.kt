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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TopicSelectionViewModel(private val model: NewsPulseModel) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTopics = MutableStateFlow<Set<String>>(emptySet())
    val selectedTopics: StateFlow<Set<String>> = _selectedTopics.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

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

    /**
     * Persists topic picks for Supabase without bulk delete/replace (OAuth JWT users often lack
     * DELETE on followed_interests). Follows each interest individually, then marks onboarding done.
     */
    suspend fun saveAndContinueNow(): Boolean = withContext(Dispatchers.IO) {
        val selected = _selectedTopics.value
        if (selected.isEmpty()) return@withContext false
        val catalog = model.getAllInterests()
        val interests = selected.mapNotNull { name ->
            catalog.find { it.name.equals(name, ignoreCase = true) }
        }
        if (interests.size != selected.size) return@withContext false
        for (interest in interests) {
            if (!model.followInterestSuspend(interest.id)) return@withContext false
        }
        model.setOnboardingCompleteSuspend()
    }

    fun saveAndContinue(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _saveError.value = null
            _isSaving.value = true
            try {
                if (saveAndContinueNow()) {
                    onSuccess()
                } else {
                    _saveError.value =
                        "Could not save your topics. Check your connection and try again."
                }
            } finally {
                _isSaving.value = false
            }
        }
    }
}
