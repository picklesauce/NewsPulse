package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedRankerTest {

    private fun article(
        id: String,
        source: String = "Source",
        publishedAt: Long = System.currentTimeMillis(),
        imageUrl: String = "",
        interests: List<Interest> = emptyList()
    ) = Article(
        id = id, title = "Title $id", source = source,
        publishedAt = publishedAt, imageUrl = imageUrl, interests = interests
    )

    private val tech = Interest("i-1", InterestType.Topic, "Tech")
    private val sports = Interest("i-2", InterestType.Topic, "Sports")

    @Test
    fun balancedMerge_interleavesAcrossInterests() {
        val buckets = mapOf(
            "Tech" to listOf(article("t1"), article("t2"), article("t3")),
            "Sports" to listOf(article("s1"), article("s2"), article("s3"))
        )
        val merged = FeedRanker.balancedMerge(buckets)
        assertEquals(6, merged.size)
        assertEquals("t1", merged[0].id)
        assertEquals("s1", merged[1].id)
        assertEquals("t2", merged[2].id)
        assertEquals("s2", merged[3].id)
    }

    @Test
    fun balancedMerge_deduplicatesByArticleId() {
        val shared = article("shared")
        val buckets = mapOf(
            "Tech" to listOf(shared, article("t1")),
            "Sports" to listOf(shared, article("s1"))
        )
        val merged = FeedRanker.balancedMerge(buckets)
        assertEquals(3, merged.size)
        assertEquals(1, merged.count { it.id == "shared" })
    }

    @Test
    fun balancedMerge_deduplicatesByCanonicalUrl_differentIds() {
        val now = System.currentTimeMillis()
        val a = Article(
            id = "id-1",
            title = "Story",
            source = "Src",
            url = "https://example.com/news/story?utm=1",
            publishedAt = now
        )
        val b = Article(
            id = "id-2",
            title = "Story",
            source = "Src",
            url = "https://example.com/news/story",
            publishedAt = now
        )
        val buckets = mapOf(
            "Tech" to listOf(a),
            "Sports" to listOf(b)
        )
        val merged = FeedRanker.balancedMerge(buckets)
        assertEquals(1, merged.size)
    }

    @Test
    fun balancedMerge_deduplicatesByTitleAndSource_differentIds() {
        val now = System.currentTimeMillis()
        val a = Article(
            id = "a",
            title = "Exclusive report",
            source = "AP News",
            url = "",
            publishedAt = now
        )
        val b = Article(
            id = "b",
            title = "exclusive  report",
            source = "ap news",
            url = "",
            publishedAt = now
        )
        val buckets = mapOf("Tech" to listOf(a), "Sports" to listOf(b))
        val merged = FeedRanker.balancedMerge(buckets)
        assertEquals(1, merged.size)
    }

    @Test
    fun balancedMerge_capsPerInterest() {
        val many = (1..20).map { article("a$it") }
        val merged = FeedRanker.balancedMerge(mapOf("Tech" to many))
        assertEquals(10, merged.size)
    }

    @Test
    fun balancedMerge_emptyBuckets_returnsEmpty() {
        assertTrue(FeedRanker.balancedMerge(emptyMap()).isEmpty())
    }

    @Test
    fun score_newerArticlesScoreHigher() {
        val now = System.currentTimeMillis()
        val recent = article("r", publishedAt = now)
        val old = article("o", publishedAt = now - 48L * 60 * 60 * 1000)
        assertTrue(FeedRanker.score(recent) > FeedRanker.score(old))
    }

    @Test
    fun score_imageGivesBonus() {
        val now = System.currentTimeMillis()
        val withImage = article("a", publishedAt = now, imageUrl = "https://img.jpg")
        val noImage = article("b", publishedAt = now, imageUrl = "")
        assertTrue(FeedRanker.score(withImage) > FeedRanker.score(noImage))
    }

    @Test
    fun diversify_splitsConsecutiveSameSource() {
        val articles = listOf(
            article("a1", source = "CNN"),
            article("a2", source = "CNN"),
            article("a3", source = "BBC"),
            article("a4", source = "Reuters")
        )
        val result = FeedRanker.diversify(articles)
        for (i in 0 until result.size - 1) {
            if (result[i].source == result[i + 1].source) {
                val sameSourceCount = result.windowed(2).count { (a, b) -> a.source == b.source }
                assertTrue(
                    "Diversify should reduce consecutive same-source pairs",
                    sameSourceCount < articles.windowed(2).count { (a, b) -> a.source == b.source }
                )
                return
            }
        }
    }

    @Test
    fun diversify_singleArticle_unchanged() {
        val single = listOf(article("a1"))
        assertEquals(single, FeedRanker.diversify(single))
    }

    @Test
    fun rank_producesNonEmptyOutputFromNonEmptyInput() {
        val buckets = mapOf(
            "Tech" to listOf(article("t1", interests = listOf(tech))),
            "Sports" to listOf(article("s1", interests = listOf(sports)))
        )
        val ranked = FeedRanker.rank(buckets)
        assertEquals(2, ranked.size)
    }

    @Test
    fun rank_emptyInput_returnsEmpty() {
        assertTrue(FeedRanker.rank(emptyMap()).isEmpty())
    }
}
