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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DiscoverUiState(
    val interestsByType: Map<InterestType, List<Interest>> = emptyMap(),
    val typeFilter: InterestType? = null,
    val selectedInterest: Interest? = null,
    val articlesForSelected: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isFollowed: Boolean = false,
    /** True while follow/unfollow is in flight. */
    val isFollowBusy: Boolean = false,
    val followError: String? = null
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

// Opens a Discover category using the same interest ids as Supabase
    fun onSelectCategory(name: String, type: InterestType) {
        viewModelScope.launch {
            val interest = model.interestForDiscoverCategory(name, type)
            val followed = model.getFollowedInterestIds().contains(interest.id)
            _uiState.update {
                it.copy(
                    selectedInterest = interest,
                    articlesForSelected = emptyList(),
                    isLoading = true,
                    isFollowed = followed,
                    isFollowBusy = false,
                    followError = null
                )
            }
            val articles = withContext(Dispatchers.IO) {
                model.searchArticlesByKeyword(interest.name)
            }
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
                isFollowed = false,
                isFollowBusy = false,
                followError = null
            )
        }
    }

    fun onFollowTopic() {
        val interest = _uiState.value.selectedInterest ?: return
        if (_uiState.value.isFollowed || _uiState.value.isFollowBusy) return
        _uiState.update { it.copy(isFollowBusy = true, followError = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ok = model.followDiscoverInterest(interest)
                withContext(Dispatchers.Main.immediate) {
                    if (ok) {
                        _uiState.update { it.copy(isFollowed = true, followError = null) }
                    } else {
                        val msg =
                            if (model.shouldShowSignInForFollowFailure()) {
                                "Sign in to save topics to your account."
                            } else {
                                "Couldn’t save follow. Check connection and try again."
                            }
                        _uiState.update { it.copy(followError = msg) }
                    }
                }
            } finally {
                withContext(Dispatchers.Main.immediate) {
                    _uiState.update { it.copy(isFollowBusy = false) }
                }
            }
        }
    }

    fun onUnfollowTopic() {
        val interest = _uiState.value.selectedInterest ?: return
        if (!_uiState.value.isFollowed || _uiState.value.isFollowBusy) return
        _uiState.update { it.copy(isFollowBusy = true, followError = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ok = model.unfollowDiscoverInterest(interest)
                withContext(Dispatchers.Main.immediate) {
                    if (ok) {
                        _uiState.update { it.copy(isFollowed = false, followError = null) }
                    } else {
                        val msg =
                            if (model.shouldShowSignInForFollowFailure()) {
                                "Sign in to update topics on your account."
                            } else {
                                "Couldn’t unfollow. Check connection and try again."
                            }
                        _uiState.update { it.copy(followError = msg) }
                    }
                }
            } finally {
                withContext(Dispatchers.Main.immediate) {
                    _uiState.update { it.copy(isFollowBusy = false) }
                }
            }
        }
    }

    fun onSetTypeFilter(type: InterestType?) {
        _uiState.update { it.copy(typeFilter = type) }
    }

    fun visibleInterestsByType(): Map<InterestType, List<Interest>> {
        val filter = _uiState.value.typeFilter ?: return _uiState.value.interestsByType
        return _uiState.value.interestsByType.filterKeys { it == filter }
    }
}
