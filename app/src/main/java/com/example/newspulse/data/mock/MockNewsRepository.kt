package com.example.newspulse.data.mock

import com.example.newspulse.domain.NewsRepository
import com.example.newspulse.domain.model.Article

class MockNewsRepository : NewsRepository {
    override fun getArticles(): List<Article> = MockDB.articles
}
