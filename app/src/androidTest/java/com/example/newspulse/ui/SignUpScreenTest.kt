package com.example.newspulse.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.newspulse.ui.theme.NewsPulseTheme
import com.example.newspulse.ui.view.SignUpScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the Sign-Up screen.
 */
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent() {
        val factory = ViewModelFactory(createTestModel())
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(CompositionLocals.LocalViewModelFactory provides factory) {
                    SignUpScreen(navController = rememberNavController())
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
    fun personalizedNewsSubtitle_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Your personalized news experience").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Form
    // ─────────────────────────────────────────────

    @Test
    fun createAccountTitle_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun signUpToGetStarted_subtitle_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Sign up to get started").assertIsDisplayed()
    }

    @Test
    fun usernameLabel_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Username").assertIsDisplayed()
    }

    @Test
    fun emailLabel_isDisplayed() {
        setContent()
        // Actual label is "Email Address" (not just "Email")
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
    }

    @Test
    fun passwordLabel_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
    }

    @Test
    fun confirmPasswordLabel_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
    }

    @Test
    fun usernameField_acceptsInput() {
        setContent()
        composeTestRule.onNodeWithText("Choose a username").performTextInput("johndoe")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("johndoe").assertIsDisplayed()
    }

    @Test
    fun emailField_acceptsInput() {
        setContent()
        composeTestRule.onNodeWithText("you@example.com").performTextInput("john@example.com")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("john@example.com").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────
    // Create account button
    // ─────────────────────────────────────────────

    @Test
    fun createAccountButton_isDisplayed() {
        setContent()
        composeTestRule.onNodeWithText("Create Account", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun createAccountButton_isEnabled() {
        setContent()
        // Don't click — would navigate and crash without NavHost
        composeTestRule.onNodeWithText("Create Account", useUnmergedTree = true).assertIsEnabled()
    }

    // ─────────────────────────────────────────────
    // Login navigation link
    // ─────────────────────────────────────────────

    @Test
    fun loginLink_isDisplayed() {
        setContent()
        // The sign-up screen has a button that says "Log In" to go back to login
        composeTestRule.onNodeWithText("Log In", substring = true).assertIsDisplayed()
    }

    @Test
    fun loginLink_isEnabled() {
        setContent()
        // Don't click — navigates without NavHost
        composeTestRule.onNodeWithText("Log In", substring = true).assertIsEnabled()
    }
}
