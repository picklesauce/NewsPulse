package com.example.newspulse.data

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.model.UserProfile
import java.time.Instant
import java.util.UUID

/**
 * In-memory implementation of [DatabaseInterface] for unit testing.
 * Uses mutable lists and maps; no SQL or external database.
 */
class MockDatabase(
    private val defaultUserId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
) : DatabaseInterface {

    private val articles = mutableMapOf<UUID, Article>()
    private val interests = mutableMapOf<UUID, Interest>()
    private val articleToInterests = mutableMapOf<UUID, MutableSet<UUID>>()
    private val userSavedArticles = mutableMapOf<UUID, MutableSet<UUID>>()
    private val readingHistory = mutableListOf<Triple<UUID, UUID, Instant>>()
    private val userFollowedInterests = mutableMapOf<UUID, MutableSet<UUID>>()
    private val userProfiles = mutableMapOf<UUID, UserProfile>()

    init {
        insertSampleData()
    }

    private fun insertSampleData() {
        val i1 = UUID.randomUUID()
        val i2 = UUID.randomUUID()
        val i3 = UUID.randomUUID()
        interests[i1] = Interest(id = i1.toString(), type = InterestType.Topic, name = "Technology")
        interests[i2] = Interest(id = i2.toString(), type = InterestType.Country, name = "USA")
        interests[i3] = Interest(id = i3.toString(), type = InterestType.Topic, name = "Science")

        val a1 = UUID.randomUUID()
        val a2 = UUID.randomUUID()
        val a3 = UUID.randomUUID()
        val now = System.currentTimeMillis()
        articles[a1] = Article(
            id = a1.toString(),
            title = "Sample Tech Article",
            source = "Tech News",
            publishedAt = now - 3600000,
            summary = "A sample article about technology.",
            interests = listOf(interests[i1]!!)
        )
        articles[a2] = Article(
            id = a2.toString(),
            title = "Sample Science Article",
            source = "Science Daily",
            publishedAt = now - 7200000,
            summary = "A sample article about science.",
            interests = listOf(interests[i3]!!)
        )
        articles[a3] = Article(
            id = a3.toString(),
            title = "Sample USA News",
            source = "National News",
            publishedAt = now,
            summary = "News from the USA.",
            interests = listOf(interests[i2]!!, interests[i1]!!)
        )

        articleToInterests[a1] = mutableSetOf(i1)
        articleToInterests[a2] = mutableSetOf(i3)
        articleToInterests[a3] = mutableSetOf(i2, i1)

        userProfiles[defaultUserId] = UserProfile(
            username = "TestUser",
            memberSince = "Jan 2025",
            selectedInterests = listOf(interests[i1]!!, interests[i2]!!)
        )
        userFollowedInterests.getOrPut(defaultUserId) { mutableSetOf() }.addAll(listOf(i1, i2))
        userSavedArticles.getOrPut(defaultUserId) { mutableSetOf() }.add(a1)
        readingHistory.add(Triple(defaultUserId, a1, Instant.now().minusSeconds(300)))
    }

    override suspend fun getArticleById(articleId: UUID): Article? {
        val article = articles[articleId] ?: return null
        val interestIds = articleToInterests[articleId] ?: emptySet()
        val articleInterests = interestIds.mapNotNull { interests[it] }
        return article.copy(interests = articleInterests)
    }

    override suspend fun getLatestArticles(limit: Int?): List<Article> {
        val list = articles.values
            .sortedByDescending { it.publishedAt }
            .map { a ->
                val interestIds = articleToInterests[UUID.fromString(a.id)] ?: emptySet()
                a.copy(interests = interestIds.mapNotNull { interests[it] })
            }
        return if (limit != null) list.take(limit) else list
    }

    override suspend fun getArticlesByInterest(interestId: UUID, limit: Int?): List<Article> {
        val articleIds = articleToInterests.filter { it.value.contains(interestId) }.keys
        val list = articleIds.mapNotNull { articles[it] }
            .sortedByDescending { it.publishedAt }
            .map { a ->
                val ids = articleToInterests[UUID.fromString(a.id)] ?: emptySet()
                a.copy(interests = ids.mapNotNull { interests[it] })
            }
        return if (limit != null) list.take(limit) else list
    }

    override suspend fun getSavedArticlesForUser(userId: UUID): List<Article> {
        val savedIds = userSavedArticles[userId] ?: return emptyList()
        return savedIds.mapNotNull { articles[it] }
            .map { a ->
                val ids = articleToInterests[UUID.fromString(a.id)] ?: emptySet()
                a.copy(interests = ids.mapNotNull { interests[it] })
            }
    }

    override suspend fun saveArticleForUser(userId: UUID, articleId: UUID) {
        userSavedArticles.getOrPut(userId) { mutableSetOf() }.add(articleId)
    }

    override suspend fun removeSavedArticleForUser(userId: UUID, articleId: UUID) {
        userSavedArticles[userId]?.remove(articleId)
    }

    override suspend fun isArticleSavedForUser(userId: UUID, articleId: UUID): Boolean {
        return userSavedArticles[userId]?.contains(articleId) == true
    }

    override suspend fun recordArticleRead(userId: UUID, articleId: UUID, readAt: Instant) {
        readingHistory.add(Triple(userId, articleId, readAt))
    }

    override suspend fun getReadingHistoryForUser(userId: UUID, limit: Int?): List<Article> {
        val userEntries = readingHistory.filter { it.first == userId }
            .sortedByDescending { it.third }
        val limited = if (limit != null) userEntries.take(limit) else userEntries
        return limited.mapNotNull { (_, articleId, _) -> articles[articleId] }
            .map { a ->
                val ids = articleToInterests[UUID.fromString(a.id)] ?: emptySet()
                a.copy(interests = ids.mapNotNull { interests[it] })
            }
    }

    override suspend fun getAllInterests(): List<Interest> = interests.values.toList()

    override suspend fun getFollowedInterestsForUser(userId: UUID): List<Interest> {
        val ids = userFollowedInterests[userId] ?: return emptyList()
        return ids.mapNotNull { interests[it] }
    }

    override suspend fun followInterest(userId: UUID, interestId: UUID) {
        userFollowedInterests.getOrPut(userId) { mutableSetOf() }.add(interestId)
    }

    override suspend fun unfollowInterest(userId: UUID, interestId: UUID) {
        userFollowedInterests[userId]?.remove(interestId)
    }

    override suspend fun isInterestFollowedByUser(userId: UUID, interestId: UUID): Boolean {
        return userFollowedInterests[userId]?.contains(interestId) == true
    }

    override suspend fun getUserProfile(userId: UUID): UserProfile? = userProfiles[userId]

    override suspend fun upsertUserProfile(profile: UserProfile): UserProfile {
        userProfiles[defaultUserId] = profile
        return profile
    }
}
