package com.example.newspulse.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.ui.CompositionLocals
import com.example.newspulse.ui.ViewModelFactory
import com.example.newspulse.ui.theme.NewsPulseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



// discover screen UI tests, grid, search, catagory, etc
// shouldnt need data db
@RunWith(AndroidJUnit4::class)
class DiscoverScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createDiscoverViewModelFactory(): ViewModelFactory {
        val model = NewsPulseModel(
            newsRepository = MockNewsRepository(),
            interestsRepository = MockInterestsRepository(),
            interestsCatalogRepository = MockInterestsCatalogRepository(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
            readingHistoryRepository = FakeReadingHistoryRepository(),
            savedArticlesRepository = InMemorySavedArticlesRepository()
        )
        return ViewModelFactory(model)
    }

    private fun setExploreScreenContent() {
        composeTestRule.setContent {
            NewsPulseTheme {
                CompositionLocalProvider(
                    CompositionLocals.LocalViewModelFactory provides createDiscoverViewModelFactory()
                ) {
                    ExploreScreen(navController = rememberNavController())
                }
            }
        }
    }

    private fun ComposeContentTestRule.waitUntilAssert(timeoutMillis: Long, assertion: () -> Unit) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        var last: AssertionError? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                assertion()
                return
            } catch (e: AssertionError) {
                last = e
                Thread.sleep(50)
            }
        }
        try {
            assertion()
        } catch (e: AssertionError) {
            throw last ?: e
        }
    }

    @Test
    fun discoverBrowse_showsTitleSearchAndCategoryCards() {
        setExploreScreenContent()

        composeTestRule.onNodeWithText("Discover Topics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search topics...", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
        composeTestRule.onNodeWithText("Finance").assertIsDisplayed()
    }

    @Test
    fun discoverBrowse_searchFiltersCategories() {
        setExploreScreenContent()

        composeTestRule.onNode(hasSetTextAction()).performTextInput("Finance")

        composeTestRule.onNodeWithText("Finance").assertIsDisplayed()
        var technologyHidden = false
        try {
            composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
        } catch (_: AssertionError) {
            technologyHidden = true
        }
        assertTrue("Filtered grid should not show Technology", technologyHidden)
    }

    @Test
    fun discoverBrowse_clickCategory_showsDetailWithFollowAndBack() {
        setExploreScreenContent()

        composeTestRule.onNodeWithText("Science").performClick()

        composeTestRule.waitUntilAssert(15_000) {
            composeTestRule.onNodeWithText("Follow", useUnmergedTree = true).assertIsDisplayed()
        }

        composeTestRule.onNodeWithText("Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("Follow", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back to Discover").assertIsDisplayed()

        composeTestRule.waitUntilAssert(15_000) {
            composeTestRule.onNodeWithText("No articles available for Science", substring = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun discoverDetail_back_returnsToBrowseGrid() {
        setExploreScreenContent()

        composeTestRule.onNodeWithText("Politics").performClick()

        composeTestRule.waitUntilAssert(15_000) {
            composeTestRule.onNodeWithContentDescription("Back to Discover").assertIsDisplayed()
        }

        composeTestRule.onNodeWithContentDescription("Back to Discover").performClick()

        composeTestRule.waitUntilAssert(15_000) {
            composeTestRule.onNodeWithText("Discover Topics").assertIsDisplayed()
        }
        composeTestRule.onNodeWithText("Technology").assertIsDisplayed()
    }
}
