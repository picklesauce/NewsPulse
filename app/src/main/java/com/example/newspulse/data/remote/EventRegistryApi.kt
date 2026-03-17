package com.example.newspulse.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Event Registry / NewsAPI.ai REST API.
 * Base URL: https://eventregistry.org
 */
interface EventRegistryApi {

    @GET("api/v1/article/getArticles")
    suspend fun getArticles(
        @Query("apiKey") apiKey: String,
        @Query("keyword") keyword: String,
        @Query("lang") lang: String = "eng",
        @Query("articlesCount") articlesCount: Int = 30,
        @Query("articlesSortBy") articlesSortBy: String = "date",
        @Query("articlesSortByAsc") articlesSortByAsc: Boolean = false
    ): Response<GetArticlesResponse>
}
