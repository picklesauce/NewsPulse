package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.InMemorySavedArticlesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var model: NewsPulseModel
    private lateinit var viewModel: DiscoverViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        model = NewsPulseModel(
            newsRepository = MockNewsRepository(),
            interestsRepository = MockInterestsRepository(),
            interestsCatalogRepository = MockInterestsCatalogRepository(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
            readingHistoryRepository = FakeReadingHistoryRepository(),
            savedArticlesRepository = InMemorySavedArticlesRepository()
        )
        viewModel = DiscoverViewModel(model)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onSelectInterest_syntheticId_showsFollowedWhenCatalogIdFollowed() {
        val catalogTech = model.getAllInterests().find { it.name == "Technology" }!!
        model.followInterest(catalogTech.id)

        val synthetic = Interest(
            id = "interest-technology",
            type = InterestType.Topic,
            name = "Technology"
        )
        viewModel.onSelectInterest(synthetic)

        val state = viewModel.uiState.value
        assertTrue(state.isFollowed)
        assertEquals(catalogTech.id, state.selectedInterest?.id)
    }

    @Test
    fun onFollowTopic_updatesUiToFollowed() {
        val synthetic = Interest(
            id = "interest-finance",
            type = InterestType.Topic,
            name = "Finance"
        )
        viewModel.onSelectInterest(synthetic)
        assertTrue(!viewModel.uiState.value.isFollowed)

        viewModel.onFollowTopic()

        val after = viewModel.uiState.value
        assertTrue(after.isFollowed)
        assertNotNull(after.selectedInterest)
        assertTrue(model.getFollowedInterestIds().contains(after.selectedInterest!!.id))
    }
}
