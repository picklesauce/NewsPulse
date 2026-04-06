package com.example.newspulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newspulse.domain.NewsPulseModel
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


//Loads a single article and related articles (by shared interests).
//Opens from feed tap using articleId.

class ArticleDetailViewModel(private val model: NewsPulseModel) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _relatedArticles = MutableStateFlow<List<Article>>(emptyList())
    val relatedArticles: StateFlow<List<Article>> = _relatedArticles.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    
// Loads the article and its related articles by id. Call from the screen when articleId is available
// Also adds the article to reading history.
    fun loadArticle(articleId: String) {
        viewModelScope.launch {
            var a = model.getArticle(articleId)
            if (a == null) {
                model.refreshSavedArticles()
                a = model.getArticle(articleId)
            }
            _article.value = a
            _relatedArticles.value = a?.let { model.getRelatedArticles(it.id) } ?: emptyList()
            _isSaved.value = model.isArticleSaved(articleId)
            a?.let { model.addToReadingHistory(it.id, it.title) }
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
