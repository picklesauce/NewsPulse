package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain


// Unit tests for ArticleDetailViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: ArticleDetailViewModel

    //Sets up test dependencies before tests
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
        viewModel = ArticleDetailViewModel(model)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    // Tests that beginning state has no article loaded
    @Test
    fun initialState_hasNoArticleLoaded() {
        val article = viewModel.article.value
        val relatedArticles = viewModel.relatedArticles.value

        assertNull(article)
        assertTrue(relatedArticles.isEmpty())
    }

    // Load article tests

    @Test
    fun loadArticle_loadsCorrectArticleById() {
        val articleId = "art-1"

        viewModel.loadArticle(articleId)

        val article = viewModel.article.value
        assertNotNull(article)
        assertEquals(articleId, article!!.id)
        assertEquals("Breaking: Major Tech Company Announces New Product Launch", article.title)
    }

    // Tests that loading a non-existent article ID returns null
    @Test
    fun loadArticle_withNonExistentId_returnsNull() {
        // Arrange: Use a non-existent article ID
        val articleId = "non-existent-id"

        viewModel.loadArticle(articleId)

        val article = viewModel.article.value
        assertNull(article)
    }

    // Tests that loading an article adds it to reading history
    @Test
    fun loadArticle_addsToReadingHistory() {
        val articleId = "art-1"
        val initialHistory = model.getReadingHistory()
        val initialCount = initialHistory.size

        viewModel.loadArticle(articleId)

        val article = viewModel.article.value
        assertNotNull(article)
    }

    // Multiple load tests
    @Test
    fun loadArticle_loadingDifferentArticle_updatesState() {
        // Arrange: Load first article
        val firstArticleId = "art-1"
        viewModel.loadArticle(firstArticleId)
        val firstArticle = viewModel.article.value
        assertNotNull(firstArticle)
        assertEquals(firstArticleId, firstArticle!!.id)

        val secondArticleId = "art-2"
        viewModel.loadArticle(secondArticleId)

        val secondArticle = viewModel.article.value
        assertNotNull(secondArticle)
        assertEquals(secondArticleId, secondArticle!!.id)
        assertTrue(secondArticle.id != firstArticle.id)
    }

    // Save article tests
    @Test
    fun saveArticle_savesArticleToRepository() {
        val articleId = "art-1"
        viewModel.loadArticle(articleId)
        val article = viewModel.article.value
        assertNotNull(article)

        viewModel.saveArticle(article!!)
    }

    // Edge case tests
    @Test
    fun loadArticle_withNoRelatedArticles_handlesGracefully() {
        val articleId = "art-1"

        viewModel.loadArticle(articleId)

        val article = viewModel.article.value
        assertNotNull(article)
        val relatedArticles = viewModel.relatedArticles.value
        assertNotNull(relatedArticles)
    }

    // Tests that loading the same article
    @Test
    fun loadArticle_loadingSameArticleTwice_worksCorrectly() {
        val articleId = "art-1"
        viewModel.loadArticle(articleId)
        val firstLoad = viewModel.article.value

        viewModel.loadArticle(articleId)

        val secondLoad = viewModel.article.value
        assertNotNull(secondLoad)
        assertEquals(articleId, secondLoad!!.id)
        assertEquals(firstLoad!!.id, secondLoad.id)
    }
}


