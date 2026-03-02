package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the related articles scoring utility.
 * Tests the scoreRelatedArticles() function that scores and sorts articles
 * based on shared interests and source matching.
 * 
 * Test Target (per ticket S2-15):
 * - Related-article scoring - scoreRelatedArticles() function
 * 
 * Scoring Algorithm:
 * - 10 points per shared interest
 * - 5 points for same source
 * - Sorted by score (highest first), then by publishedAt (newest first)
 */
class RelatedArticlesTest {

    /**
     * Helper function to create test articles with default values.
     * Makes test setup more concise and readable.
     */
    private fun article(
        id: String,
        source: String = "News",
        publishedAt: Long = 0L,
        interests: List<Interest> = emptyList()
    ) = Article(id = id, title = "Title $id", source = source, publishedAt = publishedAt, interests = interests)

    // ========== Basic Functionality Tests ==========

    /**
     * Tests that the base article is excluded from the results.
     * An article should not be related to itself.
     */
    @Test
    fun scoreRelatedArticles_excludesBaseArticle() {
        val base = article("art-1")
        val candidates = listOf(article("art-1"), article("art-2"))
        val result = scoreRelatedArticles(base, candidates)
        assertEquals(1, result.size)
        assertEquals("art-2", result[0].id)
    }

    // ========== Scoring Logic Tests ==========

    /**
     * Normal case: Tests that articles with shared interests score higher
     * and are sorted before articles with no shared interests.
     * Score: 1 shared interest = 10 points.
     */
    @Test
    fun scoreRelatedArticles_sortsBySharedInterests() {
        val tech = Interest("i-1", InterestType.Topic, "Tech")
        val base = article("art-1", interests = listOf(tech))
        val withShared = article("art-2", interests = listOf(tech))
        val noShared = article("art-3", interests = emptyList())
        val result = scoreRelatedArticles(base, listOf(noShared, withShared))
        assertEquals("art-2", result[0].id)
        assertEquals("art-3", result[1].id)
    }

    /**
     * Normal case: Tests that articles from the same source get bonus points.
     * Score: same source = 5 points.
     */
    @Test
    fun scoreRelatedArticles_sameSourceAddsScore() {
        val base = article("art-1", source = "TechNews")
        val sameSource = article("art-2", source = "TechNews")
        val diffSource = article("art-3", source = "Other")
        val result = scoreRelatedArticles(base, listOf(diffSource, sameSource))
        assertEquals("art-2", result[0].id)
    }

    /**
     * Edge case: Tests that when articles have the same score (no shared interests, different sources),
     * they are sorted by publishedAt with newest articles first.
     */
    @Test
    fun scoreRelatedArticles_sortsByPublishedAt_whenScoresEqual() {
        val base = article("art-1", publishedAt = 100L)
        val older = article("art-2", publishedAt = 50L)
        val newer = article("art-3", publishedAt = 150L)
        val result = scoreRelatedArticles(base, listOf(older, newer))
        assertEquals("art-3", result[0].id)
        assertEquals("art-2", result[1].id)
    }

    /**
     * Edge case: Tests that an empty candidates list returns an empty result list.
     */
    @Test
    fun scoreRelatedArticles_returnsEmpty_whenCandidatesEmpty() {
        val base = article("art-1")
        val result = scoreRelatedArticles(base, emptyList())
        assertTrue(result.isEmpty())
    }

    // ========== Advanced Scoring Tests ==========

    /**
     * Edge case: Tests that articles with more shared interests score higher.
     * Score calculation: 2 shared interests = 20 points, 1 shared = 10 points, 0 shared = 0 points.
     */
    @Test
    fun scoreRelatedArticles_sortsByMultipleSharedInterests() {
        val tech = Interest("i-1", InterestType.Topic, "Tech")
        val business = Interest("i-2", InterestType.Topic, "Business")
        val base = article("art-1", interests = listOf(tech, business))
        val twoShared = article("art-2", interests = listOf(tech, business))
        val oneShared = article("art-3", interests = listOf(tech))
        val noShared = article("art-4", interests = emptyList())
        val result = scoreRelatedArticles(base, listOf(noShared, oneShared, twoShared))
        assertEquals("art-2", result[0].id) // 2 shared interests = score 20
        assertEquals("art-3", result[1].id) // 1 shared interest = score 10
        assertEquals("art-4", result[2].id) // 0 shared interests = score 0
    }

    /**
     * Edge case: Tests that scoring combines both shared interests and same source.
     * Score calculation: shared interests (10) + same source (5) = 15 total.
     * Verifies that both factors contribute to the final score.
     */
    @Test
    fun scoreRelatedArticles_combinesSharedInterestsAndSameSource() {
        val tech = Interest("i-1", InterestType.Topic, "Tech")
        val base = article("art-1", source = "TechNews", interests = listOf(tech))
        val sharedAndSameSource = article("art-2", source = "TechNews", interests = listOf(tech))
        val sharedButDiffSource = article("art-3", source = "Other", interests = listOf(tech))
        val diffSourceNoShared = article("art-4", source = "Other", interests = emptyList())
        val result = scoreRelatedArticles(base, listOf(diffSourceNoShared, sharedButDiffSource, sharedAndSameSource))
        assertEquals("art-2", result[0].id) // score: 10 (shared) + 5 (same source) = 15
        assertEquals("art-3", result[1].id) // score: 10 (shared) + 0 = 10
        assertEquals("art-4", result[2].id) // score: 0
    }

    /**
     * Edge case: Tests that same source bonus applies even without shared interests.
     * Score: 0 shared interests + 5 (same source) = 5 points.
     * This ensures articles from the same source are prioritized even if interests don't match.
     */
    @Test
    fun scoreRelatedArticles_sameSourceWithoutSharedInterests() {
        val tech = Interest("i-1", InterestType.Topic, "Tech")
        val base = article("art-1", source = "TechNews", interests = listOf(tech))
        val sameSourceNoShared = article("art-2", source = "TechNews", interests = emptyList())
        val diffSourceNoShared = article("art-3", source = "Other", interests = emptyList())
        val result = scoreRelatedArticles(base, listOf(diffSourceNoShared, sameSourceNoShared))
        assertEquals("art-2", result[0].id) // score: 0 (shared) + 5 (same source) = 5
        assertEquals("art-3", result[1].id) // score: 0
    }

    /**
     * Edge case: Tests sorting when multiple articles have the same score.
     * All articles have 1 shared interest (score 10), so they're sorted by publishedAt.
     * Newest articles appear first.
     */
    @Test
    fun scoreRelatedArticles_sortsByPublishedAt_whenMultipleArticlesHaveSameScore() {
        val tech = Interest("i-1", InterestType.Topic, "Tech")
        val base = article("art-1", interests = listOf(tech))
        val older1 = article("art-2", publishedAt = 100L, interests = listOf(tech))
        val older2 = article("art-3", publishedAt = 100L, interests = listOf(tech))
        val newer = article("art-4", publishedAt = 200L, interests = listOf(tech))
        val result = scoreRelatedArticles(base, listOf(older1, older2, newer))
        assertEquals("art-4", result[0].id) // newest first
        // When publishedAt is equal, order is stable but not guaranteed - both older articles should be after newer
        assertTrue(result[1].id in listOf("art-2", "art-3"))
        assertTrue(result[2].id in listOf("art-2", "art-3"))
    }

    /**
     * Edge case: Tests handling of articles with no interests.
     * All articles have score 0 (no shared interests, no same source),
     * so they're sorted by publishedAt (newest first).
     */
    @Test
    fun scoreRelatedArticles_handlesArticlesWithNoInterests() {
        val base = article("art-1", interests = emptyList())
        val candidate1 = article("art-2", interests = emptyList())
        val candidate2 = article("art-3", interests = emptyList())
        val result = scoreRelatedArticles(base, listOf(candidate1, candidate2))
        assertEquals(2, result.size)
        // All have score 0, so sorted by publishedAt (newest first)
        assertTrue(result[0].publishedAt >= result[1].publishedAt)
    }
}
