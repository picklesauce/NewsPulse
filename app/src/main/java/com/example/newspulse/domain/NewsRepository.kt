package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article

interface NewsRepository {
    fun getArticles(): List<Article>

    /** Fetches latest articles, using cache when available (no-op for mocks). */
    suspend fun refresh() {}

    /** Fetches latest articles, bypassing any cache TTL (no-op for mocks). */
    suspend fun forceRefresh() { refresh() }

    /** Searches for articles matching [keyword], independent of followed interests. */
    suspend fun searchByKeyword(keyword: String): List<Article> = emptyList()
}
