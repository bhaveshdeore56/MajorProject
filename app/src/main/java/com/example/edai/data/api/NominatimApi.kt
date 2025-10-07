package com.example.edai.data.api

import com.example.edai.data.model.NominatimResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("zoom") zoom: Int = 18
    ): Response<NominatimResult>
    
    @GET("search")
    suspend fun forwardGeocode(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("limit") limit: Int = 5
    ): Response<List<NominatimResult>>
}
