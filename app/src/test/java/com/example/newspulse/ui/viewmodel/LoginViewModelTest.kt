package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class LoginViewModelTest {

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: LoginViewModel

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
        viewModel = LoginViewModel(model)
    }

    @Test
    fun initialState_loadsUsernameFromModel() {
        val state = viewModel.uiState.value
        assertEquals("preview_user", state.username)
        assertEquals("", state.email)
    }

    @Test
    fun updateUsername_updatesState() {
        viewModel.updateUsername("alice")
        assertEquals("alice", viewModel.uiState.value.username)
    }

    @Test
    fun updateEmail_updatesState() {
        viewModel.updateEmail("alice@example.com")
        assertEquals("alice@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun saveLogin_persistsUsernameToModel() {
        viewModel.updateUsername("bob")
        viewModel.saveLogin()
        assertEquals("bob", model.getUsername())
    }
}
