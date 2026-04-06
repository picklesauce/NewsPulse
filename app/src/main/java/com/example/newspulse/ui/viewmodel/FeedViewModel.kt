package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.DiscoverCategories
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// State for the Feed (ArticleList) screen.

data class FeedUiState(
    val articles: List<Article> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Followed interests that are not on discover page
    val followedInterestNames: Set<String> = emptySet(),
    val followedCategoryNames: Set<String> = emptySet(),
    val interestFilterSectionTitle: String = "Interests",
    val categoryFilterSectionTitle: String = "Categories",
    // Filters the feed by interests
    // null means no filter
    val activeInterestFilters: Set<String>? = null,
    val activeCategoryFilters: Set<String>? = null,
    // Shown when article list is empty
    val emptyStateMessage: String? = null,
    // True if feed has barely any articles
    val isCoverageThin: Boolean = false,
    // True when the feed is populated from falback keywords
    val isFallbackFeed: Boolean = false,
    val headerTitle: String = "NewsPulse",
    val searchPlaceholder: String = "Search articles...",
    val imagePlaceholderText: String = "[IMAGE]"
)

class FeedViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        refreshArticles()
        viewModelScope.launch {
            model.refreshNews()
            refreshArticles()
        }
        viewModelScope.launch {
            model.feedRefetchRequests.collectLatest {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    model.forceRefreshNews()
                } finally {
                    refreshArticles()
                    _uiState.update { st -> st.copy(isLoading = false) }
                }
            }
        }
    }

    fun onRefresh() {
        _uiState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        viewModelScope.launch {
            try {
                model.forceRefreshNews()
            } finally {
                refreshArticles()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshArticles()
    }

    fun unfollowInterest(name: String) {
        val id = model.getAllInterests().find { it.name == name }?.id ?: return
        viewModelScope.launch {
            model.unfollowInterestSuspend(id)
            refreshArticles()
        }
    }

    fun onToggleInterestFilter(topic: String) {
        val current = _uiState.value.activeInterestFilters
        val updated = when {
            current == null -> setOf(topic)
            topic in current -> current - topic
            else -> current + topic
        }
        _uiState.update { it.copy(activeInterestFilters = updated) }
        refreshArticles()
    }

    fun onClearInterestFilters() {
        _uiState.update { it.copy(activeInterestFilters = null) }
        refreshArticles()
    }

    fun onToggleCategoryFilter(topic: String) {
        val current = _uiState.value.activeCategoryFilters
        val updated = when {
            current == null -> setOf(topic)
            topic in current -> current - topic
            else -> current + topic
        }
        _uiState.update { it.copy(activeCategoryFilters = updated) }
        refreshArticles()
    }

    fun onClearCategoryFilters() {
        _uiState.update { it.copy(activeCategoryFilters = null) }
        refreshArticles()
    }

    fun onResumeRefresh() {
        viewModelScope.launch {
            model.refreshNews()
            refreshArticles()
        }
    }

    private fun refreshArticles() {
        val allFollowed = model.getFollowedInterestNames()
        val discover = DiscoverCategories.NAMES
        val followedCategoryNames = allFollowed.filter { it in discover }.toSet()
        val followedInterestNames = allFollowed.filter { it !in discover }.toSet()

        val fullFeed = model.getFeed()
        var base = fullFeed.filter { it.matchesInterests(allFollowed) }

        val isFallback = base.isEmpty() && fullFeed.isNotEmpty()
        if (isFallback) base = fullFeed

        val interestFilters = _uiState.value.activeInterestFilters
        val categoryFilters = _uiState.value.activeCategoryFilters
        if (interestFilters != null && interestFilters.isNotEmpty()) {
            base = base.filter { article -> article.topics.any { it in interestFilters } }
        }
        if (categoryFilters != null && categoryFilters.isNotEmpty()) {
            base = base.filter { article -> article.topics.any { it in categoryFilters } }
        }
        val query = _uiState.value.searchQuery
        val articles = if (query.isBlank()) {
            base
        } else {
            base.filter { it.matches(query) }
        }

        val isThin = articles.size in 1..THIN_FEED_THRESHOLD
        val hasActiveFilters =
            (interestFilters != null && interestFilters.isNotEmpty()) ||
                (categoryFilters != null && categoryFilters.isNotEmpty())
        val emptyMessage = when {
            articles.isNotEmpty() -> null
            query.isNotBlank() -> "No articles match your search"
            hasActiveFilters -> "No articles for the selected interests or categories"
            allFollowed.isEmpty() -> "Follow some topics to build your feed"
            else -> "No articles yet — try adding more interests from Discover"
        }
        _uiState.update {
            it.copy(
                articles = articles,
                followedInterestNames = followedInterestNames,
                followedCategoryNames = followedCategoryNames,
                emptyStateMessage = emptyMessage,
                isCoverageThin = isThin,
                isFallbackFeed = isFallback && articles.isNotEmpty()
            )
        }
    }

    companion object {
        private const val THIN_FEED_THRESHOLD = 5
    }
}
