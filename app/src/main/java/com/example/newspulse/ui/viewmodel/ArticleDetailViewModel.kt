package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Loads a single article and related articles (by shared interests + title keywords).
 * Opens from feed tap using articleId.
 */
class ArticleDetailViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _relatedArticles = MutableStateFlow<List<Article>>(emptyList())
    val relatedArticles: StateFlow<List<Article>> = _relatedArticles.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isLoadingRelated = MutableStateFlow(false)
    val isLoadingRelated: StateFlow<Boolean> = _isLoadingRelated.asStateFlow()

    /**
     * Loads the article and its related articles by id. Runs in a coroutine so that
     * if the feed is empty it triggers a refresh first, ensuring related articles
     * are computed against actual content.
     */
    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            var a = model.getArticle(articleId)
            if (a == null) {
                model.refreshSavedArticles()
                a = model.getArticle(articleId)
            }
            _article.value = a
            _isSaved.value = model.isArticleSaved(articleId)
            a?.let { model.addToReadingHistory(it.id, it.title) }

            if (a == null) return@launch

            // If feed is empty, refresh it so we have candidates for related articles.
            _isLoadingRelated.value = true
            if (model.getFeed().isEmpty()) {
                runCatching { model.refreshNews() }
            }
            _relatedArticles.value = model.getRelatedArticles(a.id)
            _isLoadingRelated.value = false
        }
    }

    fun addToReadingHistory(articleId: String, title: String) {
        model.addToReadingHistory(articleId, title)
    }

    fun saveArticle(article: Article) {
        model.saveArticle(article)
        _isSaved.value = true
    }
}
