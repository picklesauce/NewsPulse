package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for the Feed (ArticleList) screen.
 * No article data or user-facing copy is hardcoded in the UI; all content comes from this state.
 */
data class FeedUiState(
    val articles: List<Article> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedInterests: Set<String> = emptySet(),
    /** Shown when articles list is empty; null when there are articles. */
    val emptyStateMessage: String? = null,
    val headerTitle: String = "NewsPulse",
    val searchPlaceholder: String = "Search articles...",
    /** Placeholder text when article has no image (e.g. "[IMAGE]"). */
    val imagePlaceholderText: String = "[IMAGE]"
)

class FeedViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        refreshArticles()
    }

    fun onRefresh() {
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        refreshArticles()
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshArticles()
    }

    fun unfollowInterest(name: String) {
        val id = model.getAllInterests().find { it.name == name }?.id ?: return
        model.unfollowInterest(id)
        refreshArticles()
    }

    private fun refreshArticles() {
        val interests = model.getFollowedInterestNames()
        val base = model.getFeed().filter { it.matchesInterests(interests) }
        val query = _uiState.value.searchQuery
        val articles = if (query.isBlank()) {
            base
        } else {
            base.filter { it.matches(query) }
        }
        val emptyMessage = when {
            articles.isNotEmpty() -> null
            query.isNotBlank() -> "No articles match your search"
            else -> "No articles match your interests"
        }
        _uiState.update {
            it.copy(
                articles = articles,
                selectedInterests = interests,
                emptyStateMessage = emptyMessage
            )
        }
    }
}
