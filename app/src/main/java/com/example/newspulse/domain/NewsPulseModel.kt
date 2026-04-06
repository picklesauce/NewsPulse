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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
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

    private val _feedRefetchRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val feedRefetchRequests: SharedFlow<Unit> = _feedRefetchRequests.asSharedFlow()

    fun markFeedShouldRefetch() {
        _feedRefetchRequests.tryEmit(Unit)
    }

    fun getFeed(): List<Article> =
        newsRepository.getArticles().filter { it.hasDisplayImage }

    // fetch new articles, if fresh, use whats there on cache
    suspend fun refreshNews() {
        newsRepository.refresh()
    }

    // fetches latest articles
    suspend fun forceRefreshNews() {
        newsRepository.forceRefresh()
    }

    // return article by id
    fun getArticle(articleId: String): Article? =
        newsRepository.getArticles().find { it.id == articleId }
            ?: savedArticlesRepository.getSavedArticlesList().find { it.id == articleId }
            ?: discoverCache[articleId]

    fun getAllInterests(): List<Interest> =
        interestsCatalogRepository.getAllInterests()


    fun interestForDiscoverCategory(name: String, type: InterestType): Interest {
        val existing = getAllInterests().find { it.name.equals(name, ignoreCase = true) }
        if (existing != null) return existing
        return Interest(
            id = InterestSlug.stableIdForName(name),
            type = type,
            name = name
        )
    }

    // create custom interest, add and follow
    fun addCustomInterest(name: String, type: InterestType): Interest {
        val interest = interestsCatalogRepository.addCustomInterest(name, type)
        followInterest(interest.id)
        return interest
    }

    fun followInterest(id: String) {
        interestsRepository.followInterest(id)
        markFeedShouldRefetch()
    }

    fun unfollowInterest(id: String) {
        interestsRepository.unfollowInterest(id)
        markFeedShouldRefetch()
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
        markFeedShouldRefetch()
    }

    suspend fun followInterestSuspend(id: String): Boolean {
        val ok = interestsRepository.followInterestSuspend(id)
        if (ok) markFeedShouldRefetch()
        return ok
    }

    suspend fun unfollowInterestSuspend(id: String): Boolean {
        val ok = interestsRepository.unfollowInterestSuspend(id)
        if (ok) markFeedShouldRefetch()
        return ok
    }

    suspend fun setFollowedInterestIdsSuspend(ids: Set<String>): Boolean {
        val ok = interestsRepository.setFollowedInterestIdsSuspend(ids)
        if (ok) markFeedShouldRefetch()
        return ok
    }

    suspend fun setOnboardingCompleteSuspend(): Boolean {
        val ok = interestsRepository.setOnboardingCompleteSuspend()
        if (ok) markFeedShouldRefetch()
        return ok
    }

    // new catalog persistence
    suspend fun addCustomInterestPersisted(name: String, type: InterestType): Boolean {
        val interest = interestsCatalogRepository.addCustomInterestPersisted(name, type)
        val followed = followInterestSuspend(interest.id)
        if (!followed) {
            followInterest(interest.id)
        }
        return true
    }

    // Discover: ensure catalog + follow row exist in order (avoids Supabase FK / race issues).
    suspend fun followDiscoverInterest(interest: Interest): Boolean {
        if (interestsRepository.needsAuthenticatedUserForWrite() && getCloudUserId() == null) {
            return false
        }
        return withContext(Dispatchers.IO) {
            val resolved = interestsCatalogRepository.addCustomInterestPersisted(interest.name, interest.type)
            followInterestSuspend(resolved.id)
        }
    }

    // remove row postgres for discover
    suspend fun unfollowDiscoverInterest(interest: Interest): Boolean {
        if (interestsRepository.needsAuthenticatedUserForWrite() && getCloudUserId() == null) {
            return false
        }
        return withContext(Dispatchers.IO) {
            unfollowInterestSuspend(interest.id)
        }
    }

    fun isOnboardingComplete(): Boolean = interestsRepository.isOnboardingComplete()

    //Skip topic-selection when the profile says onboarding is done, or when the user already has
    //followed interests
    fun shouldSkipTopicSelection(): Boolean =
        isOnboardingComplete() || getFollowedInterestIds().isNotEmpty()
    fun setOnboardingComplete() {
        interestsRepository.setOnboardingComplete()
    }

    fun getCurrentUserId(): String? = authRepository?.getCurrentUserId()

    fun getUsername(): String = userPreferencesRepository.getUsername()
    fun setUsername(username: String) {
        userPreferencesRepository.setUsername(username)
    }

    //Re-load user profile fields from Supabase 
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
        markFeedShouldRefetch()
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

    fun getReadingHistory(): List<ReadingHistoryItem> =
        readingHistoryRepository.getReadingHistory().filter { item ->
            getArticle(item.articleId)?.hasDisplayImage == true
        }
    fun addToReadingHistory(articleId: String, title: String) {
        readingHistoryRepository.addToHistory(articleId, title)
    }

    fun getSavedArticles(): Flow<List<Article>> =
        savedArticlesRepository.getSavedArticles().map { list ->
            list.filter { it.hasDisplayImage }
        }
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
        ).filter { it.hasDisplayImage }

        val heuristicRanked = scoreRelatedArticles(article, candidates)
        val shortlist = heuristicRanked.take(15)
        return RelatedArticlesLlmRanker.pickTop3(baseArticle = article, shortlist = shortlist)
    }

    fun searchArticles(query: String): List<Article> =
        getFeed().filter { it.matches(query) }

    suspend fun searchArticlesByKeyword(keyword: String): List<Article> {
        val raw = ArticleDeduplicator.dedupePreservingOrder(newsRepository.searchByKeyword(keyword))
        val results = raw
            .filter { DiscoverCategoryRelevance.matchesDiscoverCategory(keyword, it) }
            .filter { it.hasDisplayImage }
        results.forEach { discoverCache[it.id] = it }
        return results
    }

    fun getUserProfile(): UserProfile = UserProfile(
        username = getUsername(),
        memberSince = getMemberSince(),
        selectedInterests = getFollowedInterests()
    )
}
