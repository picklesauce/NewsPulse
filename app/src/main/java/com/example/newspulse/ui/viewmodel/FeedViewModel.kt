package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.DiscoverCategories
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for the Feed (ArticleList) screen.
 * No article data or user-facing copy is hardcoded in the UI; all content comes from this state.
 */
data class FeedUiState(
    val articles: List<Article> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Followed topics that are not Discover grid categories (custom / catalog interests). */
    val followedInterestNames: Set<String> = emptySet(),
    /** Followed topics that match [DiscoverCategories.NAMES] (Discover page categories). */
    val followedCategoryNames: Set<String> = emptySet(),
    val interestFilterSectionTitle: String = "Interests",
    val categoryFilterSectionTitle: String = "Categories",
    /**
     * Narrow feed to these followed interest names. `null` = all followed interests included (no narrowing).
     * Non-empty = only articles tagged with at least one of these names.
     */
    val activeInterestFilters: Set<String>? = null,
    /**
     * Narrow feed to these followed category names. `null` = all followed categories included.
     */
    val activeCategoryFilters: Set<String>? = null,
    /** Shown when articles list is empty; null when there are articles. */
    val emptyStateMessage: String? = null,
    /** True when the feed has very few articles; UI can show a hint. */
    val isCoverageThin: Boolean = false,
    /** True when the feed is populated from fallback keywords rather than user interests. */
    val isFallbackFeed: Boolean = false,
    val headerTitle: String = "NewsPulse",
    val searchPlaceholder: String = "Search articles...",
    val imagePlaceholderText: String = "[IMAGE]"
)

class FeedViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        // Load from cache first, then fetch from API if available (no-op for mock)
        refreshArticles()
        viewModelScope.launch {
            model.refreshNews()
            refreshArticles()
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
        model.unfollowInterest(id)
        refreshArticles()
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

    /** Re-apply filters when returning to Home so the feed reflects follows from Discover. */
    fun onResumeRefresh() {
        refreshArticles()
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
