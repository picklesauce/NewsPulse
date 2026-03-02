package com.example.newspulse.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Article domain model.
 * Tests business logic including search matching and interest filtering.
 * 
 * Test Targets (per ticket S2-15):
 * - Article.matches() - search query matching logic
 * - Interest filtering - matchesInterests() method
 */
class ArticleTest {

    // ========== Article.isValid() Tests ==========

    /**
     * Tests that an article is valid when both id and title are non-blank.
     */
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

    /**
     * Tests that an article is invalid when id is blank.
     */
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

    /**
     * Tests that an article is invalid when title is blank.
     */
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

    /**
     * Tests that the topics property returns a list of interest names.
     */
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

    /**
     * Tests that the snippet property returns the article's summary.
     */
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

    // ========== Article.matches() Tests ==========
    // These tests verify search query matching logic (ticket S2-15 requirement)

    /**
     * Edge case: Tests that an empty or whitespace-only query matches all articles.
     * This is the expected behavior - empty query means "show everything".
     */
    @Test
    fun matches_returnsTrue_whenQueryIsEmpty() {
        val article = Article(id = "1", title = "Title", source = "Source", publishedAt = 0L)
        assertTrue(article.matches(""))
        assertTrue(article.matches("   "))
    }

    /**
     * Normal case: Tests that a query matching the article title returns true.
     * Verifies case-insensitive matching (both "tech" and "BREAK" match).
     */
    @Test
    fun matches_returnsTrue_whenQueryInTitle() {
        val article = Article(id = "1", title = "Tech Breakthrough", source = "News", publishedAt = 0L)
        assertTrue(article.matches("tech"))
        assertTrue(article.matches("BREAK"))
    }

    /**
     * Normal case: Tests that a query matching the article source returns true.
     */
    @Test
    fun matches_returnsTrue_whenQueryInSource() {
        val article = Article(id = "1", title = "Title", source = "TechNews", publishedAt = 0L)
        assertTrue(article.matches("tech"))
    }

    /**
     * Normal case: Tests that a query matching the article summary returns true.
     */
    @Test
    fun matches_returnsTrue_whenQueryInSummary() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, summary = "Major discovery in science")
        assertTrue(article.matches("science"))
    }

    /**
     * Normal case: Tests that a query matching any of the article's interests returns true.
     */
    @Test
    fun matches_returnsTrue_whenQueryInInterests() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Technology"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matches("technology"))
    }

    /**
     * Edge case: Tests that a query that doesn't match any field returns false.
     * This is the "no matches" edge case required by ticket S2-15.
     */
    @Test
    fun matches_returnsFalse_whenQueryNotFound() {
        val article = Article(id = "1", title = "Title", source = "Source", publishedAt = 0L)
        assertFalse(article.matches("xyz"))
    }

    /**
     * Edge case: Tests that a query matching multiple fields (title, source, summary, interests)
     * still returns true. Verifies the OR logic works correctly.
     */
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

    /**
     * Edge case: Tests that partial word matches work (e.g., "break" matches "Breakthrough").
     * Verifies substring matching functionality.
     */
    @Test
    fun matches_returnsTrue_whenQueryIsPartialWord() {
        val article = Article(id = "1", title = "Breakthrough", source = "News", publishedAt = 0L)
        assertTrue(article.matches("break"))
        assertTrue(article.matches("through"))
    }

    /**
     * Edge case: Tests that a query can match any of multiple interests.
     * Verifies the interests.any() logic works correctly.
     */
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

    /**
     * Edge case: Tests that a substring that doesn't form a complete word doesn't match.
     * E.g., "he" should not match "Tech" (it's a substring but not a meaningful match).
     */
    @Test
    fun matches_returnsFalse_whenQueryIsSubstringButNotContained() {
        val article = Article(id = "1", title = "Tech", source = "News", publishedAt = 0L)
        assertFalse(article.matches("he"))
    }

    // ========== Interest Filtering Tests (matchesInterests()) ==========
    // These tests verify interest filtering logic (ticket S2-15 requirement)

    /**
     * Edge case: Tests that when no interests are selected (empty set),
     * all articles match. This means "show all articles" when no filter is applied.
     */
    @Test
    fun matchesInterests_returnsTrue_whenSelectedIsEmpty() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L)
        assertTrue(article.matchesInterests(emptySet()))
    }

    /**
     * Normal case: Tests that an article with a matching interest returns true.
     * The article has "Tech" interest, and "Tech" is in the selected set.
     */
    @Test
    fun matchesInterests_returnsTrue_whenArticleHasMatchingInterest() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Tech"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matchesInterests(setOf("Tech", "Other")))
    }

    /**
     * Edge case: Tests that an article with no matching interests returns false.
     * This is the "no matches" edge case for interest filtering.
     */
    @Test
    fun matchesInterests_returnsFalse_whenNoMatchingInterest() {
        val interests = listOf(Interest("i-1", InterestType.Topic, "Tech"))
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertFalse(article.matchesInterests(setOf("Business", "Politics")))
    }

    /**
     * Edge case: Tests that an article with no interests doesn't match
     * when interests are selected. An article must have at least one matching interest.
     */
    @Test
    fun matchesInterests_returnsFalse_whenArticleHasNoInterestsButSelectedIsNotEmpty() {
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = emptyList())
        assertFalse(article.matchesInterests(setOf("Tech", "Business")))
    }

    /**
     * Edge case: Tests that when an article has multiple interests and multiple match,
     * it returns true. Verifies that only one match is needed (OR logic).
     */
    @Test
    fun matchesInterests_returnsTrue_whenMultipleInterestsMatch() {
        val interests = listOf(
            Interest("i-1", InterestType.Topic, "Tech"),
            Interest("i-2", InterestType.Topic, "Business")
        )
        val article = Article(id = "1", title = "Title", source = "S", publishedAt = 0L, interests = interests)
        assertTrue(article.matchesInterests(setOf("Tech", "Business", "Other")))
    }

    /**
     * Edge case: Tests that when an article has multiple interests but only one matches,
     * it still returns true. Verifies the any() logic - only one match is needed.
     */
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
