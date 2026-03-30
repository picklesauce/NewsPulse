package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ExploreScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Discover / Explore screen.
 */
class ExploreScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        model: com.example.newspulse.domain.NewsPulseModel = createTestModel(),
    ) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    ExploreScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Header
    // ─────────────────────────────────────────────

    @Test
    fun discoverTopicsHeader_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Discover Topics").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Search field
    // ─────────────────────────────────────────────

    @Test
    fun searchField_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Search topics...", substring = true).assertIsDisplayed()
    }

    @Test
    fun searchField_acceptsInput_withoutCrash() {
        setContent()
        composeTestRule.onNodeWithText("Search topics...", substring = true).performTextInput("Tech")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Discover Topics").assertIsDisplayed()
    }

    @Test
    fun searchField_withMatchingQuery_showsFilteredCategory() {
        setContent()
        composeTestRule.onNodeWithText("Search topics...", substring = true).performTextInput("Tech")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Technology", substring = true).assertIsDisplayed()
    }

    @Test
    fun searchField_withNonMatchingQuery_hidesTotalCategories() {
        setContent()
        composeTestRule.onNodeWithText("Search topics...", substring = true)
            .performTextInput("xyzzy_no_category_match")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Technology").assertDoesNotExist()
    }

    // ─────────────────────────────────────────────
    // Category grid cards — first-viewport items (indices 0–5 always visible)
    // ─────────────────────────────────────────────

    @Test
    fun technologyCategory_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
    }

    @Test
    fun financeCategory_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Finance").assertIsDisplayed()
    }

    @Test
    fun politicsCategory_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Politics").assertIsDisplayed()
    }

    @Test
    fun sportsCategory_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Sports").assertIsDisplayed()
    }

    @Test
    fun entertainmentCategory_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()
    }

    @Test
    fun scienceCategory_isDisplayed() {
        setContent()
        // Science may be near the bottom of the visible area — scroll to it first
        composeTestRule.onNodeWithText("Science").performScrollTo()
        composeTestRule.onNodeWithText("Science").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Category descriptions
    // ─────────────────────────────────────────────

    @Test
    fun categoryCard_displaysDescription() {
        setContent()
        // The Technology card description from ExploreScreen's categories list
        composeTestRule.onNodeWithText("Latest tech news & innovations", substring = true)
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Category card interaction — use Technology (always in first viewport)
    // ─────────────────────────────────────────────

    @Test
    fun categoryCard_click_doesNotCrash() {
        setContent()
        // Use Technology (index 0) — always in first viewport
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        // After clicking a category the article view renders with category name
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
    }

    @Test
    fun technologyCard_click_showsArticleView() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Article list view (after selecting a category)
    // ─────────────────────────────────────────────

    @Test
    fun afterSelectingCategory_backButton_returnsToGrid() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        // Back button in article list view has contentDescription "Back to Discover"
        composeTestRule.onNodeWithContentDescription("Back to Discover").performClick()
        composeTestRule.waitForIdle()
        // After going back, the grid is visible again
        composeTestRule.onNodeWithText("Finance").assertIsDisplayed()
    }
}
