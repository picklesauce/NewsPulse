package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
internal class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
        var result: Boolean? = null
        viewModel.logIn { result = it }
        assertEquals(false, result)
        assertEquals("Please enter your email or username", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logIn_withBlankPassword_returnsFalseAndShowsError() {
        viewModel.updateEmail("a@b.com")
        var result: Boolean? = null
        viewModel.logIn { result = it }
        assertEquals(false, result)
        assertEquals("Please enter your password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logIn_withWrongCredentials_returnsFalseAndShowsError() {
        viewModel.updateEmail("wrong@example.com")
        viewModel.updatePassword("wrongpass")
        val latch = CountDownLatch(1)
        var result = true
        viewModel.logIn {
            result = it
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertFalse(result)
        assertEquals("Invalid email or password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun logIn_withMatchingCredentials_returnsTrue() {
        viewModel.updateEmail("preview@example.com")
        viewModel.updatePassword("preview")
        val latch = CountDownLatch(1)
        var result = false
        viewModel.logIn {
            result = it
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(result)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }
}
