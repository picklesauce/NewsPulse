package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.FakeInterestsRepository
import com.example.newspulse.domain.InterestsRepository
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ArticleListScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Home / Article Feed screen.
 */
class ArticleListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(model: com.example.newspulse.domain.NewsPulseModel = createTestModel()) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    ArticleListScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // App header
    // ─────────────────────────────────────────────

    @Test
    fun appHeader_showsNewsPulseTitle() {
        setContent()
        composeTestRule.onNodeWithText("NewsPulse").assertIsDisplayed()
    }

    @Test
    fun appHeader_searchIcon_isVisible() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun appHeader_refreshIcon_isVisible() {
        setContent()
        // Content description is "Refresh feed"
        composeTestRule.onNodeWithContentDescription("Refresh feed").assertIsDisplayed()
    }

    @Test
    fun appHeader_profileIcon_isVisible() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────

    @Test
    fun searchIcon_click_expandsSearchBar() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search articles...").assertIsDisplayed()
    }

    @Test
    fun searchBar_textInput_filtersArticles() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search articles...").performTextInput("Tesla")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Tesla Announces New Electric Vehicle Line").assertIsDisplayed()
    }

    @Test
    fun searchBar_noMatch_showsEmptyState() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search articles...").performTextInput("xyzzy_no_match_123")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No articles match your search").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Article feed
    // ─────────────────────────────────────────────

    @Test
    fun feed_showsAtLeastOneArticle_withFollowedInterests() {
        setContent()
        composeTestRule
            .onNodeWithText("Breaking: Major Tech Company Announces New Product Launch")
            .assertIsDisplayed()
    }

    @Test
    fun feed_articleCard_displaysSource() {
        setContent()
        composeTestRule.onNodeWithText("TechNews").assertIsDisplayed()
    }

    @Test
    fun feed_refresh_buttonIsClickable() {
        setContent()
        // Content description is "Refresh feed"
        composeTestRule.onNodeWithContentDescription("Refresh feed").assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Refresh feed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("NewsPulse").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Interest filter dropdown
    // ─────────────────────────────────────────────

    @Test
    fun interestSectionLabel_isDisplayed() {
        setContent()
        // Section label above the dropdown is "Interests"
        composeTestRule.onNodeWithText("Interests").assertIsDisplayed()
    }

    @Test
    fun interestDropdown_showsAllInterestsValue_whenInterestsAreFollowed() {
        setContent()
        // FakeInterestsRepository follows Technology + Business → summary shows "All interests"
        composeTestRule.onNodeWithText("All interests").assertIsDisplayed()
    }

    @Test
    fun categoryDropdown_showsNoCategoriesLabel_whenNoneFollowed() {
        setContent()
        // No categories followed by default → shows "No categories followed"
        composeTestRule.onNodeWithText("No categories followed").assertIsDisplayed()
    }

    @Test
    fun categorySectionLabel_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Categories").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Fallback / empty state
    // ─────────────────────────────────────────────

    @Test
    fun emptyInterests_showsFallbackBanner() {
        // With no followed interests, the feed falls back to trending articles
        // and shows a banner instead of an empty-state message
        val noInterestsRepo = object : FakeInterestsRepository() {
            override fun getFollowedInterestIds(): Set<String> = emptySet()
        }
        setContent(createTestModel(interestsRepository = noInterestsRepo))
        composeTestRule
            .onNodeWithText("Showing trending articles while we find content for your interests")
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Navigation icons — assert presence, not click (no NavHost in test)
    // ─────────────────────────────────────────────

    @Test
    fun profileIcon_isEnabled() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Profile").assertIsEnabled()
    }
}
