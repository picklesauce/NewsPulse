package com.example.newspulse.data.mock

import com.example.newspulse.domain.SavedArticlesRepository
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemorySavedArticlesRepository : SavedArticlesRepository {
    private val _savedArticles = MutableStateFlow<List<Article>>(emptyList())
    override fun getSavedArticles(): Flow<List<Article>> = _savedArticles.asStateFlow()

    override fun saveArticle(article: Article) {
        val current = _savedArticles.value.toMutableList()
        if (!current.any { it.title == article.title }) {
            current.add(article)
            _savedArticles.value = current
        }
    }

    override fun removeArticle(article: Article) {
        val current = _savedArticles.value.toMutableList()
        current.removeAll { it.title == article.title }
        _savedArticles.value = current
    }
}
