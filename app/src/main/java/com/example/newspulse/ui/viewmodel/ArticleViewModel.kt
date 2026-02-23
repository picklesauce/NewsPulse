package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ArticleViewModel(private val model: NewsPulseModel) : ViewModel() {
    private val _articles = MutableStateFlow(emptyList<Article>())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    init {
        updateArticles()
    }

    fun updateArticles() {
        val all = model.getFeed()
        val interests = model.getFollowedInterestNames()
        val filtered = all.filter { it.matchesInterests(interests) }
        _articles.value = filtered
    }

    fun unfollowInterest(name: String) {
        val id = model.getAllInterests().find { it.name == name }?.id ?: return
        model.unfollowInterest(id)
        updateArticles()
    }

    val allArticles: List<Article> get() = model.getFeed()
    val selectedInterests: Set<String> get() = model.getFollowedInterestNames()
}
