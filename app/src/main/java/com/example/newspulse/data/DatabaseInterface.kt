package com.example.newspulse.data

import java.time.Instant
import java.util.UUID
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.UserProfile

interface DatabaseInterface {

    // Articles

    suspend fun getArticleById(articleId: UUID): Article?

    suspend fun getLatestArticles(limit: Int? = null): List<Article>

    suspend fun getArticlesByInterest(
        interestId: UUID,
        limit: Int? = null
    ): List<Article>

    // Saved articles

    suspend fun getSavedArticlesForUser(userId: UUID): List<Article>

    suspend fun saveArticleForUser(
        userId: UUID,
        articleId: UUID
    )

    suspend fun removeSavedArticleForUser(
        userId: UUID,
        articleId: UUID
    )

    suspend fun isArticleSavedForUser(
        userId: UUID,
        articleId: UUID
    ): Boolean

    // Reading history

    suspend fun recordArticleRead(
        userId: UUID,
        articleId: UUID,
        readAt: Instant = Instant.now()
    )

    suspend fun getReadingHistoryForUser(
        userId: UUID,
        limit: Int? = null
    ): List<Article>

    // Interests / following

    suspend fun getAllInterests(): List<Interest>

    suspend fun getFollowedInterestsForUser(userId: UUID): List<Interest>

    suspend fun followInterest(
        userId: UUID,
        interestId: UUID
    )

    suspend fun unfollowInterest(
        userId: UUID,
        interestId: UUID
    )

    suspend fun isInterestFollowedByUser(
        userId: UUID,
        interestId: UUID
    ): Boolean

    // User profile

    suspend fun getUserProfile(userId: UUID): UserProfile?

    suspend fun upsertUserProfile(profile: UserProfile): UserProfile
}
