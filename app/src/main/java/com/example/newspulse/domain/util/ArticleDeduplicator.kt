package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article
import java.net.URI

// Detects duplicate news stories when  API returns  same item under different ids

object ArticleDeduplicator {


    class State internal constructor(
        internal val seenIds: MutableSet<String> = mutableSetOf(),
        internal val seenCanonicalUrls: MutableSet<String> = mutableSetOf(),
        internal val seenTitleSource: MutableSet<String> = mutableSetOf()
    ) {

        fun tryAccept(article: Article): Boolean {
            if (ArticleDeduplicator.isDuplicateOfSeen(article, this)) return false
            ArticleDeduplicator.recordKeys(article, this)
            return true
        }
    }

    fun newState(): State = State()

    // articles returned in order, skipping duplicate
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
