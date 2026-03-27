package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val interestsByType: Map<InterestType, List<Interest>> = emptyMap(),
    val typeFilter: InterestType? = null,
    val selectedInterest: Interest? = null,
    val articlesForSelected: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isFollowed: Boolean = false
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
        val followed = model.getFollowedInterestIds().contains(interest.id)
        _uiState.update {
            it.copy(
                selectedInterest = interest,
                articlesForSelected = emptyList(),
                isLoading = true,
                isFollowed = followed
            )
        }
        viewModelScope.launch {
            val articles = model.searchArticlesByKeyword(interest.name)
            _uiState.update {
                it.copy(articlesForSelected = articles, isLoading = false)
            }
        }
    }

    fun onClearSelection() {
        _uiState.update {
            it.copy(
                selectedInterest = null,
                articlesForSelected = emptyList(),
                isLoading = false,
                isFollowed = false
            )
        }
    }

    fun onFollowTopic() {
        val interest = _uiState.value.selectedInterest ?: return
        if (_uiState.value.isFollowed) return
        model.addCustomInterest(interest.name, interest.type)
        _uiState.update { it.copy(isFollowed = true) }
    }

    fun onSetTypeFilter(type: InterestType?) {
        _uiState.update { it.copy(typeFilter = type) }
    }

    fun visibleInterestsByType(): Map<InterestType, List<Interest>> {
        val filter = _uiState.value.typeFilter ?: return _uiState.value.interestsByType
        return _uiState.value.interestsByType.filterKeys { it == filter }
    }
}
