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

internal class LoginViewModelTest {

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        val fakePrefs = FakeUserPreferencesRepository()
        model = NewsPulseModel(
            newsRepository = MockNewsRepository(),
            interestsRepository = MockInterestsRepository(),
            interestsCatalogRepository = MockInterestsCatalogRepository(),
            userPreferencesRepository = fakePrefs,
            readingHistoryRepository = FakeReadingHistoryRepository(),
            savedArticlesRepository = InMemorySavedArticlesRepository()
        )
        viewModel = LoginViewModel(model)
    }

    @Test
    fun initialState_hasEmptyEmailAndPassword() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
    }

    @Test
    fun updateEmail_updatesState() {
        viewModel.updateEmail("alice@example.com")
        assertEquals("alice@example.com", viewModel.uiState.value.email)
    }

    @Test
    fun updatePassword_updatesState() {
        viewModel.updatePassword("secret123")
        assertEquals("secret123", viewModel.uiState.value.password)
    }

    @Test
    fun logIn_withBlankEmail_returnsFalseAndShowsError() {
        viewModel.updatePassword("any")
        val result = viewModel.logIn()
        assertFalse(result)
        assertEquals("Please enter your email address", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logIn_withBlankPassword_returnsFalseAndShowsError() {
        viewModel.updateEmail("a@b.com")
        val result = viewModel.logIn()
        assertFalse(result)
        assertEquals("Please enter your password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logIn_withWrongCredentials_returnsFalseAndShowsError() {
        viewModel.updateEmail("wrong@example.com")
        viewModel.updatePassword("wrongpass")
        val result = viewModel.logIn()
        assertFalse(result)
        assertEquals("Invalid email or password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logIn_withMatchingCredentials_returnsTrue() {
        viewModel.updateEmail("preview@example.com")
        viewModel.updatePassword("preview")
        val result = viewModel.logIn()
        assertTrue(result)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }
}
