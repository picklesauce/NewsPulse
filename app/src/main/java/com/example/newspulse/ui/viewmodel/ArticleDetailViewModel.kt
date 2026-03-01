package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Loads a single article and related articles (by shared interests via domain helper).
 * Opens from feed tap using articleId.
 */
class ArticleDetailViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    /** The article being viewed. Loaded when [loadArticle] is called with a valid id. */
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _relatedArticles = MutableStateFlow<List<Article>>(emptyList())
    /** Related articles based on shared interests (and same source), sorted by relevance. */
    val relatedArticles: StateFlow<List<Article>> = _relatedArticles.asStateFlow()

    /**
     * Loads the article and its related articles by id. Call from the screen when articleId is available
     * (e.g. from route articleDetail/{id}). Also adds the article to reading history.
     */
    fun loadArticle(articleId: String) {
        val a = model.getArticle(articleId)
        _article.value = a
        _relatedArticles.value = a?.let { model.getRelatedArticles(it.id) } ?: emptyList()
        a?.let { model.addToReadingHistory(it.id, it.title) }
    }

    fun addToReadingHistory(articleId: String, title: String) {
        model.addToReadingHistory(articleId, title)
    }

    fun saveArticle(article: Article) {
        model.saveArticle(article)
    }
}
