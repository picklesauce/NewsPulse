package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article

private val STOP_WORDS = setOf(
    "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
    "of", "with", "by", "from", "as", "is", "was", "are", "were", "be",
    "been", "being", "have", "has", "had", "do", "does", "did", "will",
    "would", "could", "should", "may", "might", "its", "it", "this",
    "that", "these", "those", "his", "her", "their", "our", "your", "my",
    "about", "after", "new", "says", "said", "say", "not", "over", "more"
)

private val WORD_SPLIT = Regex("[^a-z0-9]+")

/** Extract significant lowercase words (length >= 4, not stop words) from a text. */
internal fun significantWords(text: String): Set<String> =
    text.lowercase()
        .split(WORD_SPLIT)
        .filter { it.length >= 4 && it !in STOP_WORDS }
        .toSet()

/**
 * Scores [candidates] against [baseArticle] using three signals:
 * - Shared interest tags: 10 pts each
 * - Same publisher source: 5 pts
 * - Overlapping significant title keywords: 3 pts each (capped at 15)
 *
 * Only returns articles with a positive score, capped at [maxResults].
 * Falls back to same-source articles if nothing has a meaningful score.
 */
fun scoreRelatedArticles(
    baseArticle: Article,
    candidates: List<Article>,
    maxResults: Int = 5
): List<Article> {
    val baseInterestNames = baseArticle.interests.map { it.name }.toSet()
    val baseTitleWords = significantWords(baseArticle.title)

    val scored = candidates
        .filter { it.id != baseArticle.id }
        .map { article ->
            val sharedInterests = article.interests.count { it.name in baseInterestNames }
            val sameSource = if (article.source == baseArticle.source) 1 else 0
            val titleOverlap = significantWords(article.title)
                .count { it in baseTitleWords }
                .coerceAtMost(5)
            val score = sharedInterests * 10 + sameSource * 5 + titleOverlap * 3
            article to score
        }
        .sortedWith(compareBy({ -it.second }, { -it.first.publishedAt }))

    // Return top results with positive score, or fallback to same-source if nothing qualifies.
    val meaningful = scored.filter { it.second > 0 }.take(maxResults).map { it.first }
    if (meaningful.isNotEmpty()) return ArticleDeduplicator.dedupePreservingOrder(meaningful)

    val sameSource = scored.filter { it.first.source == baseArticle.source }
        .take(maxResults).map { it.first }
    return ArticleDeduplicator.dedupePreservingOrder(sameSource)
}
