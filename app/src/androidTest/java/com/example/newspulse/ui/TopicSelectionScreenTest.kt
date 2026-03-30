package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.TopicSelectionScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Topic Selection (onboarding) screen.
 */
class TopicSelectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        model: com.example.newspulse.domain.NewsPulseModel = createTestModelNotOnboarded(),
    ) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    TopicSelectionScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Header / instructions
    // ─────────────────────────────────────────────

    @Test
    fun appName_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("NewsPulse").assertIsDisplayed()
    }

    @Test
    fun instructionText_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText("Select topics you're interested in, or search and add your own")
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
        composeTestRule.onNodeWithText("Search or add interests").performTextInput("Tech")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
    }

    @Test
    fun searchField_unknownQuery_showsAddChip() {
        setContent()
        composeTestRule.onNodeWithText("Search or add interests").performTextInput("Quantum Physics")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add \"Quantum Physics\"", substring = true).assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Interest chips
    // ─────────────────────────────────────────────

    @Test
    fun defaultInterestChips_areDisplayed() {
        setContent()
        // MockInterestsCatalogRepository supplies MockDB.interests
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
        composeTestRule.onNodeWithText("Business").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sports").assertIsDisplayed()
    }

    @Test
    fun selectingChip_updatesSelectionCount_inContinueButton() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Continue (1 selected)").assertIsDisplayed()
    }

    @Test
    fun selectingMultipleChips_updatesSelectionCount() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.onNodeWithText("Business").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Continue (2 selected)").assertIsDisplayed()
    }

    @Test
    fun deselectingChip_decrementsCount() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.onNodeWithText("Business").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Continue (1 selected)").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Continue button
    // ─────────────────────────────────────────────

    @Test
    fun continueButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Continue (0 selected)").assertIsDisplayed()
    }

    @Test
    fun continueButton_isDisabled_whenNothingSelected() {
        setContent()
        composeTestRule.onNodeWithText("Continue (0 selected)").assertIsNotEnabled()
    }

    @Test
    fun continueButton_isEnabled_afterSelectingOneInterest() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Continue (1 selected)").assertIsEnabled()
    }

    @Test
    fun continueButton_isEnabled_whenOneInterestSelected() {
        setContent()
        composeTestRule.onNodeWithText("Technology").performClick()
        composeTestRule.waitForIdle()
        // Don't click Continue — it calls navController.navigate() without a NavHost
        composeTestRule.onNodeWithText("Continue (1 selected)").assertIsEnabled()
    }

    // ─────────────────────────────────────────────
    // Country / Person / Company interests
    // ─────────────────────────────────────────────

    @Test
    fun countryInterest_isDisplayedInList() {
        setContent()
        composeTestRule.onNodeWithText("USA").assertIsDisplayed()
    }

    @Test
    fun personInterest_isDisplayedInList() {
        setContent()
        composeTestRule.onNodeWithText("Elon Musk").assertIsDisplayed()
    }

    @Test
    fun companyInterest_isDisplayedInList() {
        setContent()
        composeTestRule.onNodeWithText("Apple").assertIsDisplayed()
    }
}
