package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface SavedArticlesRepository {
    fun getSavedArticles(): Flow<List<Article>>
    fun saveArticle(article: Article)
    fun removeArticle(article: Article)
}
