package com.example.newspulse.data

import com.example.newspulse.data.remote.SupabaseRestClient
import com.example.newspulse.domain.ReadingHistoryRepository
import com.example.newspulse.domain.model.ReadingHistoryItem
import org.json.JSONObject
import java.util.UUID

class SupabaseReadingHistoryRepository(
    private val client: SupabaseRestClient,
    private val userIdProvider: () -> String?
) : ReadingHistoryRepository {

    override fun getReadingHistory(): List<ReadingHistoryItem> {
        val userId = userIdProvider() ?: return emptyList()
        val rows = client.select(
            table = "reading_history",
            columns = "article_id,title,read_at_millis",
            filters = mapOf("user_id" to "eq.$userId"),
            order = "read_at_millis.desc",
            limit = 50
        )
        return buildList {
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
        }
    }

    override fun addToHistory(articleId: String, title: String) {
        val userId = userIdProvider() ?: return
        ensureArticleExists(articleId = articleId, title = title)
        val now = System.currentTimeMillis()
        val row = JSONObject()
            .put("id", UUID.randomUUID().toString())
            .put("user_id", userId)
            .put("article_id", articleId)
            .put("title", title)
            .put("read_at_millis", now)
        client.insert(table = "reading_history", body = row)
    }

    private fun ensureArticleExists(articleId: String, title: String) {
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
