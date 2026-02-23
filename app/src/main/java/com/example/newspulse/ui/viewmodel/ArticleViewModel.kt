package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.Model
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ArticleViewModel(private val model: Model) : ViewModel() {
    private val _articles = MutableStateFlow(emptyList<Article>())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    init {
        updateArticles()
    }

    fun updateArticles() {
        val all = model.getArticles()
        val interests = model.getSelectedInterests()
        val filtered = if (interests.isEmpty()) {
            all
        } else {
            all.filter { article ->
                article.topics.any { it in interests }
            }
        }
        _articles.value = filtered
    }

    val allArticles: List<Article> get() = model.getArticles()
    val selectedInterests: Set<String> get() = model.getSelectedInterests()
}
