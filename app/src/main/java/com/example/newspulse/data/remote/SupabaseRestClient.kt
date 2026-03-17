package com.example.newspulse.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SupabaseRestClient(
    private val supabaseUrl: String,
    private val anonKey: String,
    private val accessTokenProvider: (() -> String?)? = null
) {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val ioExecutor = Executors.newSingleThreadExecutor()
    @Volatile var lastError: String? = null
        private set

    fun select(
        table: String,
        columns: String,
        filters: Map<String, String> = emptyMap(),
        order: String? = null,
        limit: Int? = null,
        useUserAuth: Boolean = true
    ): JSONArray {
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

        return executeJsonArray(req, useUserAuth)
    }

    fun insert(
        table: String,
        body: JSONObject,
        onConflict: String? = null,
        upsert: Boolean = false,
        useUserAuth: Boolean = true
    ): Boolean {
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
        return executeOk(req, useUserAuth)
    }

    fun patch(
        table: String,
        body: JSONObject,
        filters: Map<String, String>,
        useUserAuth: Boolean = true
    ): Boolean {
        val url = buildTableUrl(table, filters)
        val req = Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Prefer", "return=minimal")
            .patch(body.toString().toRequestBody(jsonMediaType))
            .build()
        return executeOk(req, useUserAuth)
    }

    fun delete(table: String, filters: Map<String, String>, useUserAuth: Boolean = true): Boolean {
        val url = buildTableUrl(table, filters)
        val req = Request.Builder()
            .url(url)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer $anonKey")
            .header("Prefer", "return=minimal")
            .delete()
            .build()
        return executeOk(req, useUserAuth)
    }

    private fun buildTableUrl(table: String, params: Map<String, String>): String {
        val base = "$supabaseUrl/rest/v1/$table".toHttpUrlOrNull() ?: return "$supabaseUrl/rest/v1/$table"
        val b = base.newBuilder()
        params.forEach { (k, v) -> b.addQueryParameter(k, v) }
        return b.build().toString()
    }

    private fun executeOk(request: Request, useUserAuth: Boolean): Boolean =
        runOnIo(defaultValue = false) {
            val bearer = if (useUserAuth) {
                accessTokenProvider?.invoke().orEmpty().ifBlank { anonKey }
            } else {
                anonKey
            }
            val reqWithAuth = request.newBuilder()
                .header("Authorization", "Bearer $bearer")
                .build()
            return@runOnIo client.newCall(reqWithAuth).execute().use { res ->
                if (res.isSuccessful) {
                    lastError = null
                    true
                } else {
                    val body = res.body?.string().orEmpty()
                    lastError = "HTTP ${res.code}: ${body.take(300)}"
                    false
                }
            }
        }

    private fun executeJsonArray(request: Request, useUserAuth: Boolean): JSONArray =
        runOnIo(defaultValue = JSONArray()) {
            val bearer = if (useUserAuth) {
                accessTokenProvider?.invoke().orEmpty().ifBlank { anonKey }
            } else {
                anonKey
            }
            val reqWithAuth = request.newBuilder()
                .header("Authorization", "Bearer $bearer")
                .build()
            return@runOnIo client.newCall(reqWithAuth).execute().use { res ->
                if (!res.isSuccessful) return@use JSONArray()
                val body = res.body?.string().orEmpty()
                if (body.isBlank()) JSONArray() else JSONArray(body)
            }
        }

    private fun <T> runOnIo(defaultValue: T, block: () -> T): T =
        runCatching { ioExecutor.submit(Callable { block() }).get() }
            .getOrElse {
                lastError = it.message ?: "Unknown network error"
                defaultValue
            }
}
