package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ArticleDetailScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Article Detail screen.
 */
class ArticleDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Article IDs come from MockDB; the model resolves them via MockNewsRepository. */
    private val testArticle = MockDB.articles.first()

    private fun setContent(
        articleId: String? = testArticle.id,
        model: com.example.newspulse.domain.NewsPulseModel = createTestModel(),
    ) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    ArticleDetailScreen(
                        navController = rememberNavController(),
                        articleId = articleId,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Top bar
    // ─────────────────────────────────────────────

    @Test
    fun backButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun shareButton_isDisplayed_whenArticleLoaded() {
        setContent()
        // Content description is "Share article"
        composeTestRule.onNodeWithContentDescription("Share article").assertIsDisplayed()
    }

    @Test
    fun backButton_isClickable() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Article content
    // ─────────────────────────────────────────────

    @Test
    fun articleTitle_isDisplayed_inTopBar() {
        setContent()
        composeTestRule.onNodeWithText(testArticle.title).assertIsDisplayed()
    }

    @Test
    fun articleSource_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText(testArticle.source).assertIsDisplayed()
    }

    @Test
    fun articleSummary_isDisplayed() {
        setContent()
        // Summary is the article body; at least a substring should appear
        composeTestRule.onNodeWithText(testArticle.summary, substring = true).assertIsDisplayed()
    }

    @Test
    fun unknownArticleId_showsArticleNotFound() {
        setContent(articleId = "nonexistent-id-999")
        composeTestRule.onNodeWithText("Article Not Found").assertIsDisplayed()
    }

    @Test
    fun nullArticleId_showsArticleNotFound() {
        setContent(articleId = null)
        composeTestRule.onNodeWithText("Article Not Found").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Save button
    // ─────────────────────────────────────────────

    @Test
    fun saveArticleButton_isDisplayed_withCorrectLabel() {
        setContent()
        composeTestRule.onNodeWithText("Save Article").assertIsDisplayed()
    }

    @Test
    fun saveArticleButton_isEnabled_whenNotSaved() {
        setContent()
        composeTestRule.onNodeWithText("Save Article").assertIsEnabled()
    }

    @Test
    fun saveArticleButton_click_changesTextToSaved() {
        setContent()
        composeTestRule.onNodeWithText("Save Article").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Saved").assertIsDisplayed()
    }

    @Test
    fun saveArticleButton_isDisabled_afterSaving() {
        setContent()
        composeTestRule.onNodeWithText("Save Article").performClick()
        composeTestRule.waitForIdle()
        // Saved state renders button as disabled
        composeTestRule.onNodeWithText("Saved").assertIsNotEnabled()
    }

    @Test
    fun alreadySavedArticle_showsSavedButton_disabled() {
        val repo = InMemorySavedArticlesRepository().apply { saveArticle(testArticle) }
        setContent(model = createTestModel(savedArticlesRepository = repo))
        composeTestRule.onNodeWithText("Saved").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved").assertIsNotEnabled()
    }

    @Test
    fun saveArticleButtonText_isNotSaveForOffline() {
        setContent()
        // Old label should never appear
        composeTestRule.onNodeWithText("Save for Offline").assertDoesNotExist()
    }

    // ─────────────────────────────────────────────
    // Related articles
    // ─────────────────────────────────────────────

    @Test
    fun youMightAlsoLike_section_isDisplayed_orAbsent_withoutCrash() {
        // When the feed is loaded the section should render; when empty it should be absent.
        // Either way, the screen must not crash.
        setContent()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Article Not Found").assertDoesNotExist()
    }
}
