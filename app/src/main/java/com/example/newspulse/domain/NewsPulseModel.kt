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
    private val savedArticlesRepository: SavedArticlesRepository,
    private val authRepository: AuthRepository? = null
) {
    fun getFeed(): List<Article> = newsRepository.getArticles()

    /** Fetches latest articles from the API; no-op if using mock repository. */
    suspend fun refreshNews() {
        newsRepository.refresh()
    }

    /**
     * Returns a single article by its stable [articleId].
     * Navigation and reading history are wired to use this ID, not the title.
     */
    fun getArticle(articleId: String): Article? =
        newsRepository.getArticles().find { it.id == articleId }

    fun getAllInterests(): List<Interest> =
        interestsCatalogRepository.getAllInterests()

    fun followInterest(id: String) {
        interestsRepository.followInterest(id)
    }

    fun unfollowInterest(id: String) {
        interestsRepository.unfollowInterest(id)
    }

    fun getFollowedInterestIds(): Set<String> = interestsRepository.getFollowedInterestIds()

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

    fun logIn(email: String, password: String): AuthResult =
        authRepository?.signIn(email, password) ?: run {
            val ok = userPreferencesRepository.getStoredEmail() == email &&
                userPreferencesRepository.getStoredPassword() == password
            if (ok) AuthResult(true) else AuthResult(false, "Invalid email or password")
        }

    fun signUp(email: String, password: String): AuthResult =
        authRepository?.signUp(email, password) ?: run {
            userPreferencesRepository.setStoredCredentials(email, password)
            AuthResult(true)
        }

    fun setStoredCredentials(email: String, password: String) {
        userPreferencesRepository.setStoredCredentials(email, password)
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
