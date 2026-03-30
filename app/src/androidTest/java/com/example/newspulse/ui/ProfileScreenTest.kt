package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.data.mock.MockDB
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.ProfileScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Profile screen.
 */
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        model: com.example.newspulse.domain.NewsPulseModel = createTestModel(),
    ) {
        val factory = ViewModelFactory(model)
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    ProfileScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Header / identity
    // ─────────────────────────────────────────────

    @Test
    fun profileHeader_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun username_isDisplayed() {
        setContent()
        // FakeUserPreferencesRepository returns "preview_user"
        composeTestRule.onNodeWithText("preview_user").assertIsDisplayed()
    }

    @Test
    fun memberSince_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Member since Feb 2026").assertIsDisplayed()
    }

    @Test
    fun profilePictureIcon_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Profile picture").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Edit username
    // ─────────────────────────────────────────────

    @Test
    fun editUsernameButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Edit username").assertIsDisplayed()
    }

    @Test
    fun editUsernameButton_click_opensDialog() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Edit username").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Username").assertIsDisplayed()
    }

    @Test
    fun editUsernameDialog_hasTextField_with_currentUsername() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Edit username").performClick()
        composeTestRule.waitForIdle()
        // Dialog opens with editingName = current username ("preview_user")
        composeTestRule.onNodeWithText("preview_user").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
    }

    @Test
    fun editUsernameDialog_saveButton_isDisabled_whenFieldCleared() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Edit username").performClick()
        composeTestRule.waitForIdle()
        // Clear the text field (it starts with current username "preview_user")
        composeTestRule.onNodeWithText("preview_user").performTextClearance()
        composeTestRule.waitForIdle()
        // Save should now be disabled because editingName is blank
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun editUsernameDialog_cancel_dismissesDialog() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Edit username").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Username").assertDoesNotExist()
    }

    @Test
    fun editUsernameDialog_save_updatesUsername() {
        setContent()
        composeTestRule.onNodeWithContentDescription("Edit username").performClick()
        composeTestRule.waitForIdle()
        // Clear the field that starts with "preview_user" then type a new name
        composeTestRule.onNodeWithText("preview_user").performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Enter new username").performTextInput("new_user")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
        // Dialog closed and new username visible
        composeTestRule.onNodeWithText("Edit Username").assertDoesNotExist()
        composeTestRule.onNodeWithText("new_user").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Interests section
    // ─────────────────────────────────────────────

    @Test
    fun interestsSectionHeader_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("YOUR INTERESTS & CATEGORIES").assertIsDisplayed()
    }

    @Test
    fun followedInterestChips_areDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
        composeTestRule.onNodeWithText("Business").assertIsDisplayed()
    }

    @Test
    fun interestTypeGroupHeader_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("TOPIC").assertIsDisplayed()
    }

    @Test
    fun addMoreInterestsButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("+ Add More Interests").assertIsDisplayed()
    }

    @Test
    fun addMoreInterestsButton_isEnabled() {
        setContent()
        // Don't click — navigates to "interests" route without a NavHost
        composeTestRule.onNodeWithText("+ Add More Interests").assertIsEnabled()
    }

    @Test
    fun emptyInterests_showsNoInterestsMessage() {
        val model = createTestModel(
            interestsRepository = object : com.example.newspulse.data.mock.FakeInterestsRepository() {
                override fun getFollowedInterestIds(): Set<String> = emptySet()
            }
        )
        setContent(model)
        composeTestRule.onNodeWithText("No interests selected yet").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Reading history
    // ─────────────────────────────────────────────

    @Test
    fun readingHistorySectionHeader_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("READING HISTORY").assertIsDisplayed()
    }

    @Test
    fun readingHistory_empty_showsNoArticlesMessage() {
        setContent()
        composeTestRule.onNodeWithText("No articles read yet").assertIsDisplayed()
    }

    @Test
    fun readingHistory_populated_showsArticleTitles() {
        setContent(createTestModelWithReadingHistory())
        composeTestRule.onNodeWithText(MockDB.articles[0].title).assertIsDisplayed()
        composeTestRule.onNodeWithText(MockDB.articles[1].title).assertIsDisplayed()
    }

    @Test
    fun readingHistory_seeAll_notShown_whenFiveOrFewer() {
        setContent(createTestModelWithReadingHistory())
        composeTestRule.onNodeWithText("See all").assertDoesNotExist()
    }

    // ─────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────

    @Test
    fun logoutButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
    }

    @Test
    fun logoutButton_isEnabled() {
        setContent()
        // Don't click — navigates to "login" without a NavHost
        composeTestRule.onNodeWithText("Logout").assertIsEnabled()
    }
}
