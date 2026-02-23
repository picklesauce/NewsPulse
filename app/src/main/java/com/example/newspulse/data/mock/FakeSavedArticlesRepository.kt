package com.example.newspulse.data.mock

import com.example.newspulse.domain.SavedArticlesRepository
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSavedArticlesRepository : SavedArticlesRepository {
    override fun getSavedArticles(): Flow<List<Article>> = flowOf(emptyList())
    override fun saveArticle(article: Article) {}
    override fun removeArticle(article: Article) {}
}
