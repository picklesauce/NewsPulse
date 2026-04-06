package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.InterestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


//Unit tests for InterestsViewModel.
@OptIn(ExperimentalCoroutinesApi::class)
class InterestsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: InterestsViewModel

    // Sets up test dependencies
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Start state tests
    @Test
    fun initialState_loadsAllInterestsGroupedByType() {
        val state = viewModel.uiState.value

        assertTrue(state.interestsToShow.isNotEmpty())
        assertTrue(state.followedIds.isEmpty())
        assertNull(state.typeFilter)
        
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
        assertEquals("Follow or unfollow to personalize your feed, or add your own.", state.subtitle)
        assertEquals("All", state.filterAllLabel)
        assertEquals("Showing: %s", state.showingFilterLabel)
    }

    // Tests for following and unfollowing
    @Test
    fun followUnfollow_changesFollowedSet() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id

        viewModel.onFollowToggle(techId)

        assertEquals(setOf(techId), viewModel.uiState.value.followedIds)

        viewModel.onFollowToggle(businessId)

        assertEquals(setOf(techId, businessId), viewModel.uiState.value.followedIds)

        viewModel.onFollowToggle(techId)

        assertEquals(setOf(businessId), viewModel.uiState.value.followedIds)

        viewModel.onFollowToggle(businessId)

        assertTrue(viewModel.uiState.value.followedIds.isEmpty())
    }

    // Follow interest tests

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

        viewModel.onFollowToggle(interestId)

        val state = viewModel.uiState.value
        assertTrue(state.followedIds.contains(interestId))
        assertEquals(initialState.followedIds.size + 1, state.followedIds.size)
    }

    // Following multiple interests tests
    @Test
    fun onFollowToggle_followingMultipleInterests_updatesState() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id

        viewModel.onFollowToggle(techId)
        viewModel.onFollowToggle(businessId)

        val state = viewModel.uiState.value
        assertTrue(state.followedIds.contains(techId))
        assertTrue(state.followedIds.contains(businessId))
        assertEquals(2, state.followedIds.size)
    }

    // Unfollow interest tests
    @Test
    fun onFollowToggle_unfollowingInterest_updatesState() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        assertTrue(techInterest != null)
        val interestId = techInterest!!.id
        viewModel.onFollowToggle(interestId)
        assertTrue(viewModel.uiState.value.followedIds.contains(interestId))

        viewModel.onFollowToggle(interestId)

        val state = viewModel.uiState.value
        assertFalse(state.followedIds.contains(interestId))
        assertTrue(state.followedIds.isEmpty())
    }

    // Unfollow interest affecting others tests
    @Test
    fun onFollowToggle_unfollowingOneInterest_keepsOthersFollowed() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id
        
        viewModel.onFollowToggle(techId)
        viewModel.onFollowToggle(businessId)
        assertEquals(2, viewModel.uiState.value.followedIds.size)

        viewModel.onFollowToggle(techId)

        val state = viewModel.uiState.value
        assertFalse(state.followedIds.contains(techId))
        assertTrue(state.followedIds.contains(businessId))
        assertEquals(1, state.followedIds.size)
    }

    // State consistency tests
    @Test
    fun onFollowToggle_syncsWithModel() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        assertTrue(techInterest != null)
        val interestId = techInterest!!.id

        viewModel.onFollowToggle(interestId)

        assertTrue(model.getFollowedInterestIds().contains(interestId))
        
        viewModel.onFollowToggle(interestId)

        assertFalse(model.getFollowedInterestIds().contains(interestId))
    }

    // UI state tests
    @Test
    fun uiState_reflectsCurrentFollowedIds() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        val scienceInterest = model.getAllInterests().find { it.name == "Science" }
        assertTrue(techInterest != null)
        assertTrue(businessInterest != null)
        assertTrue(scienceInterest != null)
        
        val techId = techInterest!!.id
        val businessId = businessInterest!!.id
        val scienceId = scienceInterest!!.id

        viewModel.onFollowToggle(techId)
        viewModel.onFollowToggle(businessId)
        viewModel.onFollowToggle(scienceId)
        viewModel.onFollowToggle(businessId)

        val state = viewModel.uiState.value
        assertTrue(state.followedIds.contains(techId))
        assertFalse(state.followedIds.contains(businessId))
        assertTrue(state.followedIds.contains(scienceId))
        assertEquals(setOf(techId, scienceId), state.followedIds)
    }

    // Interest filter tests
    @Test
    fun setTypeFilter_updatesState() {
        val initialState = viewModel.uiState.value
        assertNull(initialState.typeFilter)

        viewModel.setTypeFilter(InterestType.Topic)

        val state = viewModel.uiState.value
        assertEquals(InterestType.Topic, state.typeFilter)
        state.interestsToShow.forEach { (type, _) ->
            assertEquals(InterestType.Topic, type)
        }
    }

    // Clearing interest tests
    @Test
    fun setTypeFilter_clearingFilterShowsAllInterests() {
        viewModel.setTypeFilter(InterestType.Topic)
        val filteredCount = viewModel.uiState.value.interestsToShow.sumOf { it.second.size }

        viewModel.setTypeFilter(null)

        val state = viewModel.uiState.value
        assertNull(state.typeFilter)
        val allCount = state.interestsToShow.sumOf { it.second.size }
        assertTrue(allCount >= filteredCount)
    }
}

