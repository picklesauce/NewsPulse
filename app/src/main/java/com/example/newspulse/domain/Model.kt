package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.ReadingHistoryItem
import kotlinx.coroutines.flow.Flow

class Model(
    private val newsRepository: NewsRepository,
    private val interestsRepository: InterestsRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val savedArticlesRepository: SavedArticlesRepository,
    private val topicsCatalogRepository: TopicsCatalogRepository
) {
    fun getArticles(): List<Article> = newsRepository.getArticles()
    fun getArticleById(id: String): Article? = newsRepository.getArticles().find { it.id == id }
    fun getArticleByTitleOrId(key: String): Article? =
        getArticleById(key) ?: newsRepository.getArticles().find { it.title == key }
    fun getSelectedInterests(): Set<String> = interestsRepository.getSelectedInterests()
    fun setSelectedInterests(interests: Set<String>) {
        interestsRepository.setSelectedInterests(interests)
    }
    fun isOnboardingComplete(): Boolean = interestsRepository.isOnboardingComplete()
    fun setOnboardingComplete() {
        interestsRepository.setOnboardingComplete()
    }
    fun getUsername(): String = userPreferencesRepository.getUsername()
    fun setUsername(username: String) {
        userPreferencesRepository.setUsername(username)
    }
    fun getMemberSince(): String = userPreferencesRepository.getMemberSince()
    fun setMemberSinceIfFirstTime() {
        userPreferencesRepository.setMemberSinceIfFirstTime()
    }
    fun getReadingHistory(): List<ReadingHistoryItem> = readingHistoryRepository.getReadingHistory()
    fun addToReadingHistory(articleId: String, title: String) {
        readingHistoryRepository.addToHistory(articleId, title)
    }
    fun getSavedArticles(): Flow<List<Article>> = savedArticlesRepository.getSavedArticles()
    fun saveArticle(article: Article) {
        savedArticlesRepository.saveArticle(article)
    }
    fun removeArticle(article: Article) {
        savedArticlesRepository.removeArticle(article)
    }
    fun getAvailableTopics(): List<String> = topicsCatalogRepository.getAvailableTopics()
}
