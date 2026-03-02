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

internal class FiltersViewModelTest {

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: FiltersViewModel

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
        viewModel = FiltersViewModel(model)
    }

    @Test
    fun initialState_loadsFollowedIdsFromModel() {
        assertEquals(model.getFollowedInterestIds(), viewModel.selectedIds.value)
    }

    @Test
    fun allInterests_returnsFullCatalog() {
        assertTrue(viewModel.allInterests.isNotEmpty())
        assertEquals(model.getAllInterests().size, viewModel.allInterests.size)
    }

    @Test
    fun toggleInterest_addsAndRemovesId() {
        val id = model.getAllInterests().first().id

        viewModel.toggleInterest(id)
        assertTrue(viewModel.selectedIds.value.contains(id))

        viewModel.toggleInterest(id)
        assertFalse(viewModel.selectedIds.value.contains(id))
    }

    @Test
    fun apply_persistsSelectedIdsToModel() {
        val techId = model.getAllInterests().find { it.name == "Technology" }!!.id
        viewModel.reset()
        viewModel.toggleInterest(techId)
        viewModel.apply()

        assertEquals(setOf(techId), model.getFollowedInterestIds())
    }

    @Test
    fun reset_clearsAllSelections() {
        val id = model.getAllInterests().first().id
        viewModel.toggleInterest(id)
        viewModel.reset()
        assertTrue(viewModel.selectedIds.value.isEmpty())
    }
}
