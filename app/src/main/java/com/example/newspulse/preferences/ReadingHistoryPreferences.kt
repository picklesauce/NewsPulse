package com.example.newspulse.preferences

import android.content.Context
import android.content.SharedPreferences

data class ReadingHistoryItem(
    val title: String,
    val readAtMillis: Long
)

class ReadingHistoryPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getReadingHistory(): List<ReadingHistoryItem> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(ENTRY_DELIMITER)
            .mapNotNull { entry ->
                val parts = entry.split(FIELD_DELIMITER)
                if (parts.size == 2) {
                    ReadingHistoryItem(
                        title = parts[0],
                        readAtMillis = parts[1].toLongOrNull() ?: 0L
                    )
                } else null
            }
            .sortedByDescending { it.readAtMillis }
            .take(MAX_ITEMS)
    }

    fun addToHistory(title: String) {
        val existing = getReadingHistory()
        val filtered = existing.filter { it.title != title }
        val updated = listOf(ReadingHistoryItem(title, System.currentTimeMillis())) + filtered
        val encoded = updated.take(MAX_ITEMS).joinToString(ENTRY_DELIMITER) { "${it.title}$FIELD_DELIMITER${it.readAtMillis}" }
        prefs.edit().putString(KEY_HISTORY, encoded).apply()
    }

    companion object {
        private const val PREFS_NAME = "reading_history"
        private const val KEY_HISTORY = "history"
        private const val ENTRY_DELIMITER = ";"
        private const val FIELD_DELIMITER = "|"
        private const val MAX_ITEMS = 50
    }
}
