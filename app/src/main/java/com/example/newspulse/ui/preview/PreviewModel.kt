package com.example.newspulse.ui.preview

import com.example.newspulse.data.mock.FakeInterestsRepository
import com.example.newspulse.data.mock.FakeNewsRepository
import com.example.newspulse.data.mock.FakeReadingHistoryRepository
import com.example.newspulse.data.mock.FakeSavedArticlesRepository
import com.example.newspulse.data.mock.FakeTopicsCatalogRepository
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.domain.Model
import com.example.newspulse.ui.ViewModelFactory

fun createPreviewModel(): Model = Model(
    newsRepository = FakeNewsRepository(),
    interestsRepository = FakeInterestsRepository(),
    userPreferencesRepository = FakeUserPreferencesRepository(),
    readingHistoryRepository = FakeReadingHistoryRepository(),
    savedArticlesRepository = FakeSavedArticlesRepository(),
    topicsCatalogRepository = FakeTopicsCatalogRepository()
)

fun createPreviewViewModelFactory(): ViewModelFactory = ViewModelFactory(createPreviewModel())
