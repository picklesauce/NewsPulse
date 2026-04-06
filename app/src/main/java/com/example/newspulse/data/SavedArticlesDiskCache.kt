package com.example.newspulse.data

import android.content.Context
import com.example.newspulse.domain.model.Article
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



// fill article data for saved articles moved to saved preferences
class SavedArticlesDiskCache(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun get(articleId: String): Article? {
        val json = prefs.getString(key(articleId), null) ?: return null
        return try {
            val cached: CachedSavedArticle = gson.fromJson(json, CachedSavedArticle::class.java)
            cached.toArticle()
        } catch (_: Exception) {
            null
        }
    }

    fun put(article: Article) {
        val json = gson.toJson(CachedSavedArticle.from(article))
        prefs.edit().putString(key(article.id), json).apply()
    }

    fun remove(articleId: String) {
        prefs.edit().remove(key(articleId)).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun key(id: String) = "saved_$id"

    companion object {
        private const val PREFS_NAME = "saved_articles_cache"
    }
}

private data class CachedSavedArticle(
    val id: String,
    val title: String,
    val source: String,
    val url: String,
    val publishedAt: Long,
    val summary: String,
    val imageUrl: String
) {
    fun toArticle(): Article = Article(
        id = id,
        title = title,
        source = source,
        url = url,
        publishedAt = publishedAt,
        summary = summary,
        imageUrl = imageUrl,
        interests = emptyList()
    )

    companion object {
        fun from(a: Article) = CachedSavedArticle(
            id = a.id,
            title = a.title,
            source = a.source,
            url = a.url,
            publishedAt = a.publishedAt,
            summary = a.summary,
            imageUrl = a.imageUrl
        )
    }
}
