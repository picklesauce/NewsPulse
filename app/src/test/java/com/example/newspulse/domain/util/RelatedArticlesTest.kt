package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelatedArticlesTest {

    private fun article(
        id: String,
        source: String = "News",
        publishedAt: Long = 0L,
        interests: List<Interest> = emptyList()
    ) = Article(id = id, title = "Title $id", source = source, publishedAt = publishedAt, interests = interests)

    @Test
    fun scoreRelatedArticles_excludesBaseArticle() {
        val base = article("art-1")
        val candidates = listOf(article("art-1"), article("art-2"))
        val result = scoreRelatedArticles(base, candidates)
        assertEquals(1, result.size)
        assertEquals("art-2", result[0].id)
    }

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

    @Test
    fun scoreRelatedArticles_sameSourceAddsScore() {
        val base = article("art-1", source = "TechNews")
        val sameSource = article("art-2", source = "TechNews")
        val diffSource = article("art-3", source = "Other")
        val result = scoreRelatedArticles(base, listOf(diffSource, sameSource))
        assertEquals("art-2", result[0].id)
    }

    @Test
    fun scoreRelatedArticles_sortsByPublishedAt_whenScoresEqual() {
        val base = article("art-1", publishedAt = 100L)
        val older = article("art-2", publishedAt = 50L)
        val newer = article("art-3", publishedAt = 150L)
        val result = scoreRelatedArticles(base, listOf(older, newer))
        assertEquals("art-3", result[0].id)
        assertEquals("art-2", result[1].id)
    }

    @Test
    fun scoreRelatedArticles_returnsEmpty_whenCandidatesEmpty() {
        val base = article("art-1")
        val result = scoreRelatedArticles(base, emptyList())
        assertTrue(result.isEmpty())
    }
}
