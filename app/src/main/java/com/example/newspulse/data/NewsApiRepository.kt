package com.example.newspulse.data

import com.example.newspulse.data.remote.ArticleResult
import com.example.newspulse.data.remote.EventRegistryApi
import com.example.newspulse.domain.NewsRepository
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.example.newspulse.domain.util.FeedRanker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Fetches articles from Event Registry / NewsAPI.ai with a two-layer cache:
 *
 * 1. **Disk cache** ([ArticleDiskCache]) -- per-interest, survives app restarts,
 *    checked first so the feed loads instantly on cold start.
 * 2. **In-memory cache** -- the merged/sorted list served by [getArticles].
 *
 * On [refresh], each interest is skipped if its disk cache is still fresh (TTL-based).
 * Empty results are "negative-cached" with a longer TTL to avoid wasting API calls.
 * On explicit pull-to-refresh, pass [forceNetwork] = true to bypass TTL.
 */
class NewsApiRepository(
    private val apiKey: String,
    private val api: EventRegistryApi,
    private val catalogProvider: () -> List<Interest>,
    private val followedIdsProvider: () -> Set<String>,
    private val diskCache: ArticleDiskCache? = null
) : NewsRepository {

    private val memoryCache = mutableListOf<Article>()
    private val cacheLock = Any()

    override fun getArticles(): List<Article> = synchronized(cacheLock) { memoryCache.toList() }

    /**
     * Loads articles for every followed interest.
     * Serves from disk cache when fresh; fetches from API otherwise.
     * [forceNetwork] bypasses TTL (used for explicit pull-to-refresh).
     */
    override suspend fun refresh() { doRefresh(forceNetwork = false) }

    override suspend fun forceRefresh() { doRefresh(forceNetwork = true) }

    override suspend fun searchByKeyword(keyword: String): List<Article> {
        if (apiKey.isBlank() || keyword.isBlank()) return emptyList()
        val interest = Interest(
            id = "discover-${keyword.lowercase().replace(" ", "-")}",
            type = InterestType.Topic,
            name = keyword
        )
        val dateStart = LocalDate.now().minusDays(7)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
        return withContext(Dispatchers.IO) {
            loadForInterest(interest, dateStart, forceNetwork = false)
        }
    }

    private suspend fun doRefresh(forceNetwork: Boolean) {
        if (apiKey.isBlank()) return

        val allCatalog = catalogProvider()
        val followedIds = followedIdsProvider()
        val followed = allCatalog.filter { it.id in followedIds }
        val toFetch = followed.ifEmpty {
            allCatalog.filter { it.type == InterestType.Topic }.take(3)
        }

        val dateStart = LocalDate.now().minusDays(7)
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        val buckets = mutableMapOf<String, List<Article>>()
        withContext(Dispatchers.IO) {
            toFetch.forEach { interest ->
                val articles = loadForInterest(interest, dateStart, forceNetwork)
                if (articles.isNotEmpty()) {
                    buckets[interest.name] = articles
                }
            }
        }

        var ranked = FeedRanker.rank(buckets)

        if (ranked.isEmpty()) {
            ranked = withContext(Dispatchers.IO) {
                fetchFallbackArticles(dateStart)
            }
        }

        synchronized(cacheLock) {
            memoryCache.clear()
            memoryCache.addAll(ranked)
        }
    }

    private val fallbackKeywords = listOf("trending news", "world news today", "breaking news")

    private suspend fun fetchFallbackArticles(dateStart: String): List<Article> {
        for (keyword in fallbackKeywords) {
            val interest = Interest(
                id = "fallback-${keyword.replace(" ", "-")}",
                type = InterestType.Topic,
                name = keyword
            )
            val articles = fetchFromApi(interest, dateStart)
            if (articles.isNotEmpty()) {
                diskCache?.put(interest.id, articles)
                return articles.sortedByDescending { it.publishedAt }
            }
        }
        return emptyList()
    }

    /**
     * Returns articles for a single interest, using disk cache when available
     * and fresh, or fetching from the API and writing back to cache.
     */
    private suspend fun loadForInterest(
        interest: Interest,
        dateStart: String,
        forceNetwork: Boolean
    ): List<Article> {
        if (!forceNetwork && diskCache != null) {
            val ttl = if (diskCache.get(interest.id)?.articles.isNullOrEmpty()) {
                ArticleDiskCache.NEGATIVE_TTL
            } else {
                ArticleDiskCache.DEFAULT_TTL
            }
            if (diskCache.isFresh(interest.id, ttl)) {
                return diskCache.get(interest.id)?.articles ?: emptyList()
            }
        }

        val articles = fetchFromApi(interest, dateStart)
        diskCache?.put(interest.id, articles)
        return articles
    }

    private suspend fun fetchFromApi(interest: Interest, dateStart: String): List<Article> {
        return try {
            val keyword = buildKeyword(interest)
            val response = api.getArticles(
                apiKey = apiKey,
                keyword = keyword,
                lang = "eng",
                articlesCount = 20,
                articlesSortBy = "date",
                articlesSortByAsc = false,
                dateStart = dateStart
            )
            if (!response.isSuccessful) return emptyList()
            val results = response.body()?.articles?.results ?: emptyList()
            results.mapNotNull { toArticle(it, interest) }
        } catch (_: UnknownHostException) { emptyList() }
        catch (_: HttpException) { emptyList() }
        catch (_: Exception) { emptyList() }
    }

    /**
     * Converts an [Interest] into an effective Event Registry keyword.
     * Country names alone return geographic/travel content, so we append "news".
     * Person, Company, and Topic names work well as-is for keyword search.
     */
    private fun buildKeyword(interest: Interest): String = when (interest.type) {
        InterestType.Country -> "${interest.name} news"
        InterestType.Person  -> interest.name
        InterestType.Company -> interest.name
        InterestType.Topic   -> interest.name
    }

    private fun toArticle(r: ArticleResult, interest: Interest): Article? {
        val title = r.title?.takeIf { it.isNotBlank() } ?: return null
        val uri = r.uri ?: return null
        val imageUrl = r.image?.takeIf { it.isNotBlank() } ?: return null
        val source = r.source?.title?.takeIf { it.isNotBlank() } ?: "Unknown"
        val url = r.url?.takeIf { it.isNotBlank() } ?: ""
        val summary = r.body?.trim() ?: ""
        val publishedAt = parseDateTime(r.dateTime, r.date)

        return Article(
            id = uri,
            title = title,
            source = source,
            url = url,
            publishedAt = publishedAt,
            summary = summary,
            imageUrl = imageUrl,
            interests = listOf(interest)
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
