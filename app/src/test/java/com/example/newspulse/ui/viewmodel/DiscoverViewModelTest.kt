package com.example.newspulse.ui.viewmodel

import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.data.mock.MockInterestsRepository
import com.example.newspulse.data.mock.MockNewsRepository
import com.example.newspulse.domain.NewsPulseModel
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
    fun onSelectCategory_setsSelectedInterest() {
        val catalogTech = model.getAllInterests().find { it.name == "Technology" }!!
        model.followInterest(catalogTech.id)

        val interest = model.interestForDiscoverCategory("Technology", InterestType.Topic)
        val followed = model.getFollowedInterestIds().contains(interest.id)

        assertTrue("Technology should be followed", followed)
        assertEquals(catalogTech.id, interest.id)
    }

    @Test
    fun followDiscoverInterest_addsToFollowedIds() {
        val interest = model.interestForDiscoverCategory("Finance", InterestType.Topic)
        assertTrue(!model.getFollowedInterestIds().contains(interest.id))

        model.addCustomInterest("Finance", InterestType.Topic)

        assertTrue(model.getFollowedInterestIds().contains(interest.id))
    }
}
