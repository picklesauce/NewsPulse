package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.SavedArticlesScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Saved Articles screen.
 */
class SavedArticlesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ─────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────

    private fun setContent(model: com.example.newspulse.domain.NewsPulseModel) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    SavedArticlesScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Header
    // ─────────────────────────────────────────────

    @Test
    fun header_displaysSavedArticlesTitle() {
        setContent(createTestModel())
        composeTestRule.onNodeWithText("Saved Articles").assertIsDisplayed()
    }

    @Test
    fun header_doesNotShow_hardcodedTimestamp() {
        setContent(createTestModel())
        // Verify the old fake timestamp "9:41" and battery "100%" are not shown
        composeTestRule.onNodeWithText("9:41").assertDoesNotExist()
        composeTestRule.onNodeWithText("100%").assertDoesNotExist()
    }

    // ─────────────────────────────────────────────
    // Empty state
    // ─────────────────────────────────────────────

    @Test
    fun emptyState_isDisplayed_whenNoSavedArticles() {
        setContent(createTestModel())
        composeTestRule.onNodeWithText("No saved articles yet").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Populated state
    // ─────────────────────────────────────────────

    @Test
    fun savedArticleTitle_isDisplayed() {
        setContent(createTestModelWithSavedArticles())
        composeTestRule
            .onNodeWithText(MockDB.articles[0].title)
            .assertIsDisplayed()
    }

    @Test
    fun savedArticleSource_isDisplayed() {
        setContent(createTestModelWithSavedArticles())
        composeTestRule.onNodeWithText(MockDB.articles[0].source).assertIsDisplayed()
    }

    @Test
    fun deleteButton_isDisplayed_forEachSavedArticle() {
        setContent(createTestModelWithSavedArticles())
        // Three articles pre-saved — there should be three delete buttons
        val deleteButtons = composeTestRule.onAllNodesWithContentDescription("Remove saved article")
        deleteButtons[0].assertIsDisplayed()
        deleteButtons[1].assertIsDisplayed()
        deleteButtons[2].assertIsDisplayed()
    }

    @Test
    fun deleteButton_click_removesArticle() {
        setContent(createTestModelWithSavedArticles())
        // Remove the first article
        composeTestRule.onAllNodesWithContentDescription("Remove saved article")[0].performClick()
        composeTestRule.waitForIdle()
        // The first article's title should no longer be visible
        composeTestRule.onNodeWithText(MockDB.articles[0].title).assertDoesNotExist()
    }

    @Test
    fun savedArticleRow_isDisplayed_andExists() {
        setContent(createTestModelWithSavedArticles())
        // Verify article row renders — clicking would navigate without a NavHost
        composeTestRule.onNodeWithText(MockDB.articles[0].title).assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Articles").assertIsDisplayed()
    }

    @Test
    fun afterDeletingAllArticles_emptyState_isShown() {
        val repo = InMemorySavedArticlesRepository().apply {
            saveArticle(MockDB.articles[0])
        }
        val model = createTestModel(savedArticlesRepository = repo)
        setContent(model)
        composeTestRule.onAllNodesWithContentDescription("Remove saved article")[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No saved articles yet").assertIsDisplayed()
    }
}
