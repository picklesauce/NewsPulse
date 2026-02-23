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
}
