package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface SavedArticlesRepository {
    fun getSavedArticles(): Flow<List<Article>>
    fun getSavedArticlesList(): List<Article> = emptyList()
    fun saveArticle(article: Article)
    fun removeArticle(article: Article)
    fun onUserChanged() {}
    fun clearState() {}
}
