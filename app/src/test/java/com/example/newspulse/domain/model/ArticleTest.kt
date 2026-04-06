package com.example.newspulse.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

//article domain model tests
//Article.matches() - search query matching logic
//Interest filtering - matchesInterests() method

class ArticleTest {

    // Valid article tests

    // article is valid when both id and title are non-blank.
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

    // article is invalid when id is blank.
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

    // an article is invalid when title is blank.
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

    //topics property returns a list of interest names.
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

    //  snippet property returns the article's summary.
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

    // Article match tests
    // These tests verify search query matching logic


    @Test
    fun matches_returnsTrue_whenQueryIsEmpty() {
        val article = Article(id = "1", title = "Title", source = "Source", publishedAt = 0L)
        assertTrue(article.matches(""))
        assertTrue(article.matches("   "))
    }


    // that a query matching the article title returns true.

    @Test
    fun matches_returnsTrue_whenQueryInTitle() {
        val article = Article(id = "1", title = "Tech Breakthrough", source = "News", publishedAt = 0L)
        assertTrue(article.matches("tech"))
        assertTrue(article.matches("BREAK"))
    }

    // that a query matching the article source returns true.
    @Test
    fun matches_returnsTrue_whenQueryInSource() {
        val article = Article(id = "1", title = "Title", source = "TechNews", publishedAt = 0L)
        assertTrue(article.matches("tech"))
    }

    //that a query matching the article summary returns true.
    @Test
    fun matches_returnsTrue_whenQueryInSummary() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, summary = "Major discovery in science")
        assertTrue(article.matches("science"))
    }

    // that a query matching any of the article's interests returns true.
    @Test
    fun matches_returnsTrue_whenQueryInInterests() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Technology"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matches("technology"))
    }

    // that a query that doesn't match any field returns false.
    @Test
    fun matches_returnsFalse_whenQueryNotFound() {
        val article = Article(id = "1", title = "Title", source = "Source", publishedAt = 0L)
        assertFalse(article.matches("xyz"))
    }

    // that a query matching multiple fields (title, source, summary, interests)
    //still returns true. Verifies the OR logic works correctly.
    @Test
    fun matches_returnsTrue_whenQueryMatchesMultipleFields() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Technology"))
        val article = Article(
            id = "1",
            title = "Tech News",
            source = "TechSource",
            publishedAt = 0L,
            summary = "Tech summary",
            interests = interests
        )
        // Query matches title, source, summary, and interests
        assertTrue(article.matches("tech"))
    }

    // test partial words
    @Test
    fun matches_returnsTrue_whenQueryIsPartialWord() {
        val article = Article(id = "1", title = "Breakthrough", source = "News", publishedAt = 0L)
        assertTrue(article.matches("break"))
        assertTrue(article.matches("through"))
    }

    //query can match any of multiple interests.
    @Test
    fun matches_returnsTrue_whenQueryMatchesMultipleInterests() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Technology"),
            Interest("i-2", InterestType.Topic, "Science")
        )
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matches("technology"))
        assertTrue(article.matches("science"))
    }

    @Test
    fun matches_returnsFalse_whenQueryIsSubstringButNotContained() {
        val article = Article(id = "1", title = "Tech", source = "News", publishedAt = 0L)
        assertFalse(article.matches("he"))
    }

    // Interest filter tests
    // These tests verify interest filtering logic

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

    @Test
    fun matchesInterests_returnsFalse_whenArticleHasNoInterestsButSelectedIsNotEmpty() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = emptyList())
        assertFalse(article.matchesInterests(setOf("Tech", "Business")))
    }

    @Test
    fun matchesInterests_returnsTrue_whenMultipleInterestsMatch() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Tech"),
            Interest("i-2", InterestType.Topic, "Business")
        )
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matchesInterests(setOf("Tech", "Business", "Other")))
    }

    @Test
    fun matchesInterests_returnsTrue_whenOnlyOneOfMultipleInterestsMatches() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Tech"),
            Interest("i-2", InterestType.Topic, "Science")
        )
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matchesInterests(setOf("Tech", "Politics")))
    }
}
