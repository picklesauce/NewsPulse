package com.example.newspulse.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface EventRegistryApi {

    @GET("api/v1/article/getArticles")
    suspend fun getArticles(
        @Query("apiKey") apiKey: String,
        @Query("keyword") keyword: String,
        @Query("lang") lang: String = "eng",
        @Query("articlesCount") articlesCount: Int = 30,
        @Query("articlesSortBy") articlesSortBy: String = "date",
        @Query("articlesSortByAsc") articlesSortByAsc: Boolean = false,
        @Query("keywordsLoc") keywordsLoc: String = "body,title",
        @Query("isDuplicateFilter") isDuplicateFilter: String = "skipDuplicates",
        @Query("dateStart") dateStart: String? = null
    ): Response<GetArticlesResponse>
}
