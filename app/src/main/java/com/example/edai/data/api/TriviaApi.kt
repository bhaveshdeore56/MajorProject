package com.example.edai.data.api

import com.example.edai.data.model.TriviaResponse
import com.example.edai.data.model.OverpassResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {
    
    @GET("api.php")
    suspend fun getTriviaQuestions(
        @Query("amount") amount: Int = 5,
        @Query("category") category: Int? = null, // 22=Geography, 23=History, 9=General Knowledge
        @Query("type") type: String = "multiple",
        @Query("difficulty") difficulty: String? = null
    ): Response<TriviaResponse>
}

interface OverpassApi {
    
    @GET("interpreter")
    suspend fun queryPlaces(
        @Query("data") data: String
    ): Response<OverpassResponse>
}