package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.Model
import com.example.newspulse.domain.model.Article

class ArticleDetailViewModel(private val model: Model) : ViewModel() {
    fun getArticleByTitle(title: String): Article? =
        model.getArticles().find { it.title == title }

    fun addToReadingHistory(title: String) {
        model.addToReadingHistory(title)
    }

    fun saveArticle(article: Article) {
        model.saveArticle(article)
    }
}
