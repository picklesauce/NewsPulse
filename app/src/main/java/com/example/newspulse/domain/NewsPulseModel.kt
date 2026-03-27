package com.example.newspulse.domain

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
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
    private val discoverCache = mutableMapOf<String, Article>()

    fun getFeed(): List<Article> = newsRepository.getArticles()

    /** Fetches latest articles, using disk cache when fresh. */
    suspend fun refreshNews() {
        newsRepository.refresh()
    }

    /** Fetches latest articles, bypassing cache TTL (for pull-to-refresh). */
    suspend fun forceRefreshNews() {
        newsRepository.forceRefresh()
    }

    /**
     * Returns a single article by its stable [articleId].
     * Checks the live feed first, then falls back to saved articles so that
     * previously-saved articles remain accessible even after interest changes.
     */
    fun getArticle(articleId: String): Article? =
        newsRepository.getArticles().find { it.id == articleId }
            ?: savedArticlesRepository.getSavedArticlesList().find { it.id == articleId }
            ?: discoverCache[articleId]

    fun getAllInterests(): List<Interest> =
        interestsCatalogRepository.getAllInterests()

    /** Creates a custom interest, adds it to the catalog, and auto-follows it. */
    fun addCustomInterest(name: String, type: InterestType): Interest {
        val interest = interestsCatalogRepository.addCustomInterest(name, type)
        interestsRepository.followInterest(interest.id)
        return interest
    }

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

    fun logIn(email: String, password: String): AuthResult {
        val result = authRepository?.signIn(email, password) ?: run {
            val ok = userPreferencesRepository.getStoredEmail() == email &&
                userPreferencesRepository.getStoredPassword() == password
            if (ok) AuthResult(true) else AuthResult(false, "Invalid email or password")
        }
        if (result.success) onUserLoggedIn()
        return result
    }

    fun signUp(email: String, password: String): AuthResult {
        val result = authRepository?.signUp(email, password) ?: run {
            userPreferencesRepository.setStoredCredentials(email, password)
            AuthResult(true)
        }
        if (result.success) onUserLoggedIn()
        return result
    }

    fun signOut() {
        authRepository?.signOut()
        savedArticlesRepository.clearState()
    }

    private fun onUserLoggedIn() {
        interestsRepository.onUserChanged()
        savedArticlesRepository.onUserChanged()
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
    fun refreshSavedArticles() { savedArticlesRepository.onUserChanged() }
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

    suspend fun searchArticlesByKeyword(keyword: String): List<Article> {
        val results = newsRepository.searchByKeyword(keyword)
        results.forEach { discoverCache[it.id] = it }
        return results
    }

    fun getUserProfile(): UserProfile = UserProfile(
        username = getUsername(),
        memberSince = getMemberSince(),
        selectedInterests = getFollowedInterests()
    )
}
