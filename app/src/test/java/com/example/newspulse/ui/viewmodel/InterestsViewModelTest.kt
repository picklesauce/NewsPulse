package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for InterestsViewModel.
 * Tests that the ViewModel correctly updates state when interests are followed/unfollowed.
 * 
 * Test Target (per ticket S2-17):
 * - InterestsViewModel follow/unfollow updates state
 * 
 * Note: These are pure unit tests with no Android instrumentation.
 * StateFlow updates are synchronous, so no test dispatchers are needed.
 */
class InterestsViewModelTest {

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: InterestsViewModel

    /**
     * Sets up test dependencies before each test.
     * Creates a fresh NewsPulseModel with mock repositories for each test.
     */
    @Before
    fun setUp() {
        model = NewsPulseModel(
            newsRepository = MockNewsRepository(),
            interestsRepository = MockInterestsRepository(),
            interestsCatalogRepository = MockInterestsCatalogRepository(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
            readingHistoryRepository = FakeReadingHistoryRepository(),
            savedArticlesRepository = InMemorySavedArticlesRepository()
        )
        viewModel = InterestsViewModel(model)
    }

    // ========== Initial State Tests ==========

    /**
     * Tests that initial state loads all interests grouped by type.
     */
    @Test
    fun initialState_loadsAllInterestsGroupedByType() {
        // Act: ViewModel is initialized in setUp()
        val state = viewModel.uiState.value

        // Assert: Interests should be loaded and grouped by type
        assertTrue(state.interestsToShow.isNotEmpty())
        assertTrue(state.followedIds.isEmpty())
        assertNull(state.typeFilter)
        
        // Verify interests are grouped by type
        state.interestsToShow.forEach { (type, interests) ->
            assertTrue(interests.isNotEmpty())
            interests.forEach { interest ->
                assertEquals(type, interest.type)
            }
        }
    }

    /**
     * Tests that initial state has correct default values.
     */
    @Test
    fun initialState_hasCorrectDefaults() {
        val state = viewModel.uiState.value

        assertEquals("Interests", state.headerTitle)
        assertEquals("Follow or unfollow to personalize your feed. Changes apply immediately.", state.subtitle)
        assertEquals("All", state.filterAllLabel)
        assertEquals("Showing: %s", state.showingFilterLabel)
    }

    // ========== Follow Interest Tests ==========

    /**
     * Tests that following an interest updates the state correctly.
     */
    @Test
    fun onFollowToggle_followingInterest_updatesState() {
        // Arrange: Get an interest ID
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        assertTrue(techInterest != null)
        val interestId = techInterest!!.id
        val initialState = viewModel.uiState.value
        assertFalse(initialState.followedIds.contains(interestId))

        // Act: Follow the interest
        viewModel.onFollowToggle(interestId)

        // Assert: Interest should be in followedIds
        val state = viewModel.uiState.value
        assertTrue(state.followedIds.contains(interestId))
        assertEquals(initialState.followedIds.size + 1, state.followedIds.size)
    }

    /**
     * Tests that following multiple interests updates state correctly.
     */
    @Test
    fun onFollowToggle_followingMultipleInterests_updatesState() {
        // Arrange: Get multiple interest IDs
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id

        // Act: Follow both interests
        viewModel.onFollowToggle(techId)
        viewModel.onFollowToggle(businessId)

        // Assert: Both interests should be in followedIds
        val state = viewModel.uiState.value
        assertTrue(state.followedIds.contains(techId))
        assertTrue(state.followedIds.contains(businessId))
        assertEquals(2, state.followedIds.size)
    }

    // ========== Unfollow Interest Tests ==========

    /**
     * Tests that unfollowing an interest updates the state correctly.
     */
    @Test
    fun onFollowToggle_unfollowingInterest_updatesState() {
        // Arrange: Follow an interest first
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        assertTrue(techInterest != null)
        val interestId = techInterest!!.id
        viewModel.onFollowToggle(interestId)
        assertTrue(viewModel.uiState.value.followedIds.contains(interestId))

        // Act: Unfollow the interest (toggle again)
        viewModel.onFollowToggle(interestId)

        // Assert: Interest should be removed from followedIds
        val state = viewModel.uiState.value
        assertFalse(state.followedIds.contains(interestId))
        assertTrue(state.followedIds.isEmpty())
    }

    /**
     * Tests that unfollowing one interest doesn't affect others.
     */
    @Test
    fun onFollowToggle_unfollowingOneInterest_keepsOthersFollowed() {
        // Arrange: Follow multiple interests
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id
        
        viewModel.onFollowToggle(techId)
        viewModel.onFollowToggle(businessId)
        assertEquals(2, viewModel.uiState.value.followedIds.size)

        // Act: Unfollow one interest
        viewModel.onFollowToggle(techId)

        // Assert: Only the unfollowed interest should be removed
        val state = viewModel.uiState.value
        assertFalse(state.followedIds.contains(techId))
        assertTrue(state.followedIds.contains(businessId))
        assertEquals(1, state.followedIds.size)
    }

    // ========== State Consistency Tests ==========

    /**
     * Tests that the model's state is updated when following/unfollowing.
     * The ViewModel should sync with the domain model.
     */
    @Test
    fun onFollowToggle_syncsWithModel() {
        // Arrange: Get an interest
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        assertTrue(techInterest != null)
        val interestId = techInterest!!.id

        // Act: Follow via ViewModel
        viewModel.onFollowToggle(interestId)

        // Assert: Model should also have the interest followed
        assertTrue(model.getFollowedInterestIds().contains(interestId))
        
        // Act: Unfollow via ViewModel
        viewModel.onFollowToggle(interestId)

        // Assert: Model should also have the interest unfollowed
        assertFalse(model.getFollowedInterestIds().contains(interestId))
    }

    /**
     * Tests that UI state reflects the current followed IDs.
     */
    @Test
    fun uiState_reflectsCurrentFollowedIds() {
        // Arrange: Follow multiple interests
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        val scienceInterest = model.getAllInterests().find { it.name == "Science" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        assertTrue(scienceInterest != null)
        
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id
        val scienceId = scienceInterest!!.id

        // Act: Follow and unfollow interests
        viewModel.onFollowToggle(techId)
        viewModel.onFollowToggle(businessId)
        viewModel.onFollowToggle(scienceId)
        viewModel.onFollowToggle(businessId) // Unfollow business

        // Assert: UI state should reflect only currently followed interests
        val state = viewModel.uiState.value
        assertTrue(state.followedIds.contains(techId))
        assertFalse(state.followedIds.contains(businessId))
        assertTrue(state.followedIds.contains(scienceId))
        assertEquals(setOf(techId, scienceId), state.followedIds)
    }

    // ========== Type Filter Tests ==========

    /**
     * Tests that setting a type filter updates the state.
     */
    @Test
    fun setTypeFilter_updatesState() {
        // Arrange: Initial state has no filter
        val initialState = viewModel.uiState.value
        assertNull(initialState.typeFilter)

        // Act: Set type filter
        viewModel.setTypeFilter(InterestType.Topic)

        // Assert: Type filter should be updated
        val state = viewModel.uiState.value
        assertEquals(InterestType.Topic, state.typeFilter)
        // Only Topic interests should be shown
        state.interestsToShow.forEach { (type, _) ->
            assertEquals(InterestType.Topic, type)
        }
    }

    /**
     * Tests that clearing type filter shows all interests again.
     */
    @Test
    fun setTypeFilter_clearingFilterShowsAllInterests() {
        // Arrange: Set a filter
        viewModel.setTypeFilter(InterestType.Topic)
        val filteredCount = viewModel.uiState.value.interestsToShow.sumOf { it.second.size }

        // Act: Clear filter
        viewModel.setTypeFilter(null)

        // Assert: All interests should be shown again
        val state = viewModel.uiState.value
        assertNull(state.typeFilter)
        val allCount = state.interestsToShow.sumOf { it.second.size }
        assertTrue(allCount >= filteredCount)
    }
}

