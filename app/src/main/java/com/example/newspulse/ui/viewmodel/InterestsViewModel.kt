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
 * Manages interest lists and follow state.
 * - Shows interests grouped by [InterestType] (Country, Person, Company, Topic).
 * - Follow/unfollow updates the model immediately and reflected in [followedIds].
 * - Followed interests are used by the feed (FeedViewModel filters by them).
 */
class InterestsViewModel(private val model: NewsPulseModel) : ViewModel() {

    /** All interests from the catalog. */
    val allInterests: List<Interest> get() = model.getAllInterests()

    /** Interests grouped by type for display. Order: Country, Person, Company, Topic. */
    val interestsGroupedByType: List<Pair<InterestType, List<Interest>>> get() =
        INTEREST_TYPE_ORDER.map { type ->
            type to allInterests.filterByType(type)
        }.filter { it.second.isNotEmpty() }

    /** Currently followed interest IDs. Updates immediately when follow/unfollow is called. */
    private val _followedIds = MutableStateFlow(model.getFollowedInterestIds())
    val followedIds: StateFlow<Set<String>> = _followedIds.asStateFlow()

    /** Optional filter: show only this type, or null for all. */
    private val _typeFilter = MutableStateFlow<InterestType?>(null)
    val typeFilter: StateFlow<InterestType?> = _typeFilter.asStateFlow()

    /** Interests to show: either grouped (when no filter) or single group (when filter set). */
    fun getInterestsToShow(): List<Pair<InterestType, List<Interest>>> =
        _typeFilter.value?.let { type ->
            val list = allInterests.filterByType(type)
            if (list.isEmpty()) emptyList() else listOf(type to list)
        } ?: interestsGroupedByType

    fun setTypeFilter(type: InterestType?) {
        _typeFilter.value = type
    }

    fun follow(id: String) {
        model.followInterest(id)
        _followedIds.update { it + id }
    }

    fun unfollow(id: String) {
        model.unfollowInterest(id)
        _followedIds.update { it - id }
    }

    fun isFollowed(id: String): Boolean = _followedIds.value.contains(id)

    fun toggle(id: String) {
        if (isFollowed(id)) unfollow(id) else follow(id)
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
