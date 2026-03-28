package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.ReadingHistoryRepository
import com.example.newspulse.domain.model.ReadingHistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class SupabaseReadingHistoryRepository(
    private val client: SupabaseRestClient,
    private val userIdProvider: () -> String?
) : ReadingHistoryRepository {

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cacheLock = Any()
    private var historyCache: List<ReadingHistoryItem> = emptyList()

    override fun getReadingHistory(): List<ReadingHistoryItem> = synchronized(cacheLock) {
        historyCache.toList()
    }

    suspend fun refreshFromRemote() {
        val userId = userIdProvider() ?: run {
            synchronized(cacheLock) { historyCache = emptyList() }
            return
        }
        val rows = client.select(
            table = "reading_history",
            columns = "article_id,title,read_at_millis",
            filters = mapOf("user_id" to "eq.$userId"),
            order = "read_at_millis.desc",
            limit = 50
        )
        val list = buildList {
            for (i in 0 until rows.length()) {
                val r = rows.optJSONObject(i) ?: continue
                val articleId = r.optString("article_id")
                val title = r.optString("title")
                val readAt = r.optLong("read_at_millis")
                if (articleId.isNotBlank() && title.isNotBlank()) {
                    add(
                        ReadingHistoryItem(
                            articleId = articleId,
                            title = title,
                            readAtMillis = readAt
                        )
                    )
                }
            }
        }.distinctBy { it.articleId }
        synchronized(cacheLock) { historyCache = list }
    }

    override fun addToHistory(articleId: String, title: String) {
        val userId = userIdProvider() ?: return
        val now = System.currentTimeMillis()
        val item = ReadingHistoryItem(articleId = articleId, title = title, readAtMillis = now)
        synchronized(cacheLock) {
            historyCache = listOf(item) + historyCache.filter { it.articleId != articleId }
        }
        ioScope.launch {
            ensureArticleExists(articleId = articleId, title = title)
            client.delete(
                table = "reading_history",
                filters = mapOf("user_id" to "eq.$userId", "article_id" to "eq.$articleId")
            )
            val row = JSONObject()
                .put("id", UUID.randomUUID().toString())
                .put("user_id", userId)
                .put("article_id", articleId)
                .put("title", title)
                .put("read_at_millis", now)
            client.insert(table = "reading_history", body = row)
        }
    }

    private suspend fun ensureArticleExists(articleId: String, title: String) {
        val body = JSONObject()
            .put("id", articleId)
            .put("title", title.ifBlank { articleId })
            .put("source", "Unknown")
            .put("url", "")
            .put("published_at", System.currentTimeMillis())
            .put("summary", "")
            .put("image_url", "")
        client.insert(
            table = "articles",
            body = body,
            onConflict = "id",
            upsert = true
        )
    }
}
