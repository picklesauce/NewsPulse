package com.example.newspulse.domain.store

import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SavedArticlesStore {
    private val _savedArticles = MutableStateFlow<List<Article>>(emptyList())
    val savedArticles: StateFlow<List<Article>> = _savedArticles.asStateFlow()

    fun saveArticle(article: Article) {
        val current = _savedArticles.value.toMutableList()
        if (!current.any { it.title == article.title }) {
            current.add(article)
            _savedArticles.value = current
        }
    }

    fun removeArticle(article: Article) {
        val current = _savedArticles.value.toMutableList()
        current.removeAll { it.title == article.title }
        _savedArticles.value = current
    }
}
