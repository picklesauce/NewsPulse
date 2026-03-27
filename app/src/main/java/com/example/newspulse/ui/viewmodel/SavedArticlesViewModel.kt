package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SavedArticlesViewModel(private val model: NewsPulseModel) : ViewModel() {

    init {
        model.refreshSavedArticles()
    }

    val savedArticles: StateFlow<List<Article>> = model.getSavedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeArticle(article: Article) {
        model.removeArticle(article)
    }
}
