package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.SavedArticlesRepository
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

class SupabaseSavedArticlesRepository(
    private val client: SupabaseRestClient,
    private val userIdProvider: () -> String?
) : SavedArticlesRepository {
    private val saved = MutableStateFlow<List<Article>>(emptyList())

    init {
        refreshSaved()
    }

    override fun getSavedArticles(): Flow<List<Article>> = saved.asStateFlow()

    override fun saveArticle(article: Article) {
        val userId = userIdProvider() ?: return
        ensureArticleExists(article)
        val row = JSONObject()
            .put("id", UUID.randomUUID().toString())
            .put("user_id", userId)
            .put("article_id", article.id)
        client.insert(table = "saved_articles", body = row)
        refreshSaved()
    }

    override fun removeArticle(article: Article) {
        val userId = userIdProvider() ?: return
        client.delete(
            table = "saved_articles",
            filters = mapOf(
                "user_id" to "eq.$userId",
                "article_id" to "eq.${article.id}"
            )
        )
        refreshSaved()
    }

    private fun refreshSaved() {
        val userId = userIdProvider() ?: run {
            saved.value = emptyList()
            return
        }
        val savedRows = client.select(
            table = "saved_articles",
            columns = "article_id,saved_at",
            filters = mapOf("user_id" to "eq.$userId"),
            order = "saved_at.desc"
        )
        val idsInOrder = buildList {
            for (i in 0 until savedRows.length()) {
                val id = savedRows.optJSONObject(i)?.optString("article_id").orEmpty()
                if (id.isNotBlank()) add(id)
            }
        }
        if (idsInOrder.isEmpty()) {
            saved.value = emptyList()
            return
        }

        val inClause = idsInOrder.joinToString(",") { "\"$it\"" }
        val articles = client.select(
            table = "articles",
            columns = "id,title,source,url,published_at,summary,image_url",
            filters = mapOf("id" to "in.($inClause)")
        )
        val byId = mutableMapOf<String, Article>()
        for (i in 0 until articles.length()) {
            val r = articles.optJSONObject(i) ?: continue
            val id = r.optString("id")
            if (id.isBlank()) continue
            byId[id] = Article(
                id = id,
                title = r.optString("title"),
                source = r.optString("source"),
                url = r.optString("url"),
                publishedAt = r.optLong("published_at"),
                summary = r.optString("summary"),
                imageUrl = r.optString("image_url"),
                interests = emptyList()
            )
        }
        saved.value = idsInOrder.mapNotNull { byId[it] }
    }

    private fun ensureArticleExists(article: Article) {
        val body = JSONObject()
            .put("id", article.id)
            .put("title", article.title)
            .put("source", article.source.ifBlank { "Unknown" })
            .put("url", article.url)
            .put("published_at", article.publishedAt)
            .put("summary", article.summary)
            .put("image_url", article.imageUrl)
        client.insert(
            table = "articles",
            body = body,
            onConflict = "id",
            upsert = true
        )
    }
}
