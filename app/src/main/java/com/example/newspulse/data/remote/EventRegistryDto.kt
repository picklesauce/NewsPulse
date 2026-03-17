package com.example.newspulse.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Event Registry / NewsAPI.ai getArticles response.
 * See https://eventregistry.org / https://newsapi.ai
 */
data class GetArticlesResponse(
    @SerializedName("articles") val articles: ArticlesWrapper? = null,
    @SerializedName("error") val error: ApiError? = null
)

data class ArticlesWrapper(
    @SerializedName("results") val results: List<ArticleResult>? = null
)

data class ArticleResult(
    @SerializedName("uri") val uri: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("dateTime") val dateTime: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("source") val source: SourceResult? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("lang") val lang: String? = null
)

data class SourceResult(
    @SerializedName("title") val title: String? = null,
    @SerializedName("uri") val uri: String? = null
)

data class ApiError(
    @SerializedName("error") val message: String? = null
)
