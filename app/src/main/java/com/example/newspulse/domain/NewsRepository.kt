package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article

interface NewsRepository {
    fun getArticles(): List<Article>
}
