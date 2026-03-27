package com.example.newspulse.data

import android.content.Context
import com.example.newspulse.domain.model.Article
import com.example.newspulse.domain.model.Interest
import com.example.newspulse.domain.model.InterestType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Persists fetched articles per-interest to SharedPreferences as JSON.
 * Each interest gets its own cache entry with a fetch timestamp so the
 * repository can skip the API when the cache is still fresh.
 */
class ArticleDiskCache(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun get(interestId: String): CacheEntry? {
        val json = prefs.getString(articlesKey(interestId), null) ?: return null
        val fetchedAt = prefs.getLong(timestampKey(interestId), 0L)
        if (fetchedAt == 0L) return null
        val articles: List<CachedArticle> = try {
            gson.fromJson(json, object : TypeToken<List<CachedArticle>>() {}.type)
        } catch (_: Exception) {
            return null
        }
        return CacheEntry(articles.map { it.toArticle() }, fetchedAt)
    }

    fun put(interestId: String, articles: List<Article>) {
        val cached = articles.map { CachedArticle.from(it) }
        prefs.edit()
            .putString(articlesKey(interestId), gson.toJson(cached))
            .putLong(timestampKey(interestId), System.currentTimeMillis())
            .apply()
    }

    fun isFresh(interestId: String, ttlMillis: Long = DEFAULT_TTL): Boolean {
        val fetchedAt = prefs.getLong(timestampKey(interestId), 0L)
        return fetchedAt > 0L && System.currentTimeMillis() - fetchedAt < ttlMillis
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun articlesKey(id: String) = "articles_$id"
    private fun timestampKey(id: String) = "ts_$id"

    data class CacheEntry(val articles: List<Article>, val fetchedAt: Long)

    companion object {
        private const val PREFS_NAME = "article_cache"
        const val DEFAULT_TTL = 60L * 60 * 1000       // 1 hour
        const val NEGATIVE_TTL = 4L * 60 * 60 * 1000  // 4 hours for empty results
    }
}

/**
 * Flat serializable representation of [Article] for Gson.
 * [Interest] fields are inlined so Gson doesn't need custom adapters.
 */
private data class CachedArticle(
    val id: String,
    val title: String,
    val source: String,
    val url: String,
    val publishedAt: Long,
    val summary: String,
    val imageUrl: String,
    val interestId: String,
    val interestType: String,
    val interestName: String
) {
    fun toArticle(): Article = Article(
        id = id,
        title = title,
        source = source,
        url = url,
        publishedAt = publishedAt,
        summary = summary,
        imageUrl = imageUrl,
        interests = listOf(
            Interest(
                id = interestId,
                type = runCatching { InterestType.valueOf(interestType) }
                    .getOrDefault(InterestType.Topic),
                name = interestName
            )
        )
    )

    companion object {
        fun from(a: Article): CachedArticle {
            val interest = a.interests.firstOrNull()
            return CachedArticle(
                id = a.id,
                title = a.title,
                source = a.source,
                url = a.url,
                publishedAt = a.publishedAt,
                summary = a.summary,
                imageUrl = a.imageUrl,
                interestId = interest?.id.orEmpty(),
                interestType = interest?.type?.name.orEmpty(),
                interestName = interest?.name.orEmpty()
            )
        }
    }
}
