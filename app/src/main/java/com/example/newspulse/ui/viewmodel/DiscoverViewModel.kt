package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DiscoverUiState(
    /** All interests from the catalog, grouped by type. */
    val interestsByType: Map<InterestType, List<Interest>> = emptyMap(),
    /** Active type filter in the browse view; null = show all types. */
    val typeFilter: InterestType? = null,
    /** Interest the user tapped — triggers article list view. Null = browse view. */
    val selectedInterest: Interest? = null,
    /** Articles for the selectedInterest. Empty when no interest is selected. */
    val articlesForSelected: List<Article> = emptyList()
)

class DiscoverViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        val grouped = model.getAllInterests()
            .groupBy { it.type }
            .toSortedMap(compareBy { it.name })
        _uiState.update { it.copy(interestsByType = grouped) }
    }

    fun onSelectInterest(interest: Interest) {
        val articles = model.getFeed().filter { article ->
            article.interests.any { it.id == interest.id }
        }
        _uiState.update {
            it.copy(selectedInterest = interest, articlesForSelected = articles)
        }
    }

    fun onClearSelection() {
        _uiState.update { it.copy(selectedInterest = null, articlesForSelected = emptyList()) }
    }

    fun onSetTypeFilter(type: InterestType?) {
        _uiState.update { it.copy(typeFilter = type) }
    }

    /** Returns the visible interests based on the current typeFilter. */
    fun visibleInterestsByType(): Map<InterestType, List<Interest>> {
        val filter = _uiState.value.typeFilter ?: return _uiState.value.interestsByType
        return _uiState.value.interestsByType.filterKeys { it == filter }
    }
}
