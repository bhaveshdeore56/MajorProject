package com.example.edai.data.api

import com.example.edai.data.model.WikipediaResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WikipediaApi {
    
    @GET("page/summary/{title}")
    suspend fun getPageSummary(
        @Path("title") title: String
    ): Response<WikipediaResponse>
    
    @GET("page/summary/{title}")
    suspend fun getPageSummaryWithLang(
        @Path("title") title: String,
        @Query("lang") language: String = "en"
    ): Response<WikipediaResponse>
}