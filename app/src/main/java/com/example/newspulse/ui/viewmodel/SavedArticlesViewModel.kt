package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedArticlesViewModel : ViewModel() {
    private val _savedArticles = MutableStateFlow<List<Article>>(emptyList())
    val savedArticles: StateFlow<List<Article>> = _savedArticles.asStateFlow()

    fun saveArticle(article: Article) {
        viewModelScope.launch {
            val current = _savedArticles.value.toMutableList()
            if (!current.any { it.title == article.title }) {
                current.add(article)
                _savedArticles.value = current
            }
        }
    }

    fun removeArticle(article: Article) {
        viewModelScope.launch {
            val current = _savedArticles.value.toMutableList()
            current.removeAll { it.title == article.title }
            _savedArticles.value = current
        }
    }
}
