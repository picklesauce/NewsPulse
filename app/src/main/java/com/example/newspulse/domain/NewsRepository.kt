package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article

interface NewsRepository {
    fun getArticles(): List<Article>

    // fetch latest article
    suspend fun refresh() {}

    //Fetches latest articles, bypassing any cache
    suspend fun forceRefresh() { refresh() }

    //Searches for articles matching [keyword], regardless of followed interests
    suspend fun searchByKeyword(keyword: String): List<Article> = emptyList()
}
