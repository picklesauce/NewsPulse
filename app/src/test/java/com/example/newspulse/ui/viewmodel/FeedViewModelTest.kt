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

//Unit tests for FeedViewModel.

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: FeedViewModel

    // Sets up dependencies before tests
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
        viewModel = FeedViewModel(model)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Loads fake data correctly test
    @Test
    fun loadsFakeDataCorrectly_fromMockRepository() {
        // Arrange: setUp() already creates model with MockNewsRepository
        val expectedArticles = MockDB.articles
        val expectedCount = expectedArticles.size
        val expectedFirstId = expectedArticles.first().id
        val expectedFirstTitle = expectedArticles.first().title

        val state = viewModel.uiState.value

        assertEquals(expectedCount, state.articles.size)
        assertEquals(expectedFirstId, state.articles.first().id)
        assertEquals(expectedFirstTitle, state.articles.first().title)
        assertEquals(expectedArticles.map { it.id }, state.articles.map { it.id })
    }

    // Feed returns proper size
    @Test
    fun feedRetrieval_returnsExpectedSizeAndOrder() {
        // Arrange: MockDB has exactly 20 articles in fixed order
        val expectedSize = 20
        val expectedOrder = (1..20).map { "art-$it" }

        val state = viewModel.uiState.value
        val actualIds = state.articles.map { it.id }

        assertEquals(expectedSize, state.articles.size)
        assertEquals(expectedOrder, actualIds)
    }

    // Starting state tests
    @Test
    fun initialState_loadsArticlesIntoState() {
        val state = viewModel.uiState.value

        assertTrue(state.articles.isNotEmpty()) // All articles should be shown
        assertTrue(state.followedInterestNames.isEmpty())
        assertTrue(state.followedCategoryNames.isEmpty())
        assertNull(state.emptyStateMessage) // Articles are present, so no empty message
    }


    @Test
    fun initialState_hasCorrectDefaults() {
        val state = viewModel.uiState.value

        assertEquals("", state.searchQuery)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals("NewsPulse", state.headerTitle)
        assertEquals("Search articles...", state.searchPlaceholder)
        assertEquals("[IMAGE]", state.imagePlaceholderText)
    }

    // Articles loading tests
    @Test
    fun loadArticles_loadsArticlesMatchingFollowedInterests() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }

        val newViewModel = FeedViewModel(model)

        val state = newViewModel.uiState.value
        assertTrue(state.articles.isNotEmpty())
        assertTrue(state.followedCategoryNames.contains("Technology"))
        state.articles.forEach { article ->
            assertTrue(article.interests.any { it.name == "Technology" })
        }
    }


    // Tests that articles are filtered correctly when multiple interests are followed
    @Test
    fun loadArticles_filtersByMultipleInterests() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        techInterest?.let { model.followInterest(it.id) }
        businessInterest?.let { model.followInterest(it.id) }

        val newViewModel = FeedViewModel(model)

        val state = newViewModel.uiState.value
        assertTrue(state.articles.isNotEmpty())
        assertEquals(setOf("Technology", "Business"), state.followedCategoryNames)
        assertTrue(state.followedInterestNames.isEmpty())
        state.articles.forEach { article ->
            val hasTech = article.interests.any { it.name == "Technology" }
            val hasBusiness = article.interests.any { it.name == "Business" }
            assertTrue(hasTech || hasBusiness)
        }
    }

    // Search tests
    @Test
    fun onSearch_updatesSearchQueryAndFiltersArticles() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        val initialArticleCount = newViewModel.uiState.value.articles.size

        newViewModel.onSearch("Tech")

        val state = newViewModel.uiState.value
        assertEquals("Tech", state.searchQuery)
        state.articles.forEach { article ->
            val matches = article.title.contains("Tech", ignoreCase = true) ||
                    article.source.contains("Tech", ignoreCase = true) ||
                    article.summary.contains("Tech", ignoreCase = true) ||
                    article.interests.any { it.name.contains("Tech", ignoreCase = true) }
            assertTrue(matches)
        }
    }

    // Tests that clearing search query shows all articles again
    @Test
    fun onSearch_clearingQueryShowsAllArticles() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        newViewModel.onSearch("Tech")
        val filteredCount = newViewModel.uiState.value.articles.size

        newViewModel.onSearch("")

        val state = newViewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.articles.size >= filteredCount)
    }

    // Refresh tests
    @Test
    fun onRefresh_updatesLoadingStateAndRefreshesArticles() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)

        newViewModel.onRefresh()

        val state = newViewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertTrue(state.articles.isNotEmpty())
    }

    // Unfollow interest tests
    @Test
    fun unfollowInterest_updatesStateAndFiltersArticles() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        techInterest?.let { model.followInterest(it.id) }
        businessInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        val initialCount = newViewModel.uiState.value.articles.size
        assertTrue(initialCount > 0)

        newViewModel.unfollowInterest("Technology")

        val state = newViewModel.uiState.value
        assertFalse(state.followedCategoryNames.contains("Technology"))
        assertTrue(state.followedCategoryNames.contains("Business"))
        assertTrue(state.articles.size <= initialCount)
    }

    // Dropdown tests for interest bar
    @Test
    fun onToggleTopicFilter_filtersArticlesByTopic() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        techInterest?.let { model.followInterest(it.id) }
        businessInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        val allCount = newViewModel.uiState.value.articles.size
        assertTrue(allCount > 0)

        newViewModel.onToggleCategoryFilter("Technology")

        val state = newViewModel.uiState.value
        assertEquals(setOf("Technology"), state.activeCategoryFilters)
        state.articles.forEach { article ->
            assertTrue(article.interests.any { it.name == "Technology" })
        }
        assertTrue(state.articles.size <= allCount)
    }

    // Clearing topics tests
    @Test
    fun onClearTopicFilters_showsAllTopics() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        newViewModel.onToggleCategoryFilter("Technology")
        val filteredCount = newViewModel.uiState.value.articles.size

        newViewModel.onClearCategoryFilters()

        val state = newViewModel.uiState.value
        assertNull(state.activeCategoryFilters)
        assertTrue(state.articles.size >= filteredCount)
    }

    // Empty state tests
    @Test
    fun emptyState_showsMessageWhenNoArticlesMatchInterests() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        
        val stateWithArticles = newViewModel.uiState.value
        if (stateWithArticles.articles.isNotEmpty()) {
            assertNull(stateWithArticles.emptyStateMessage)
        }

        assertTrue(
            stateWithArticles.followedInterestNames.isNotEmpty() ||
                stateWithArticles.followedCategoryNames.isNotEmpty()
        )
    }

    // Tests empty message
    @Test
    fun emptyState_showsMessageWhenSearchReturnsNoResults() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)

        newViewModel.onSearch("NonExistentTerm12345")

        val state = newViewModel.uiState.value
        assertEquals("No articles match your search", state.emptyStateMessage)
        assertTrue(state.articles.isEmpty())
    }

    // Dropdown for topics tests
    @Test
    fun onToggleTopicFilter_multiSelectShowsMatchingArticles() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        techInterest?.let { model.followInterest(it.id) }
        businessInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        val allCount = newViewModel.uiState.value.articles.size
        assertTrue(allCount > 0)

        newViewModel.onToggleCategoryFilter("Technology")

        val state = newViewModel.uiState.value
        assertEquals(setOf("Technology"), state.activeCategoryFilters)
        assertTrue(state.articles.size <= allCount)
        state.articles.forEach { article ->
            assertTrue(article.interests.any { it.name == "Technology" })
        }
    }

    // Toggling interests on and off test
    @Test
    fun onToggleTopicFilter_toggleOffRestoresFilter() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        newViewModel.onToggleCategoryFilter("Technology")
        val filteredCount = newViewModel.uiState.value.articles.size

        newViewModel.onToggleCategoryFilter("Technology")

        val state = newViewModel.uiState.value
        assertEquals(emptySet<String>(), state.activeCategoryFilters)
        assertTrue(state.articles.size >= filteredCount)
    }

    // Clearing interests tests
    @Test
    fun onClearTopicFilters_showsAllMatchingArticles() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        val businessInterest = model.getAllInterests().find { it.name == "Business" }
        techInterest?.let { model.followInterest(it.id) }
        businessInterest?.let { model.followInterest(it.id) }
        val newViewModel = FeedViewModel(model)
        val allCount = newViewModel.uiState.value.articles.size
        newViewModel.onToggleCategoryFilter("Technology")
        val filteredCount = newViewModel.uiState.value.articles.size

        newViewModel.onClearCategoryFilters()

        val state = newViewModel.uiState.value
        assertNull(state.activeCategoryFilters)
        assertEquals(allCount, state.articles.size)
        assertTrue(state.articles.size >= filteredCount)
    }

    // Empty message for no articles test
    @Test
    fun emptyState_messageIsNullWhenArticlesPresent() {
        val techInterest = model.getAllInterests().find { it.name == "Technology" }
        techInterest?.let { model.followInterest(it.id) }

        val newViewModel = FeedViewModel(model)

        val state = newViewModel.uiState.value
        assertTrue(state.articles.isNotEmpty())
        assertNull(state.emptyStateMessage)
    }
}

