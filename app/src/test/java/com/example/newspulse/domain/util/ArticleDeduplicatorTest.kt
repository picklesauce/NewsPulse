package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleDeduplicatorTest {

    private val now = System.currentTimeMillis()
    private val tech = Interest("t1", InterestType.Topic, "Tech")

    private fun article(
        id: String,
        title: String,
        source: String = "CNN",
        url: String = "",
        publishedAt: Long = now
    ) = Article(
        id = id,
        title = title,
        source = source,
        url = url,
        publishedAt = publishedAt,
        interests = listOf(tech)
    )

    @Test
    fun canonicalUrl_stripsQueryAndNormalizesHost() {
        val a = ArticleDeduplicator.canonicalUrl("https://NEWS.example.com/path/to/story?utm_source=x&ref=1")
        val b = ArticleDeduplicator.canonicalUrl("https://news.example.com/path/to/story")
        assertEquals(a, b)
    }

    @Test
    fun canonicalUrl_blank_returnsNull() {
        assertNull(ArticleDeduplicator.canonicalUrl("   "))
    }

    @Test
    fun dedupePreservingOrder_sameId_keepsFirst() {
        val first = article("1", "A", url = "https://x.com/1")
        val dup = article("1", "Different title", url = "https://other.com")
        val out = ArticleDeduplicator.dedupePreservingOrder(listOf(first, dup))
        assertEquals(1, out.size)
        assertEquals("A", out[0].title)
    }

    @Test
    fun dedupePreservingOrder_sameCanonicalUrl_differentIds_keepsFirst() {
        val first = article("uri-a", "Story", url = "https://site.com/article?id=1")
        val second = article("uri-b", "Story", url = "https://site.com/article?id=2")
        val out = ArticleDeduplicator.dedupePreservingOrder(listOf(first, second))
        assertEquals(1, out.size)
        assertEquals("uri-a", out[0].id)
    }

    @Test
    fun dedupePreservingOrder_sameTitleAndSource_differentIds_keepsFirst() {
        val first = article("a", "Breaking: Markets Rise", "Reuters", url = "")
        val second = article("b", "Breaking: Markets Rise", "Reuters", url = "")
        val out = ArticleDeduplicator.dedupePreservingOrder(listOf(first, second))
        assertEquals(1, out.size)
    }

    @Test
    fun dedupePreservingOrder_sameTitleDifferentSource_keepsBoth() {
        val first = article("a", "Same headline", "CNN")
        val second = article("b", "Same headline", "BBC")
        val out = ArticleDeduplicator.dedupePreservingOrder(listOf(first, second))
        assertEquals(2, out.size)
    }
}
