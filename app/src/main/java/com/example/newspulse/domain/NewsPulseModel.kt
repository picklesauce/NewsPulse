package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.ReadingHistoryItem
import com.example.newspulse.domain.model.UserProfile
import com.example.newspulse.domain.util.scoreRelatedArticles
import kotlinx.coroutines.flow.Flow

class NewsPulseModel(
    private val newsRepository: NewsRepository,
    private val interestsRepository: InterestsRepository,
    private val interestsCatalogRepository: InterestsCatalogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val readingHistoryRepository: ReadingHistoryRepository,
    private val savedArticlesRepository: SavedArticlesRepository
) {
    fun getFeed(): List<Article> = newsRepository.getArticles()

    fun getArticle(articleId: String): Article? =
        newsRepository.getArticles().find { it.id == articleId }
            ?: newsRepository.getArticles().find { it.title == articleId }

    fun getAllInterests(): List<Interest> =
        interestsCatalogRepository.getAllInterests()

    fun followInterest(id: String) {
        interestsRepository.followInterest(id)
    }

    fun unfollowInterest(id: String) {
        interestsRepository.unfollowInterest(id)
    }

    fun getFollowedInterests(): List<Interest> {
        val ids = interestsRepository.getFollowedInterestIds()
        return interestsCatalogRepository.getAllInterests().filter { it.id in ids }
    }

    fun getFollowedInterestNames(): Set<String> = getFollowedInterests().map { it.name }.toSet()

    fun setFollowedInterestIds(ids: Set<String>) {
        interestsRepository.setFollowedInterestIds(ids)
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

    fun getRelatedArticles(articleId: String): List<Article> {
        val article = getArticle(articleId) ?: return emptyList()
        return scoreRelatedArticles(article, getFeed())
    }

    fun searchArticles(query: String): List<Article> =
        getFeed().filter { it.matches(query) }

    fun getUserProfile(): UserProfile = UserProfile(
        username = getUsername(),
        memberSince = getMemberSince(),
        selectedInterests = getFollowedInterests()
    )
}
