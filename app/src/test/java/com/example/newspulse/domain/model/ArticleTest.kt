package com.example.newspulse.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleTest {

    @Test
    fun isValid_returnsTrue_whenIdAndTitleAreNonBlank() {
        val article = Article(
            id = "art-1",
            title = "Test Title",
            source = "Source",
            publishedAt = 0L
        )
        assertTrue(article.isValid())
    }

    @Test
    fun isValid_returnsFalse_whenIdIsBlank() {
        val article = Article(
            id = "",
            title = "Test Title",
            source = "Source",
            publishedAt = 0L
        )
        assertFalse(article.isValid())
    }

    @Test
    fun isValid_returnsFalse_whenTitleIsBlank() {
        val article = Article(
            id = "art-1",
            title = "",
            source = "Source",
            publishedAt = 0L
        )
        assertFalse(article.isValid())
    }

    @Test
    fun topics_returnsInterestNames() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Tech"),
            Interest("i-2", InterestType.Topic, "Business")
        )
        val article = Article(
            id = "art-1",
            title = "Title",
            source = "Source",
            publishedAt = 0L,
            interests = interests
        )
        assertEquals(listOf("Tech", "Business"), article.topics)
    }

    @Test
    fun snippet_returnsSummary() {
        val article = Article(
            id = "art-1",
            title = "Title",
            source = "Source",
            publishedAt = 0L,
            summary = "A brief summary"
        )
        assertEquals("A brief summary", article.snippet)
    }

    @Test
    fun matches_returnsTrue_whenQueryIsEmpty() {
        val article = Article(id = "1", title = "Title", source = "Source", publishedAt = 0L)
        assertTrue(article.matches(""))
        assertTrue(article.matches("   "))
    }

    @Test
    fun matches_returnsTrue_whenQueryInTitle() {
        val article = Article(id = "1", title = "Tech Breakthrough", source = "News", publishedAt = 0L)
        assertTrue(article.matches("tech"))
        assertTrue(article.matches("BREAK"))
    }

    @Test
    fun matches_returnsTrue_whenQueryInSource() {
        val article = Article(id = "1", title = "Title", source = "TechNews", publishedAt = 0L)
        assertTrue(article.matches("tech"))
    }

    @Test
    fun matches_returnsTrue_whenQueryInSummary() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, summary = "Major discovery in science")
        assertTrue(article.matches("science"))
    }

    @Test
    fun matches_returnsTrue_whenQueryInInterests() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Technology"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matches("technology"))
    }

    @Test
    fun matches_returnsFalse_whenQueryNotFound() {
        val article = Article(id = "1", title = "Title", source = "Source", publishedAt = 0L)
        assertFalse(article.matches("xyz"))
    }

    @Test
    fun matchesInterests_returnsTrue_whenSelectedIsEmpty() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L)
        assertTrue(article.matchesInterests(emptySet()))
    }

    @Test
    fun matchesInterests_returnsTrue_whenArticleHasMatchingInterest() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Tech"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matchesInterests(setOf("Tech", "Other")))
    }

    @Test
    fun matchesInterests_returnsFalse_whenNoMatchingInterest() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Tech"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertFalse(article.matchesInterests(setOf("Business", "Politics")))
    }
}
