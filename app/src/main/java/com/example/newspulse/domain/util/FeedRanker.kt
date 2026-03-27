package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article

/**
 * Assembles a final feed from per-interest article buckets by:
 * 1. Balancing representation across interests (round-robin)
 * 2. Scoring each article on freshness + image presence
 * 3. Penalizing consecutive same-source articles for diversity
 */
object FeedRanker {

    private const val MAX_ARTICLES_PER_INTEREST = 10
    private const val FRESHNESS_MAX_POINTS = 50.0
    private const val FRESHNESS_HALF_LIFE_MS = 12L * 60 * 60 * 1000 // 12 hours
    private const val IMAGE_BONUS = 5.0
    private const val SAME_SOURCE_PENALTY = 15.0

    /**
     * Merges per-interest article lists into a single ranked feed.
     *
     * @param buckets map of interest-name → articles for that interest
     * @return a de-duplicated, scored, diversity-adjusted list
     */
    fun rank(buckets: Map<String, List<Article>>): List<Article> {
        val balanced = balancedMerge(buckets)
        val scored = balanced.map { it to score(it) }.sortedByDescending { it.second }
        return diversify(scored.map { it.first })
    }

    /**
     * Round-robin across interest buckets so that each interest gets
     * fair representation even when one interest returns many more results.
     */
    internal fun balancedMerge(buckets: Map<String, List<Article>>): List<Article> {
        if (buckets.isEmpty()) return emptyList()

        val capped = buckets.mapValues { (_, articles) ->
            articles.sortedByDescending { it.publishedAt }.take(MAX_ARTICLES_PER_INTEREST)
        }
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Article>()
        val maxSize = capped.values.maxOf { it.size }
        val keys = capped.keys.toList()

        for (i in 0 until maxSize) {
            for (key in keys) {
                val list = capped[key] ?: continue
                if (i < list.size) {
                    val article = list[i]
                    if (seen.add(article.id)) {
                        result.add(article)
                    }
                }
            }
        }
        return result
    }

    /**
     * Scores a single article. Higher = better placement.
     * - Freshness: exponential decay from [FRESHNESS_MAX_POINTS] with [FRESHNESS_HALF_LIFE_MS]
     * - Image bonus: small bump for articles with a thumbnail
     */
    internal fun score(article: Article): Double {
        val ageMs = (System.currentTimeMillis() - article.publishedAt)
            .coerceAtLeast(0)
        val freshness = FRESHNESS_MAX_POINTS *
            Math.pow(0.5, ageMs.toDouble() / FRESHNESS_HALF_LIFE_MS)
        val image = if (article.imageUrl.isNotBlank()) IMAGE_BONUS else 0.0
        return freshness + image
    }

    /**
     * Pushes consecutive articles from the same source apart.
     * When a repeat source is detected, it is deferred and re-inserted later.
     */
    internal fun diversify(articles: List<Article>): List<Article> {
        if (articles.size <= 1) return articles
        val result = mutableListOf<Article>()
        val deferred = mutableListOf<Article>()
        var lastSource: String? = null

        for (article in articles) {
            if (article.source == lastSource) {
                deferred.add(article)
            } else {
                result.add(article)
                lastSource = article.source
            }
        }
        if (deferred.isEmpty()) return result

        val final = mutableListOf<Article>()
        var deferIdx = 0
        for (article in result) {
            final.add(article)
            if (deferIdx < deferred.size &&
                deferred[deferIdx].source != article.source
            ) {
                final.add(deferred[deferIdx])
                deferIdx++
            }
        }
        while (deferIdx < deferred.size) {
            final.add(deferred[deferIdx])
            deferIdx++
        }
        return final
    }
}
