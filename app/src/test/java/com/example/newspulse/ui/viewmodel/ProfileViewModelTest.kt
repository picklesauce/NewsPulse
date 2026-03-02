package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class ProfileViewModelTest {

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: ProfileViewModel

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
        viewModel = ProfileViewModel(model)
    }

    @Test
    fun username_returnsModelUsername() {
        assertEquals("preview_user", viewModel.username)
    }

    @Test
    fun username_fallsBackWhenEmpty() {
        model.setUsername("")
        val vm = ProfileViewModel(model)
        assertEquals("username123", vm.username)
    }

    @Test
    fun memberSince_returnsModelValue() {
        assertEquals("Feb 2026", viewModel.memberSince)
    }

    @Test
    fun interests_returnsFollowedInterestNames() {
        val techId = model.getAllInterests().find { it.name == "Technology" }!!.id
        model.followInterest(techId)
        val vm = ProfileViewModel(model)
        assertTrue(vm.interests.contains("Technology"))
    }

    @Test
    fun readingHistory_returnsModelHistory() {
        assertTrue(viewModel.readingHistory.isEmpty())
    }
}
