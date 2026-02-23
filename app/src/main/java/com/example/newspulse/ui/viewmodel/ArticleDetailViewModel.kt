package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article

class ArticleDetailViewModel(private val model: NewsPulseModel) : ViewModel() {
    fun getArticleById(id: String): Article? = model.getArticle(id)

    fun addToReadingHistory(articleId: String, title: String) {
        model.addToReadingHistory(articleId, title)
    }

    fun saveArticle(article: Article) {
        model.saveArticle(article)
    }
}
