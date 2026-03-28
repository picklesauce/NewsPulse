package com.example.newspulse.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SupabaseRestClient(
    private val supabaseUrl: String,
    private val anonKey: String,
    private val accessTokenProvider: (() -> String?)? = null
) {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    @Volatile var lastError: String? = null
        private set

    suspend fun select(
        table: String,
        columns: String,
        filters: Map<String, String> = emptyMap(),
        order: String? = null,
        limit: Int? = null,
        useUserAuth: Boolean = true
    ): JSONArray = withContext(Dispatchers.IO) {
        val query = linkedMapOf("select" to columns)
        filters.forEach { (k, v) -> query[k] = v }
        if (!order.isNullOrBlank()) query["order"] = order
        if (limit != null) query["limit"] = limit.toString()

        val url = buildTableUrl(table, query)
        val req = Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .get()
            .build()

        executeJsonArray(req, useUserAuth)
    }

    suspend fun insert(
        table: String,
        body: JSONObject,
        onConflict: String? = null,
        upsert: Boolean = false,
        useUserAuth: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        val query = linkedMapOf<String, String>()
        if (!onConflict.isNullOrBlank()) query["on_conflict"] = onConflict
        val url = buildTableUrl(table, query)
        val prefer = if (upsert) "resolution=merge-duplicates,return=minimal" else "return=minimal"
        val req = Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Prefer", prefer)
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        executeOk(req, useUserAuth)
    }

    suspend fun patch(
        table: String,
        body: JSONObject,
        filters: Map<String, String>,
        useUserAuth: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        val url = buildTableUrl(table, filters)
        val req = Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Prefer", "return=minimal")
            .patch(body.toString().toRequestBody(jsonMediaType))
            .build()
        executeOk(req, useUserAuth)
    }

    suspend fun delete(table: String, filters: Map<String, String>, useUserAuth: Boolean = true): Boolean =
        withContext(Dispatchers.IO) {
            val url = buildTableUrl(table, filters)
            val req = Request.Builder()
                .url(url)
                .header("apikey", anonKey)
                .header("Authorization", "Bearer $anonKey")
                .header("Prefer", "return=minimal")
                .delete()
                .build()
            executeOk(req, useUserAuth)
        }

    private fun buildTableUrl(table: String, params: Map<String, String>): String {
        val base = "$supabaseUrl/rest/v1/$table".toHttpUrlOrNull() ?: return "$supabaseUrl/rest/v1/$table"
        val b = base.newBuilder()
        params.forEach { (k, v) -> b.addQueryParameter(k, v) }
        return b.build().toString()
    }

    private fun executeOk(request: Request, useUserAuth: Boolean): Boolean {
        val bearer = if (useUserAuth) {
            accessTokenProvider?.invoke().orEmpty().ifBlank { anonKey }
        } else {
            anonKey
        }
        val reqWithAuth = request.newBuilder()
            .header("Authorization", "Bearer $bearer")
            .build()
        return runCatching {
            client.newCall(reqWithAuth).execute().use { res ->
                if (res.isSuccessful) {
                    lastError = null
                    true
                } else {
                    val body = res.body?.string().orEmpty()
                    lastError = "HTTP ${res.code}: ${body.take(300)}"
                    false
                }
            }
        }.getOrElse {
            lastError = it.message ?: "Unknown network error"
            false
        }
    }

    private fun executeJsonArray(request: Request, useUserAuth: Boolean): JSONArray {
        val bearer = if (useUserAuth) {
            accessTokenProvider?.invoke().orEmpty().ifBlank { anonKey }
        } else {
            anonKey
        }
        val reqWithAuth = request.newBuilder()
            .header("Authorization", "Bearer $bearer")
            .build()
        return runCatching {
            client.newCall(reqWithAuth).execute().use { res ->
                if (!res.isSuccessful) return@use JSONArray()
                val body = res.body?.string().orEmpty()
                if (body.isBlank()) JSONArray() else JSONArray(body)
            }
        }.getOrElse { JSONArray() }
    }
}
