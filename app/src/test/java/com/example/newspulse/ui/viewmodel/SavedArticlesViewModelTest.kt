package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SavedArticlesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var model: NewsPulseModel
    private lateinit var savedRepo: InMemorySavedArticlesRepository
    private lateinit var viewModel: SavedArticlesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedRepo = InMemorySavedArticlesRepository()
        model = NewsPulseModel(
            newsRepository = MockNewsRepository(),
            interestsRepository = MockInterestsRepository(),
            interestsCatalogRepository = MockInterestsCatalogRepository(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
            readingHistoryRepository = FakeReadingHistoryRepository(),
            savedArticlesRepository = savedRepo
        )
        viewModel = SavedArticlesViewModel(model)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasNoSavedArticles() {
        assertTrue(viewModel.savedArticles.value.isEmpty())
    }

    @Test
    fun saveArticle_appearsInSavedFlow() = runTest {
        val article = MockDB.articles.first()
        model.saveArticle(article)
        val saved = model.getSavedArticles().first()
        assertTrue(saved.any { it.id == article.id })
    }

    @Test
    fun removeArticle_removesFromSavedFlow() = runTest {
        val article = MockDB.articles.first()
        model.saveArticle(article)
        model.removeArticle(article)
        val saved = model.getSavedArticles().first()
        assertTrue(saved.none { it.id == article.id })
    }

    @Test
    fun saveDuplicateArticle_doesNotDuplicate() = runTest {
        val article = MockDB.articles.first()
        model.saveArticle(article)
        model.saveArticle(article)
        val saved = model.getSavedArticles().first()
        assertEquals(1, saved.count { it.id == article.id })
    }
}
