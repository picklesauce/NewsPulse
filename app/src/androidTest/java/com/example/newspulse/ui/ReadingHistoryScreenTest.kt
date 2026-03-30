package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ReadingHistoryScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Reading History screen (full reading history page).
 */
class ReadingHistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        model: com.example.newspulse.domain.NewsPulseModel = createTestModel(),
    ) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    ReadingHistoryScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Header
    // ─────────────────────────────────────────────

    @Test
    fun readingHistoryHeader_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Reading History").assertIsDisplayed()
    }

    @Test
    fun backButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun backButton_isEnabled() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back").assertIsEnabled()
    }

    @Test
    fun backButton_click_doesNotCrash() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Empty state
    // ─────────────────────────────────────────────

    @Test
    fun emptyState_isDisplayed_whenNoHistory() {
        setContent()
        composeTestRule.onNodeWithText("No articles read yet").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Populated state
    // ─────────────────────────────────────────────

    @Test
    fun historyItem_titles_areDisplayed() {
        setContent(createTestModelWithReadingHistory())
        composeTestRule.onNodeWithText(MockDB.articles[0].title).assertIsDisplayed()
        composeTestRule.onNodeWithText(MockDB.articles[1].title).assertIsDisplayed()
    }

    @Test
    fun historyItem_timeAgo_isDisplayed() {
        setContent(createTestModelWithReadingHistory())
        // The time-ago label for a 1-hour-old item is "1h ago"
        composeTestRule.onNodeWithText("1h ago").assertIsDisplayed()
    }

    @Test
    fun historyItem_isRendered_withoutCrash() {
        setContent(createTestModelWithReadingHistory())
        // Clicking navigates without NavHost — just verify the item is displayed
        composeTestRule.onNodeWithText(MockDB.articles[0].title).assertIsDisplayed()
        composeTestRule.onNodeWithText("Reading History").assertIsDisplayed()
    }

    @Test
    fun allHistoryItems_areDisplayed_notJustFive() {
        // Build a model with more than 5 history entries to confirm full history is shown
        val manyHistoryRepo = object : com.example.newspulse.domain.ReadingHistoryRepository {
            override fun getReadingHistory(): List<com.example.newspulse.domain.model.ReadingHistoryItem> =
                MockDB.articles.take(8).mapIndexed { i, article ->
                    com.example.newspulse.domain.model.ReadingHistoryItem(
                        articleId = article.id,
                        title = article.title,
                        readAtMillis = System.currentTimeMillis() - (i + 1) * 3_600_000L,
                    )
                }
            override fun addToHistory(articleId: String, title: String) {}
        }
        setContent(createTestModel(readingHistoryRepository = manyHistoryRepo))
        // All items are in a regular verticalScroll Column (non-lazy) — scroll to find them
        composeTestRule.onNodeWithText(MockDB.articles[5].title).performScrollTo()
        composeTestRule.onNodeWithText(MockDB.articles[5].title).assertIsDisplayed()
        composeTestRule.onNodeWithText(MockDB.articles[7].title).performScrollTo()
        composeTestRule.onNodeWithText(MockDB.articles[7].title).assertIsDisplayed()
    }
}
