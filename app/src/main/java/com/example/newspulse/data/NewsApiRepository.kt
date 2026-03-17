package com.example.newspulse.data

import com.example.newspulse.data.remote.ArticleResult
import com.example.newspulse.data.remote.EventRegistryApi
import com.example.newspulse.domain.NewsRepository
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

/**
 * Fetches articles from Event Registry / NewsAPI.ai and caches them.
 * Keeps default topics (from [topicKeywords]); articles are fetched per topic and merged.
 */
class NewsApiRepository(
    private val apiKey: String,
    private val api: EventRegistryApi,
    private val topicKeywords: List<Interest>
) : NewsRepository {

    private val cache = mutableListOf<Article>()
    private val cacheLock = Any()

    override fun getArticles(): List<Article> = synchronized(cacheLock) { cache.toList() }

    override suspend fun refresh() {
        if (apiKey.isBlank()) return
        val fetched = mutableListOf<Article>()
        val seenUris = ConcurrentHashMap.newKeySet<String>()

        val keywordsToFetch = if (topicKeywords.isEmpty()) {
            listOf(Interest("interest-news", InterestType.Topic, "news"))
        } else {
            topicKeywords
        }

        withContext(Dispatchers.IO) {
            keywordsToFetch.forEach { interest ->
                try {
                    val response = api.getArticles(
                        apiKey = apiKey,
                        keyword = interest.name,
                        lang = "eng",
                        articlesCount = 25,
                        articlesSortBy = "date",
                        articlesSortByAsc = false
                    )
                    if (!response.isSuccessful) return@forEach
                    val body = response.body() ?: return@forEach
                    val results = body.articles?.results ?: emptyList()
                    results.forEach { result ->
                        val uri = result.uri ?: return@forEach
                        if (seenUris.add(uri)) {
                            toArticle(result, interest)?.let { fetched.add(it) }
                        }
                    }
                } catch (_: UnknownHostException) { /* no network */ }
                catch (_: HttpException) { /* server error */ }
                catch (_: Exception) { /* ignore */ }
            }
        }

        val sorted = fetched.distinctBy { it.id }.sortedByDescending { it.publishedAt }
        synchronized(cacheLock) {
            cache.clear()
            cache.addAll(sorted)
        }
    }

    private fun toArticle(r: ArticleResult, topicInterest: Interest): Article? {
        val title = r.title?.takeIf { it.isNotBlank() } ?: return null
        val uri = r.uri ?: return null
        val source = r.source?.title?.takeIf { it.isNotBlank() } ?: "Unknown"
        val url = r.url?.takeIf { it.isNotBlank() } ?: ""
        val summary = r.body?.take(300)?.trim() ?: ""
        val publishedAt = parseDateTime(r.dateTime, r.date)

        return Article(
            id = uri,
            title = title,
            source = source,
            url = url,
            publishedAt = publishedAt,
            summary = summary,
            imageUrl = r.image?.takeIf { it.isNotBlank() } ?: "",
            interests = listOf(topicInterest)
        )
    }

    private fun parseDateTime(dateTime: String?, date: String?): Long {
        if (!dateTime.isNullOrBlank()) {
            try {
                val iso = dateTime.replace("Z", "+00:00")
                java.time.Instant.parse(iso).toEpochMilli().let { return it }
            } catch (_: Exception) { }
        }
        if (!date.isNullOrBlank()) {
            try {
                val atStartOfDay = java.time.LocalDate.parse(date).atStartOfDay(java.time.ZoneOffset.UTC)
                return atStartOfDay.toInstant().toEpochMilli()
            } catch (_: Exception) { }
        }
        return System.currentTimeMillis()
    }
}
