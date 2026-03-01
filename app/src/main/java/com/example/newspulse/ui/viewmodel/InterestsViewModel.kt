package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.filterByType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single source of truth for the Interests screen.
 * Interests list comes from ViewModel; follow toggle triggers ViewModel event ([onFollowToggle]).
 */
data class InterestsUiState(
    /** Interests to show, grouped by type. Comes from ViewModel only. */
    val interestsToShow: List<Pair<InterestType, List<Interest>>> = emptyList(),
    val followedIds: Set<String> = emptySet(),
    val typeFilter: InterestType? = null,
    val headerTitle: String = "Interests",
    val subtitle: String = "Follow or unfollow to personalize your feed. Changes apply immediately.",
    val filterAllLabel: String = "All",
    val showingFilterLabel: String = "Showing: %s"
)

/**
 * Manages interest lists and follow state.
 * - Interests list comes from [uiState.interestsToShow].
 * - Follow toggle triggers [onFollowToggle] (ViewModel event).
 */
class InterestsViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _followedIds = MutableStateFlow(model.getFollowedInterestIds())
    private val _typeFilter = MutableStateFlow<InterestType?>(null)

    private val allInterests: List<Interest> get() = model.getAllInterests()

    private val interestsGroupedByType: List<Pair<InterestType, List<Interest>>> get() =
        INTEREST_TYPE_ORDER.map { type ->
            type to allInterests.filterByType(type)
        }.filter { it.second.isNotEmpty() }

    private fun computeInterestsToShow(): List<Pair<InterestType, List<Interest>>> =
        _typeFilter.value?.let { type ->
            val list = allInterests.filterByType(type)
            if (list.isEmpty()) emptyList() else listOf(type to list)
        } ?: interestsGroupedByType

    private fun buildUiState(): InterestsUiState = InterestsUiState(
        interestsToShow = computeInterestsToShow(),
        followedIds = _followedIds.value,
        typeFilter = _typeFilter.value
    )

    private val _uiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<InterestsUiState> = _uiState.asStateFlow()

    private fun refreshUiState() {
        _uiState.value = buildUiState()
    }

    init {
        refreshUiState()
    }

    fun setTypeFilter(type: InterestType?) {
        _typeFilter.value = type
        refreshUiState()
    }

    /** ViewModel event: follow toggle. Call from the UI when user taps an interest chip. */
    fun onFollowToggle(id: String) {
        if (_followedIds.value.contains(id)) {
            model.unfollowInterest(id)
            _followedIds.update { it - id }
        } else {
            model.followInterest(id)
            _followedIds.update { it + id }
        }
        refreshUiState()
    }

    private companion object {
        private val INTEREST_TYPE_ORDER = listOf(
            InterestType.Country,
            InterestType.Person,
            InterestType.Company,
            InterestType.Topic
        )
    }
}
