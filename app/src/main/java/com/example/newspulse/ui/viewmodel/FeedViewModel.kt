package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedViewModel(private val model: NewsPulseModel) : ViewModel() {
    private val _articles = MutableStateFlow(emptyList<Article>())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshArticles()
    }

    fun onRefresh() {
        _isLoading.value = true
        _errorMessage.value = null
        refreshArticles()
        _isLoading.value = false
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
        refreshArticles()
    }

    fun unfollowInterest(name: String) {
        val id = model.getAllInterests().find { it.name == name }?.id ?: return
        model.unfollowInterest(id)
        refreshArticles()
    }

    val selectedInterests: Set<String> get() = model.getFollowedInterestNames()

    private fun refreshArticles() {
        val interests = model.getFollowedInterestNames()
        val base = model.getFeed().filter { it.matchesInterests(interests) }
        val query = _searchQuery.value
        _articles.value = if (query.isBlank()) {
            base
        } else {
            base.filter { it.matches(query) }
        }
    }
}
