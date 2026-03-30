package com.example.newspulse.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.FakeInterestsRepository
import com.example.newspulse.data.mock.FakeNewsRepository
import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.domain.InterestsRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.ReadingHistoryRepository
import com.example.newspulse.domain.SavedArticlesRepository
import com.example.newspulse.domain.UserPreferencesRepository
import com.example.newspulse.domain.model.ReadingHistoryItem

/**
 * Creates a [NewsPulseModel] backed by in-memory fakes.
 * Optionally override individual repositories to test specific states.
 */
fun createTestModel(
    interestsRepository: InterestsRepository = FakeInterestsRepository(),
    savedArticlesRepository: SavedArticlesRepository = InMemorySavedArticlesRepository(),
    readingHistoryRepository: ReadingHistoryRepository = FakeReadingHistoryRepository(),
    userPreferencesRepository: UserPreferencesRepository = FakeUserPreferencesRepository(),
): NewsPulseModel = NewsPulseModel(
    newsRepository = FakeNewsRepository(),
    interestsRepository = interestsRepository,
    interestsCatalogRepository = MockInterestsCatalogRepository(),
    userPreferencesRepository = userPreferencesRepository,
    readingHistoryRepository = readingHistoryRepository,
    savedArticlesRepository = savedArticlesRepository,
)

/** Model whose saved-articles repo is pre-populated with the first three mock articles. */
fun createTestModelWithSavedArticles(): NewsPulseModel {
    val repo = InMemorySavedArticlesRepository().apply {
        MockDB.articles.take(3).forEach { saveArticle(it) }
    }
    return createTestModel(savedArticlesRepository = repo)
}

/** Model with a pre-seeded reading history of the first two mock articles. */
fun createTestModelWithReadingHistory(): NewsPulseModel {
    val repo = object : ReadingHistoryRepository {
        override fun getReadingHistory(): List<ReadingHistoryItem> = listOf(
            ReadingHistoryItem(
                articleId = MockDB.articles[0].id,
                title = MockDB.articles[0].title,
                readAtMillis = System.currentTimeMillis() - 3_600_000L,
            ),
            ReadingHistoryItem(
                articleId = MockDB.articles[1].id,
                title = MockDB.articles[1].title,
                readAtMillis = System.currentTimeMillis() - 7_200_000L,
            ),
        )
        override fun addToHistory(articleId: String, title: String) {}
    }
    return createTestModel(readingHistoryRepository = repo)
}

/** Model whose onboarding is NOT complete (for testing first-run flows). */
fun createTestModelNotOnboarded(): NewsPulseModel {
    val repo = object : FakeInterestsRepository() {
        override fun isOnboardingComplete() = false
    }
    return createTestModel(interestsRepository = repo)
}

/**
 * Wraps [content] with the [CompositionLocals.LocalViewModelFactory] provider
 * so that composables using `viewModel(factory = ...)` work under test.
 */
@Composable
fun WithFactory(factory: ViewModelFactory, content: @Composable (NavController) -> Unit) {
    CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
        content(rememberNavController())
    }
}
