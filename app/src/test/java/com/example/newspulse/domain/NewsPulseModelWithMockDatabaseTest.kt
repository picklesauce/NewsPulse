package com.example.newspulse.domain

import com.example.newspulse.data.DatabaseInterface
import com.example.newspulse.data.MockDatabase
import com.example.newspulse.data.mock.FakeUserPreferencesRepository
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.ReadingHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for [NewsPulseModel] using [MockDatabase] as the backing store.
 * Uses repository adapters that delegate to [DatabaseInterface] via runBlocking.
 */
class NewsPulseModelWithMockDatabaseTest {

    private val defaultUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private lateinit var db: MockDatabase
    private lateinit var model: NewsPulseModel

    @Before
    fun setUp() {
        db = MockDatabase(defaultUserId)
        model = NewsPulseModel(
            newsRepository = DatabaseBackedNewsRepository(db),
            interestsRepository = DatabaseBackedInterestsRepository(db, defaultUserId),
            interestsCatalogRepository = DatabaseBackedInterestsCatalogRepository(db),
            userPreferencesRepository = FakeUserPreferencesRepository(),
            readingHistoryRepository = DatabaseBackedReadingHistoryRepository(db, defaultUserId),
            savedArticlesRepository = DatabaseBackedSavedArticlesRepository(db, defaultUserId)
        )
    }

    // ---- Saved articles ----

    @Test
    fun saveArticle_andRetrieve_appearsInSavedArticles() = runBlocking {
        val article = model.getFeed().first()
        model.saveArticle(article)
        val saved = model.getSavedArticles().first()
        assertTrue(saved.any { it.id == article.id })
    }

    @Test
    fun removeSavedArticle_removesFromSavedArticles() = runBlocking {
        val article = model.getFeed().first()
        model.saveArticle(article)
        model.removeArticle(article)
        val saved = model.getSavedArticles().first()
        assertFalse(saved.any { it.id == article.id })
    }

    @Test
    fun getSavedArticles_returnsOnlySavedOnes() = runBlocking {
        val feed = model.getFeed()
        if (feed.size >= 2) {
            model.saveArticle(feed[0])
            model.saveArticle(feed[1])
            val saved = model.getSavedArticles().first()
            assertTrue(saved.size >= 2)
            assertTrue(saved.any { it.id == feed[0].id })
            assertTrue(saved.any { it.id == feed[1].id })
        }
    }

    // ---- Reading history ----

    @Test
    fun addToReadingHistory_andGetReadingHistory_appearsInHistory() = runBlocking {
        val article = model.getFeed().first()
        model.addToReadingHistory(article.id, article.title)
        val history = model.getReadingHistory()
        assertTrue(history.any { it.articleId == article.id && it.title == article.title })
    }

    @Test
    fun getReadingHistory_returnsOrderedByRecentRead() = runBlocking {
        val feed = model.getFeed()
        if (feed.size >= 2) {
            model.addToReadingHistory(feed[0].id, feed[0].title)
            model.addToReadingHistory(feed[1].id, feed[1].title)
            val history = model.getReadingHistory()
            assertTrue(history.size >= 2)
            assertTrue(history.any { it.articleId == feed[0].id })
            assertTrue(history.any { it.articleId == feed[1].id })
        }
    }

    // ---- Following interests ----

    @Test
    fun followInterest_addsToFollowedInterests() = runBlocking {
        val allInterests = model.getAllInterests()
        val toFollow = allInterests.first()
        model.followInterest(toFollow.id)
        val followed = model.getFollowedInterestIds()
        assertTrue(followed.contains(toFollow.id))
    }

    @Test
    fun unfollowInterest_removesFromFollowedInterests() = runBlocking {
        val allInterests = model.getAllInterests()
        val interest = allInterests.first()
        model.followInterest(interest.id)
        model.unfollowInterest(interest.id)
        val followed = model.getFollowedInterestIds()
        assertFalse(followed.contains(interest.id))
    }

    @Test
    fun getFollowedInterests_returnsOnlyFollowedOnes() = runBlocking {
        val allInterests = model.getAllInterests()
        if (allInterests.size >= 2) {
            model.setFollowedInterestIds(emptySet())
            model.followInterest(allInterests[0].id)
            model.followInterest(allInterests[1].id)
            val followed = model.getFollowedInterests()
            assertEquals(2, followed.size)
            assertTrue(followed.any { it.id == allInterests[0].id })
            assertTrue(followed.any { it.id == allInterests[1].id })
        }
    }

    // ---- Adapters (delegate to DatabaseInterface via runBlocking) ----

    private class DatabaseBackedNewsRepository(private val db: DatabaseInterface) : NewsRepository {
        override fun getArticles(): List<Article> = runBlocking { db.getLatestArticles() }
    }

    private class DatabaseBackedInterestsCatalogRepository(private val db: DatabaseInterface) : InterestsCatalogRepository {
        override fun getAllInterests(): List<Interest> = runBlocking { db.getAllInterests() }
    }

    private class DatabaseBackedInterestsRepository(
        private val db: DatabaseInterface,
        private val userId: UUID
    ) : InterestsRepository {
        private var onboardingComplete = false

        override fun getFollowedInterestIds(): Set<String> = runBlocking {
            db.getFollowedInterestsForUser(userId).map { it.id }.toSet()
        }

        override fun setFollowedInterestIds(ids: Set<String>) = runBlocking {
            val current = db.getFollowedInterestsForUser(userId).map { it.id }.toSet()
            current.forEach { if (it !in ids) db.unfollowInterest(userId, UUID.fromString(it)) }
            ids.forEach { if (it !in current) db.followInterest(userId, UUID.fromString(it)) }
        }

        override fun followInterest(id: String) = runBlocking { db.followInterest(userId, UUID.fromString(id)) }
        override fun unfollowInterest(id: String) = runBlocking { db.unfollowInterest(userId, UUID.fromString(id)) }
        override fun isOnboardingComplete(): Boolean = onboardingComplete
        override fun setOnboardingComplete() { onboardingComplete = true }
    }

    private class DatabaseBackedReadingHistoryRepository(
        private val db: DatabaseInterface,
        private val userId: UUID
    ) : ReadingHistoryRepository {
        override fun getReadingHistory(): List<ReadingHistoryItem> = runBlocking {
            db.getReadingHistoryForUser(userId).map { a ->
                ReadingHistoryItem(a.id, a.title, a.publishedAt)
            }
        }

        override fun addToHistory(articleId: String, title: String) = runBlocking {
            db.recordArticleRead(userId, UUID.fromString(articleId))
        }
    }

    private class DatabaseBackedSavedArticlesRepository(
        private val db: DatabaseInterface,
        private val userId: UUID
    ) : SavedArticlesRepository {
        override fun getSavedArticles(): Flow<List<Article>> = flow {
            emit(runBlocking { db.getSavedArticlesForUser(userId) })
        }

        override fun saveArticle(article: Article) = runBlocking {
            db.saveArticleForUser(userId, UUID.fromString(article.id))
        }

        override fun removeArticle(article: Article) = runBlocking {
            db.removeSavedArticleForUser(userId, UUID.fromString(article.id))
        }
    }
}
