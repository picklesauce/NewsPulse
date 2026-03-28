package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import java.net.URI

/**
 * Detects duplicate news stories when the API returns the same item under different
 * article ids (syndication, republishing). Uses stable keys: [Article.id], canonical
 * [Article.url], and normalized title + source together.
 */
object ArticleDeduplicator {

    /**
     * Mutable state for one merge pass; create a new instance per [dedupePreservingOrder] or feed build.
     */
    class State internal constructor(
        internal val seenIds: MutableSet<String> = mutableSetOf(),
        internal val seenCanonicalUrls: MutableSet<String> = mutableSetOf(),
        internal val seenTitleSource: MutableSet<String> = mutableSetOf()
    ) {
        /**
         * Returns true if [article] is new and was recorded; false if it matched an earlier article.
         */
        fun tryAccept(article: Article): Boolean {
            if (ArticleDeduplicator.isDuplicateOfSeen(article, this)) return false
            ArticleDeduplicator.recordKeys(article, this)
            return true
        }
    }

    fun newState(): State = State()

    /**
     * Returns articles in order, skipping any that duplicate an earlier item by id, canonical URL,
     * or (normalized title + normalized source).
     */
    fun dedupePreservingOrder(articles: List<Article>): List<Article> {
        val state = newState()
        return articles.filter { state.tryAccept(it) }
    }

    internal fun isDuplicateOfSeen(article: Article, state: State): Boolean {
        if (article.id.isNotBlank() && article.id in state.seenIds) return true
        canonicalUrl(article.url)?.let { canonical ->
            if (canonical in state.seenCanonicalUrls) return true
        }
        val ts = titleSourceKey(article.title, article.source)
        if (ts in state.seenTitleSource) return true
        return false
    }

    internal fun recordKeys(article: Article, state: State) {
        if (article.id.isNotBlank()) state.seenIds.add(article.id)
        canonicalUrl(article.url)?.let { state.seenCanonicalUrls.add(it) }
        state.seenTitleSource.add(titleSourceKey(article.title, article.source))
    }

    /** Scheme + host + path, lowercase, no trailing slash, query stripped (tracking params often differ). */
    fun canonicalUrl(url: String): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return null
        return try {
            val uri = URI(trimmed)
            val scheme = (uri.scheme ?: "https").lowercase()
            val host = uri.host?.lowercase() ?: return trimmed.lowercase()
            val path = (uri.path ?: "").trimEnd('/')
            "$scheme://$host$path"
        } catch (_: Exception) {
            trimmed.lowercase()
        }
    }

    fun titleSourceKey(title: String, source: String): String {
        val t = title.trim().lowercase().replace(Regex("\\s+"), " ")
        val s = source.trim().lowercase().replace(Regex("\\s+"), " ")
        return "$t\u0001$s"
    }
}
