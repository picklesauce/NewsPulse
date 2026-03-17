package com.example.newspulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.ui.viewmodel.ArticleDetailViewModel
import com.example.newspulse.ui.viewmodel.DiscoverViewModel
import com.example.newspulse.ui.viewmodel.FeedViewModel
import com.example.newspulse.ui.viewmodel.FiltersViewModel
import com.example.newspulse.ui.viewmodel.InterestsViewModel
import com.example.newspulse.ui.viewmodel.LoginViewModel
import com.example.newspulse.ui.viewmodel.ProfileViewModel
import com.example.newspulse.ui.viewmodel.SavedArticlesViewModel
import com.example.newspulse.ui.viewmodel.TopicSelectionViewModel

class ViewModelFactory(private val model: NewsPulseModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(FeedViewModel::class.java) -> FeedViewModel(model) as T
            modelClass.isAssignableFrom(ArticleDetailViewModel::class.java) -> ArticleDetailViewModel(model) as T
            modelClass.isAssignableFrom(DiscoverViewModel::class.java) -> DiscoverViewModel(model) as T
            modelClass.isAssignableFrom(TopicSelectionViewModel::class.java) -> TopicSelectionViewModel(model) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(model) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(model) as T
            modelClass.isAssignableFrom(FiltersViewModel::class.java) -> FiltersViewModel(model) as T
            modelClass.isAssignableFrom(InterestsViewModel::class.java) -> InterestsViewModel(model) as T
            modelClass.isAssignableFrom(SavedArticlesViewModel::class.java) -> SavedArticlesViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
