package com.example.newspulse.domain.util

import android.util.Log
import com.example.newspulse.BuildConfig
import com.example.newspulse.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min

/**
 * Minimal LLM-based re-ranking:
 * - Input is the heuristic top-N shortlist (N=15 in the caller).
 * - The LLM chooses an ordered top-3 from the shortlist.
 * - If LLM config is missing or parsing fails, falls back to heuristic top-3.
 */
object RelatedArticlesLlmRanker {
    private const val TAG = "RelatedArticlesLlm"
    private const val SHORTLIST_SIZE = 10
    private const val SUMMARY_CHARS = 120
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    @Volatile
    private var lastNetworkFailure: String? = null

    suspend fun pickTop3(baseArticle: Article, shortlist: List<Article>): List<Article> {
        if (shortlist.isEmpty()) return emptyList()
        if (shortlist.size <= 3) return shortlist

        // Precompute a tiny bit of signal to help the model; we do NOT rely on it for correctness.
        val baseInterestNames = baseArticle.interests.map { it.name }.toSet()

        val compactShortlist = shortlist.take(SHORTLIST_SIZE)
        val prompt = buildPrompt(baseArticle = baseArticle, shortlist = compactShortlist, baseInterestNames)
        Log.d(TAG, "LLM prompt=${prompt.take(4000)}")

        val system = "You pick the best 3 items. Return JSON only."
        val schema = """{"top3Indices":[1,5,9]}""".trimIndent()
        val systemContent = "$system The JSON schema is exactly: $schema"

        val canUseOpenAi =
            BuildConfig.LLM_API_KEY.isNotBlank() &&
                BuildConfig.LLM_API_URL.isNotBlank() &&
                BuildConfig.LLM_MODEL.isNotBlank()

        val canUseGemini =
            BuildConfig.GEMINI_API_KEY.isNotBlank() &&
                BuildConfig.GEMINI_MODEL.isNotBlank() &&
                BuildConfig.RELATED_ARTICLES_LLM_ENABLED

        if (!canUseOpenAi && !canUseGemini) {
            val openAiReason =
                "openai(apiKey=${BuildConfig.LLM_API_KEY.isNotBlank()}, apiUrl=${BuildConfig.LLM_API_URL.isNotBlank()}, model=${BuildConfig.LLM_MODEL.isNotBlank()})"
            val geminiReason =
                "gemini(apiKey=${BuildConfig.GEMINI_API_KEY.isNotBlank()}, model=${BuildConfig.GEMINI_MODEL.isNotBlank()}, enabled=${BuildConfig.RELATED_ARTICLES_LLM_ENABLED})"
            Log.d(
                TAG,
                "LLM poll skipped: no LLM provider configured, using heuristic top 3; $openAiReason; $geminiReason"
            )
            return shortlist.take(3)
        }

        Log.d(
            TAG,
                "Polling LLM for related-article rerank: candidates=${compactShortlist.size}, provider=${
                if (canUseOpenAi) "openai-compatible" else "gemini"
            }"
        )

        return withContext(Dispatchers.IO) {
            lastNetworkFailure = null
            val respText = when {
                canUseOpenAi -> {
                    val requestBody = JSONObject().apply {
                        put("model", BuildConfig.LLM_MODEL)
                        put(
                            "messages",
                            JSONArray().apply {
                                put(JSONObject().apply {
                                    put("role", "system")
                                    put("content", systemContent)
                                })
                                put(JSONObject().apply {
                                    put("role", "user")
                                    put("content", prompt)
                                })
                            }
                        )
                        put("temperature", 0)
                        put("max_tokens", 200)
                    }

                    val req = Request.Builder()
                        .url(BuildConfig.LLM_API_URL)
                        .header("Authorization", "Bearer ${BuildConfig.LLM_API_KEY}")
                        .header("Content-Type", "application/json")
                        .post(requestBody.toString().toRequestBody(jsonMediaType))
                        .build()

                    runCatching {
                        client.newCall(req).execute().use { res ->
                            val body = res.body?.string().orEmpty()
                            if (!res.isSuccessful) {
                                lastNetworkFailure =
                                    "openai http=${res.code} body=${body.take(240)}"
                                return@use ""
                            }
                            body
                        }
                    }.getOrElse {
                        lastNetworkFailure = "openai exception=${it.message.orEmpty()}"
                        ""
                    }
                }

                canUseGemini -> {
                    val url =
                        "https://generativelanguage.googleapis.com/v1beta/models/" +
                            "${BuildConfig.GEMINI_MODEL}:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
                    Log.d(TAG, "Gemini request model=${BuildConfig.GEMINI_MODEL}")

                    val fullPrompt = "$systemContent\n\n$prompt"
                    val requestBody = JSONObject().apply {
                        put(
                            "contents",
                            JSONArray().apply {
                                put(
                                    JSONObject().apply {
                                        put(
                                            "parts",
                                            JSONArray().apply {
                                                put(JSONObject().apply { put("text", fullPrompt) })
                                            }
                                        )
                                    }
                                )
                            }
                        )
                        put(
                            "generationConfig",
                            JSONObject().apply {
                                put("temperature", 0)
                                put("maxOutputTokens", 128)
                                put("responseMimeType", "application/json")
                                put(
                                    "responseSchema",
                                    JSONObject().apply {
                                        put("type", "OBJECT")
                                        put(
                                            "properties",
                                            JSONObject().apply {
                                                put(
                                                    "top3Indices",
                                                    JSONObject().apply {
                                                        put("type", "ARRAY")
                                                        put(
                                                            "items",
                                                            JSONObject().apply {
                                                                put("type", "INTEGER")
                                                            }
                                                        )
                                                        put("minItems", 3)
                                                        put("maxItems", 3)
                                                    }
                                                )
                                            }
                                        )
                                        put("required", JSONArray().put("top3Indices"))
                                    }
                                )
                                put(
                                    "thinkingConfig",
                                    JSONObject().apply {
                                        put("thinkingBudget", 0)
                                    }
                                )
                            }
                        )
                    }

                    val req = Request.Builder()
                        .url(url)
                        .header("Content-Type", "application/json")
                        .post(requestBody.toString().toRequestBody(jsonMediaType))
                        .build()

                    runCatching {
                        client.newCall(req).execute().use { res ->
                            val body = res.body?.string().orEmpty()
                            if (!res.isSuccessful) {
                                lastNetworkFailure =
                                    "gemini http=${res.code} body=${body.take(240)}"
                                return@use ""
                            }
                            body
                        }
                    }.getOrElse {
                        lastNetworkFailure = "gemini exception=${it.message.orEmpty()}"
                        ""
                    }
                }

                else -> ""
            }

            if (respText.isBlank()) {
                val reason = lastNetworkFailure ?: "no details"
                Log.w(
                    TAG,
                    "LLM poll failed/empty response, falling back to heuristic top 3; reason=$reason"
                )
                return@withContext shortlist.take(3)
            }
            Log.d(TAG, "LLM raw response=${respText.take(2000)}")

            val outputText = runCatching {
                if (canUseOpenAi) {
                    val root = JSONObject(respText)
                    root
                        .optJSONArray("choices")
                        ?.optJSONObject(0)
                        ?.optJSONObject("message")
                        ?.optString("content")
                        .orEmpty()
                } else {
                    // Gemini: candidates[0].content.parts[0].text
                    val root = JSONObject(respText)
                    root
                        .optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                }
            }.getOrDefault("")
            Log.d(TAG, "LLM extracted output=${outputText.take(2000)}")

            val parseSource = if (outputText.isNotBlank()) outputText else respText
            val indices0 = extractTop3Indices0Based(parseSource, compactShortlist.size)
            val picked = indices0.take(3).toMutableList()
            if (picked.size < 3) {
                Log.w(
                    TAG,
                    "LLM response missing top3 indices, filling from heuristic fallback; outputSnippet=${parseSource.take(320)}"
                )
                // Fill any missing slots with heuristic order.
                for (i in 0 until compactShortlist.size) {
                    if (picked.size >= 3) break
                    if (i !in picked) picked.add(i)
                }
            }
            val selected = picked.take(3).map { compactShortlist[it] }
            Log.d(TAG, "LLM rerank selected article ids=${selected.joinToString(",") { it.id }}")
            selected
        }
    }

    private fun buildPrompt(
        baseArticle: Article,
        shortlist: List<Article>,
        baseInterestNames: Set<String>
    ): String {
        val interestsText = baseArticle.interests.joinToString(", ") { it.name }
        val baseTitle = baseArticle.title.trim()
        val baseSource = baseArticle.source.trim()

        val sb = StringBuilder()
        sb.appendLine("Base article")
        sb.appendLine("Title: ${baseTitle}")
        sb.appendLine("Source: ${baseSource}")
        sb.appendLine("Interests: ${interestsText.ifBlank { "—" }}")
        sb.appendLine()
        sb.appendLine("Heuristic shortlist (numbered 1..${shortlist.size}, already in best-to-worst order):")

        shortlist.forEachIndexed { idx, a ->
            val shared = a.interests.count { it.name in baseInterestNames }
            val summary = a.summary.replace(Regex("\\s+"), " ").trim()
            val summaryShort = summary.take(min(summary.length, SUMMARY_CHARS))
            sb.appendLine(
                "${idx + 1}. Title: ${a.title.trim()} | Source: ${a.source.trim()} | SharedInterestCount: $shared | Summary: ${if (summaryShort.isBlank()) "—" else summaryShort}"
            )
        }

        sb.appendLine()
        sb.appendLine(
            "Choose the best 3 related articles (most relevant to the base). " +
                "Return ONLY strict JSON in this exact shape: {\"top3Indices\":[1,2,3]}. " +
                "Use 1-based indices, no markdown, no extra keys."
        )
        return sb.toString()
    }

    /**
     * @return up to 3 unique indices, 0-based into [shortlist].
     */
    private fun extractTop3Indices0Based(responseText: String, shortlistSize: Int): List<Int> {
        if (responseText.isBlank()) return emptyList()

        val cleaned = cleanModelResponse(responseText)

        // 1) Try JSON parsing first (best case).
        val jsonCandidate = cleaned.let { t ->
            val start = t.indexOf('{')
            val end = t.lastIndexOf('}')
            if (start >= 0 && end > start) t.substring(start, end + 1) else ""
        }

        if (jsonCandidate.isNotBlank()) {
            val parsed = runCatching { JSONObject(jsonCandidate) }.getOrNull()
            if (parsed != null) {
                val array =
                    parsed.optJSONArray("top3Indices")
                        ?: parsed.optJSONArray("top3")
                        ?: parsed.optJSONArray("top_3")
                if (array != null) {
                    val out = mutableListOf<Int>()
                    for (i in 0 until array.length()) {
                        val v = array.opt(i)
                        val n = when (v) {
                            is Number -> v.toInt()
                            is String -> v.toIntOrNull()
                            else -> null
                        }
                        if (n != null) {
                            val idx0 = n - 1 // convert 1-based to 0-based
                            if (idx0 in 0 until shortlistSize && idx0 !in out) out.add(idx0)
                            if (out.size >= 3) break
                        }
                    }
                    if (out.isNotEmpty()) return out
                }
            }
        }

        // 2) Fallback: extract integers and interpret as 1-based indices.
        val numbers = Regex("(\\d+)").findAll(cleaned)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .map { it - 1 } // to 0-based
            .filter { it in 0 until shortlistSize }

        val out = mutableListOf<Int>()
        for (idx0 in numbers) {
            if (idx0 !in out) out.add(idx0)
            if (out.size >= 3) break
        }
        return out
    }

    /**
     * Handles common LLM wrappers like:
     * - "Here is the JSON requested:"
     * - fenced markdown blocks with/without json language tag.
     */
    private fun cleanModelResponse(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```")) {
            text = text.removePrefix("```")
            if (text.startsWith("json")) {
                text = text.removePrefix("json").trimStart('\n', '\r', ' ')
            }
            text = text.removeSuffix("```").trim()
        } else if (text.contains("```")) {
            val fenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```")
            val match = fenceRegex.find(text)
            if (match != null) {
                text = match.groupValues[1].trim()
            }
        }
        return text
    }
}

