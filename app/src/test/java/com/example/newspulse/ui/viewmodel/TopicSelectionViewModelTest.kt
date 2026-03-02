package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class TopicSelectionViewModelTest {

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: TopicSelectionViewModel

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
        viewModel = TopicSelectionViewModel(model)
    }

    @Test
    fun initialState_hasEmptySelectionAndQuery() {
        assertTrue(viewModel.selectedTopics.value.isEmpty())
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun allTopics_returnsInterestNamesFromModel() {
        val topics = viewModel.allTopics
        assertTrue(topics.contains("Technology"))
        assertTrue(topics.contains("Business"))
        assertTrue(topics.size > 5)
    }

    @Test
    fun toggleTopic_selectsAndDeselectsTopic() {
        viewModel.toggleTopic("Technology")
        assertTrue(viewModel.selectedTopics.value.contains("Technology"))

        viewModel.toggleTopic("Technology")
        assertFalse(viewModel.selectedTopics.value.contains("Technology"))
    }

    @Test
    fun updateSearchQuery_filtersTopics() {
        viewModel.updateSearchQuery("Tech")
        val filtered = viewModel.getFilteredTopics()
        assertTrue(filtered.contains("Technology"))
        assertFalse(filtered.contains("Sports"))
    }

    @Test
    fun saveAndContinue_persistsFollowedIdsAndCompletesOnboarding() {
        viewModel.toggleTopic("Technology")
        viewModel.toggleTopic("Science")
        viewModel.saveAndContinue()

        val followedNames = model.getFollowedInterests().map { it.name }.toSet()
        assertTrue(followedNames.contains("Technology"))
        assertTrue(followedNames.contains("Science"))
        assertTrue(model.isOnboardingComplete())
    }
}
