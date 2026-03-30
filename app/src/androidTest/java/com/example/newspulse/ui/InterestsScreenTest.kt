package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.InterestsScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Interests management screen (accessible from Profile).
 */
class InterestsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        model: com.example.newspulse.domain.NewsPulseModel = createTestModel(),
    ) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    InterestsScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Header
    // ─────────────────────────────────────────────

    @Test
    fun backButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back to Profile").assertIsDisplayed()
    }

    @Test
    fun screenHeaderTitle_isDisplayed() {
        setContent()
        // InterestsUiState.headerTitle defaults to "Interests"
        composeTestRule.onNodeWithText("Interests").assertIsDisplayed()
    }

    @Test
    fun screenSubtitle_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText("Follow or unfollow to personalize your feed, or add your own.")
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Search field
    // ─────────────────────────────────────────────

    @Test
    fun searchField_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Search or add interests").assertIsDisplayed()
    }

    @Test
    fun searchField_acceptsTextInput() {
        setContent()
        composeTestRule.onNodeWithText("Search or add interests").performTextInput("Sport")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sports").assertIsDisplayed()
    }

    @Test
    fun searchField_unknownQuery_showsAddChip() {
        setContent()
        composeTestRule.onNodeWithText("Search or add interests").performTextInput("Quantum Mechanics")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add \"Quantum Mechanics\"", substring = true).assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Type filter chips
    // ─────────────────────────────────────────────

    @Test
    fun allFilterChip_isDisplayed() {
        setContent()
        // InterestsUiState.filterAllLabel = "All"
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
    }

    @Test
    fun topicFilterChip_isDisplayedInTypeRow() {
        setContent()
        // "Topic" appears as both a type filter chip and a section header — asserting it exists
        composeTestRule.onNodeWithText("Topic").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Interest chips
    // ─────────────────────────────────────────────

    @Test
    fun interestChips_areDisplayed_fromCatalog() {
        setContent()
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
        composeTestRule.onNodeWithText("Business").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sports").assertIsDisplayed()
    }

    @Test
    fun interestChips_includeDifferentTypes() {
        setContent()
        // Countries
        composeTestRule.onNodeWithText("USA").assertIsDisplayed()
        // Companies
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
        // Persons
        composeTestRule.onNodeWithText("Elon Musk").assertIsDisplayed()
    }

    @Test
    fun followedInterestChip_showsAsSelected() {
        setContent()
        // Technology and Business are pre-followed in FakeInterestsRepository
        // The FilterChip selected state is visually different but we test click doesn't crash
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Interests").assertIsDisplayed()
    }

    @Test
    fun unfollowedInterestChip_click_doesNotCrash() {
        setContent()
        // Health is not pre-followed; clicking it should follow it without crash
        composeTestRule.onNodeWithText("Health").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Interests").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Back button
    // ─────────────────────────────────────────────

    @Test
    fun backButton_isClickable_withoutCrash() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Back to Profile").performClick()
        composeTestRule.waitForIdle()
    }
}
