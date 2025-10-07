package com.example.edai.data.network

import com.example.edai.data.api.NominatimApi
import com.example.edai.data.api.WikipediaApi
import com.example.edai.data.api.TriviaApi
import com.example.edai.data.api.OverpassApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/api/rest_v1/"
    private const val TRIVIA_BASE_URL = "https://opentdb.com/"
    private const val OVERPASS_BASE_URL = "https://overpass-api.de/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "EdaiApp/1.0 (Educational Travel App)")
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .build()
    
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val nominatimApi: NominatimApi by lazy {
        createRetrofit(NOMINATIM_BASE_URL).create(NominatimApi::class.java)
    }
    
    val wikipediaApi: WikipediaApi by lazy {
        createRetrofit(WIKIPEDIA_BASE_URL).create(WikipediaApi::class.java)
    }
    
    val triviaApi: TriviaApi by lazy {
        createRetrofit(TRIVIA_BASE_URL).create(TriviaApi::class.java)
    }
    
    val overpassApi: OverpassApi by lazy {
        createRetrofit(OVERPASS_BASE_URL).create(OverpassApi::class.java)
    }
}