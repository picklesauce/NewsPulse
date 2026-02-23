package com.example.newspulse.domain.model

data class ArticleList(
    val articles: List<Article>,
    val totalCount: Int? = null
)
