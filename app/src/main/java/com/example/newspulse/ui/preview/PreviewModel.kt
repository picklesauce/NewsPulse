package com.example.newspulse.ui.preview

import com.example.newspulse.data.mock.FakeInterestsRepository
import com.example.newspulse.data.mock.FakeNewsRepository
import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeSavedArticlesRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.data.mock.MockInterestsCatalogRepository
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.ui.ViewModelFactory

fun createPreviewModel(): NewsPulseModel = NewsPulseModel(
    newsRepository = FakeNewsRepository(),
    interestsRepository = FakeInterestsRepository(),
    interestsCatalogRepository = MockInterestsCatalogRepository(),
    userPreferencesRepository = FakeUserPreferencesRepository(),
    readingHistoryRepository = FakeReadingHistoryRepository(),
    savedArticlesRepository = FakeSavedArticlesRepository()
)

fun createPreviewViewModelFactory(): ViewModelFactory = ViewModelFactory(createPreviewModel())
