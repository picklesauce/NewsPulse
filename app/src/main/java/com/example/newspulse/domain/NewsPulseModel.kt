package com.example.newspulse.domain

import com.example.newspulse.data.SupabaseInterestsRepository
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.model.ReadingHistoryItem
import com.example.newspulse.domain.model.UserProfile
import com.example.newspulse.domain.util.ArticleDeduplicator
import com.example.newspulse.domain.util.DiscoverCategoryRelevance
import com.example.newspulse.domain.util.InterestSlug
import com.example.newspulse.domain.util.RelatedArticlesLlmRanker
import com.example.newspulse.domain.util.scoreRelatedArticles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    /**
     * Interest for a Discover grid category: uses the same id as [addCustomInterestPersisted]
     * will create (catalog UUID/slug match), so follow state and Supabase rows stay aligned.
     */
    fun interestForDiscoverCategory(name: String, type: InterestType): Interest {
        val existing = getAllInterests().find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing
        return Interest(
            id = InterestSlug.stableIdForName(name),
            type = type,
            name = name
        )
    }

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

    fun getCloudUserId(): String? = authRepository?.getCurrentUserId()

    fun shouldShowSignInForFollowFailure(): Boolean =
        interestsRepository.needsAuthenticatedUserForWrite() && getCloudUserId() == null

    fun setFollowedInterestIds(ids: Set<String>) {
        interestsRepository.setFollowedInterestIds(ids)
    }

    suspend fun followInterestSuspend(id: String): Boolean =
        interestsRepository.followInterestSuspend(id)

    suspend fun unfollowInterestSuspend(id: String): Boolean =
        interestsRepository.unfollowInterestSuspend(id)

    suspend fun setFollowedInterestIdsSuspend(ids: Set<String>): Boolean =
        interestsRepository.setFollowedInterestIdsSuspend(ids)

    suspend fun setOnboardingCompleteSuspend(): Boolean =
        interestsRepository.setOnboardingCompleteSuspend()

    /** Persist new catalog row (when needed) then follow; used for Interests / topic flows. */
    suspend fun addCustomInterestPersisted(name: String, type: InterestType): Boolean {
        val interest = interestsCatalogRepository.addCustomInterestPersisted(name, type)
        val followed = followInterestSuspend(interest.id)
        if (!followed) {
            interestsRepository.followInterest(interest.id)
        }
        return true
    }

    /** Discover: ensure catalog + follow row exist in order (avoids Supabase FK / race issues). */
    suspend fun followDiscoverInterest(interest: Interest): Boolean {
        if (interestsRepository.needsAuthenticatedUserForWrite() && getCloudUserId() == null) {
            return false
        }
        return withContext(Dispatchers.IO) {
            val resolved = interestsCatalogRepository.addCustomInterestPersisted(interest.name, interest.type)
            followInterestSuspend(resolved.id)
        }
    }

    /** Discover: remove follow row in Postgres / local store. */
    suspend fun unfollowDiscoverInterest(interest: Interest): Boolean {
        if (interestsRepository.needsAuthenticatedUserForWrite() && getCloudUserId() == null) {
            return false
        }
        return withContext(Dispatchers.IO) {
            unfollowInterestSuspend(interest.id)
        }
    }

    fun isOnboardingComplete(): Boolean = interestsRepository.isOnboardingComplete()
    fun setOnboardingComplete() {
        interestsRepository.setOnboardingComplete()
    }

    fun getCurrentUserId(): String? = authRepository?.getCurrentUserId()

    fun getUsername(): String = userPreferencesRepository.getUsername()
    fun setUsername(username: String) {
        userPreferencesRepository.setUsername(username)
    }

    /** Re-load user profile fields from Supabase (used by Profile screen after OAuth). */
    suspend fun refreshProfileDisplayFromRemote() {
        userPreferencesRepository.refreshProfileFromRemote()
    }

    suspend fun signInWithGoogle(): AuthResult =
        authRepository?.let { withContext(Dispatchers.IO) { it.signInWithGoogle() } }
            ?: AuthResult(false, "Google sign-in is not available")

    fun observeSupabaseAuthUserId(): Flow<String?> =
        authRepository?.observeSupabaseAuthUserId() ?: emptyFlow()

    suspend fun syncSupabaseAuthSessionToApp(): Boolean {
        val ok = authRepository?.syncSupabaseAuthSessionToApp() == true
        if (ok) {
            pullRemoteUserStateAfterAuth()
            onUserLoggedIn()
        }
        return ok
    }

    private suspend fun pullRemoteUserStateAfterAuth() {
        userPreferencesRepository.clearCachedProfileForAccountSwitch()
        withContext(Dispatchers.IO) {
            (interestsRepository as? SupabaseInterestsRepository)?.awaitInitialSync()
            userPreferencesRepository.refreshProfileFromRemote()
        }
    }

    suspend fun logIn(email: String, password: String): AuthResult {
        val result = authRepository?.let { withContext(Dispatchers.IO) { it.signIn(email, password) } }
            ?: run {
                val ident = email.trim()
                val pwdOk = userPreferencesRepository.getStoredPassword() == password
                val emailOk = userPreferencesRepository.getStoredEmail().equals(ident, ignoreCase = true)
                val usernameOk =
                    userPreferencesRepository.getUsername().isNotBlank() &&
                        userPreferencesRepository.getUsername().equals(ident, ignoreCase = true)
                val ok = pwdOk && (emailOk || usernameOk)
                if (ok) AuthResult(true) else AuthResult(false, "Invalid email or password")
            }
        if (result.success) {
            if (authRepository != null) {
                pullRemoteUserStateAfterAuth()
            }
            onUserLoggedIn()
        }
        return result
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        val result = authRepository?.let { withContext(Dispatchers.IO) { it.signUp(email, password) } }
            ?: run {
                userPreferencesRepository.setStoredCredentials(email, password)
                AuthResult(true)
            }
        if (result.success) {
            if (authRepository != null) {
                pullRemoteUserStateAfterAuth()
            }
            onUserLoggedIn()
        }
        return result
    }

    fun signOut() {
        authRepository?.signOut()
        userPreferencesRepository.clearCachedProfileForAccountSwitch()
        interestsRepository.onUserChanged()
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
    fun isArticleSaved(articleId: String): Boolean =
        savedArticlesRepository.getSavedArticlesList().any { it.id == articleId }
    fun saveArticle(article: Article) {
        savedArticlesRepository.saveArticle(article)
    }
    fun removeArticle(article: Article) {
        savedArticlesRepository.removeArticle(article)
    }

    suspend fun getRelatedArticles(articleId: String): List<Article> {
        val article = getArticle(articleId) ?: return emptyList()
        val candidates = ArticleDeduplicator.dedupePreservingOrder(
            buildList {
                addAll(getFeed())
                addAll(savedArticlesRepository.getSavedArticlesList())
                addAll(discoverCache.values)
            }
        )

        val heuristicRanked = scoreRelatedArticles(article, candidates)
        val shortlist = heuristicRanked.take(15)
        return RelatedArticlesLlmRanker.pickTop3(baseArticle = article, shortlist = shortlist)
    }

    fun searchArticles(query: String): List<Article> =
        getFeed().filter { it.matches(query) }

    suspend fun searchArticlesByKeyword(keyword: String): List<Article> {
        val raw = ArticleDeduplicator.dedupePreservingOrder(newsRepository.searchByKeyword(keyword))
        val results = raw.filter { DiscoverCategoryRelevance.matchesDiscoverCategory(keyword, it) }
        results.forEach { discoverCache[it.id] = it }
        return results
    }

    fun getUserProfile(): UserProfile = UserProfile(
        username = getUsername(),
        memberSince = getMemberSince(),
        selectedInterests = getFollowedInterests()
    )
}
