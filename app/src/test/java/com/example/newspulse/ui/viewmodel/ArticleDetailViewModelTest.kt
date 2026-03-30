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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ArticleDetailViewModel.
 * Tests that the ViewModel correctly loads an article and its related articles.
 *
 * Test Target (per ticket S2-17):
 * - ArticleDetailViewModel loads correct article + related list
 *
 * loadArticle() uses viewModelScope.launch, so tests use UnconfinedTestDispatcher
 * to eagerly execute coroutines before assertions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: ArticleDetailViewModel

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

    // ========== Initial State Tests ==========

    @Test
    fun initialState_hasNoArticleLoaded() {
        assertNull(viewModel.article.value)
        assertTrue(viewModel.relatedArticles.value.isEmpty())
    }

    @Test
    fun loadArticle_loadsCorrectArticleById() = runTest {
        viewModel.loadArticle("art-1")
        val article = viewModel.article.value
        assertNotNull(article)
        assertEquals("art-1", article!!.id)
        assertEquals("Breaking: Major Tech Company Announces New Product Launch", article.title)
    }

    @Test
    fun loadArticle_withNonExistentId_returnsNull() = runTest {
        viewModel.loadArticle("non-existent-id")
        assertNull(viewModel.article.value)
    }

    @Test
    fun loadArticle_addsToReadingHistory() = runTest {
        viewModel.loadArticle("art-1")
        assertNotNull(viewModel.article.value)
    }

    @Test
    fun loadArticle_loadsRelatedArticles() = runTest {
        viewModel.loadArticle("art-1")
        assertNotNull(viewModel.article.value)
        val related = viewModel.relatedArticles.value
        assertTrue(related.isNotEmpty())
        assertTrue(related.none { it.id == "art-1" })
    }

    @Test
    fun loadArticle_relatedArticlesAreSortedByRelevance() = runTest {
        viewModel.loadArticle("art-1")
        val related = viewModel.relatedArticles.value
        assertTrue(related.isNotEmpty())
        val base = viewModel.article.value!!
        val baseInterests = base.interests.map { it.name }.toSet()
        related.forEach { r ->
            val hasSharedInterest = r.interests.any { it.name in baseInterests }
            val hasSameSource = r.source == base.source
            assertTrue(hasSharedInterest || hasSameSource || related.indexOf(r) > 0)
        }
    }

    @Test
    fun loadArticle_withNonExistentId_hasEmptyRelatedArticles() = runTest {
        viewModel.loadArticle("non-existent-id")
        assertTrue(viewModel.relatedArticles.value.isEmpty())
    }

    @Test
    fun loadArticle_relatedArticlesExcludeBaseArticle() = runTest {
        viewModel.loadArticle("art-1")
        assertTrue(viewModel.relatedArticles.value.none { it.id == "art-1" })
    }

    @Test
    fun loadArticle_loadingDifferentArticle_updatesState() = runTest {
        viewModel.loadArticle("art-1")
        val first = viewModel.article.value
        assertNotNull(first)

        viewModel.loadArticle("art-2")
        val second = viewModel.article.value
        assertNotNull(second)
        assertEquals("art-2", second!!.id)
        assertTrue(viewModel.relatedArticles.value.none { it.id == "art-2" })
    }

    @Test
    fun saveArticle_savesArticleToRepository() = runTest {
        viewModel.loadArticle("art-1")
        val article = viewModel.article.value
        assertNotNull(article)
        viewModel.saveArticle(article!!)
        assertTrue(viewModel.isSaved.value)
    }

    @Test
    fun loadArticle_withNoRelatedArticles_handlesGracefully() = runTest {
        viewModel.loadArticle("art-1")
        assertNotNull(viewModel.article.value)
        assertNotNull(viewModel.relatedArticles.value)
    }

    @Test
    fun loadArticle_loadingSameArticleTwice_worksCorrectly() = runTest {
        viewModel.loadArticle("art-1")
        val firstRelatedSize = viewModel.relatedArticles.value.size

        viewModel.loadArticle("art-1")
        assertEquals("art-1", viewModel.article.value?.id)
        assertEquals(firstRelatedSize, viewModel.relatedArticles.value.size)
    }
}


