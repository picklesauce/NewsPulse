package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.LoginScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Login screen.
 */
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent() {
        val factory = ViewModelFactory(createTestModel())
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    LoginScreen(navController = rememberNavController())
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // ─────────────────────────────────────────────
    // Branding
    // ─────────────────────────────────────────────

    @Test
    fun appName_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("NewsPulse").assertIsDisplayed()
    }

    @Test
    fun welcomeSubtitle_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Welcome back!").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Form labels and fields
    // ─────────────────────────────────────────────

    @Test
    fun logInCardSubtitle_isDisplayed() {
        setContent()
        // Unique text in the card that doesn't appear elsewhere
        composeTestRule.onNodeWithText("Enter your credentials to continue").assertIsDisplayed()
    }

    @Test
    fun emailLabel_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Email/Username").assertIsDisplayed()
    }

    @Test
    fun passwordLabel_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun emailField_acceptsInput() {
        setContent()
        composeTestRule.onNodeWithText("you@example.com or yourname").performTextInput("test@example.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun forgotPasswordLink_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Forgot?").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Buttons
    // ─────────────────────────────────────────────

    @Test
    fun continueWithGoogle_button_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Continue with Google").assertIsDisplayed()
    }

    @Test
    fun loginButton_isDisplayed() {
        setContent()
        // "Log In" appears twice (card section title + button); use index [0]
        composeTestRule.onAllNodesWithText("Log In")[0].assertIsDisplayed()
    }

    @Test
    fun loginButton_isEnabled() {
        setContent()
        // Don't click — clicking navigates and crashes without a NavHost
        composeTestRule.onAllNodesWithText("Log In")[0].assertIsEnabled()
    }

    @Test
    fun encryptionNotice_isDisplayed() {
        setContent()
        composeTestRule
            .onNodeWithText("Protected by industry-standard encryption")
            .assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Sign-up link
    // ─────────────────────────────────────────────

    @Test
    fun signUpButton_isDisplayed() {
        setContent()
        // Actual text is "Sign up" (lowercase 'u')
        composeTestRule.onNodeWithText("Sign up").assertIsDisplayed()
    }

    @Test
    fun signUpButton_isEnabled() {
        setContent()
        // Don't click — clicking navigates and crashes without a NavHost
        composeTestRule.onNodeWithText("Sign up").assertIsEnabled()
    }
}
