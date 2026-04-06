package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.filterByType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State for the interests screen
data class InterestsUiState(
    val interestsToShow: List<Pair<InterestType, List<Interest>>> = emptyList(),
    val followedIds: Set<String> = emptySet(),
    val typeFilter: InterestType? = null,
    val searchQuery: String = "",
    val canAddCustom: Boolean = false,
    val headerTitle: String = "Interests",
    val subtitle: String = "Follow or unfollow to personalize your feed, or add your own.",
    val filterAllLabel: String = "All",
    val showingFilterLabel: String = "Showing: %s"
)

// Manages interest list and follow state
class InterestsViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _followedIds = MutableStateFlow(model.getFollowedInterestIds())
    private val _typeFilter = MutableStateFlow<InterestType?>(null)
    private val _searchQuery = MutableStateFlow("")

    private val allInterests: List<Interest> get() = model.getAllInterests()

    private val interestsGroupedByType: List<Pair<InterestType, List<Interest>>> get() =
        INTEREST_TYPE_ORDER.map { type ->
            type to allInterests.filterByType(type)
        }.filter { it.second.isNotEmpty() }

    private fun computeInterestsToShow(): List<Pair<InterestType, List<Interest>>> {
        val query = _searchQuery.value.trim().lowercase()
        val grouped = _typeFilter.value?.let { type ->
            val list = allInterests.filterByType(type)
            if (list.isEmpty()) emptyList() else listOf(type to list)
        } ?: interestsGroupedByType

        if (query.isEmpty()) return grouped
        return grouped.map { (type, interests) ->
            type to interests.filter { it.name.lowercase().contains(query) }
        }.filter { it.second.isNotEmpty() }
    }

    private fun buildUiState(): InterestsUiState {
        val query = _searchQuery.value.trim()
        val canAdd = query.length >= 2 &&
            allInterests.none { it.name.equals(query, ignoreCase = true) }
        return InterestsUiState(
            interestsToShow = computeInterestsToShow(),
            followedIds = _followedIds.value,
            typeFilter = _typeFilter.value,
            searchQuery = query,
            canAddCustom = canAdd
        )
    }

    private val _uiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<InterestsUiState> = _uiState.asStateFlow()

    private fun refreshUiState() {
        _uiState.value = buildUiState()
    }

    init {
        refreshUiState()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        refreshUiState()
    }

    fun setTypeFilter(type: InterestType?) {
        _typeFilter.value = type
        refreshUiState()
    }

    fun onFollowToggle(id: String) {
        viewModelScope.launch {
            if (_followedIds.value.contains(id)) {
                _followedIds.update { it - id }
                refreshUiState()
                if (!model.unfollowInterestSuspend(id)) {
                    // DB failed — keep local state removed (user intent) but log is fine.
                }
            } else {
                _followedIds.update { it + id }
                refreshUiState()
                model.followInterestSuspend(id)
            }
        }
    }

    fun addCustomInterest(type: InterestType) {
        val name = _searchQuery.value.trim()
        if (name.length < 2) return
        viewModelScope.launch {
            if (model.addCustomInterestPersisted(name, type)) {
                val interest = model.getAllInterests().find { it.name.equals(name, ignoreCase = true) }
                if (interest != null) {
                    _followedIds.update { it + interest.id }
                }
                _searchQuery.value = ""
                refreshUiState()
            }
        }
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
