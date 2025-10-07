package com.example.edai.data.repository

import android.content.Context
import com.example.edai.data.model.GeminiPlaceInfo
import com.example.edai.data.model.GeminiQuizResponse
import com.example.edai.data.model.GeminiResponse
import com.example.edai.data.model.PopularPlace
import com.example.edai.data.model.PopularPlacesResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class PopularPlacesRepository(private val context: Context) {
    
    private val gson = Gson()
    private var cachedPlaces: List<PopularPlace>? = null
    private val geminiRepository = GeminiRepository(context)
    
    suspend fun getPopularPlaces(): Result<List<PopularPlace>> = withContext(Dispatchers.IO) {
        try {
            // Return cached data if available
            cachedPlaces?.let { 
                return@withContext Result.success(it)
            }
            
            // Load from assets
            val jsonString = context.assets.open("pune_places.json").use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
            
            val response = gson.fromJson(jsonString, PopularPlacesResponse::class.java)
            cachedPlaces = response.places
            
            Result.success(response.places)
        } catch (e: IOException) {
            Result.failure(Exception("Failed to load popular places data", e))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to parse popular places data", e))
        }
    }
    
    suspend fun getPlaceById(id: Int): Result<PopularPlace?> = withContext(Dispatchers.IO) {
        try {
            val places = getPopularPlaces().getOrThrow()
            val place = places.find { it.id == id }
            Result.success(place)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPlacesByCategory(category: String): Result<List<PopularPlace>> = withContext(Dispatchers.IO) {
        try {
            val places = getPopularPlaces().getOrThrow()
            val filteredPlaces = places.filter { it.category.equals(category, ignoreCase = true) }
            Result.success(filteredPlaces)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCategories(): List<String> {
        return cachedPlaces?.map { it.category }?.distinct() ?: emptyList()
    }
    
    /**
     * Get enhanced place information using Gemini AI
     */
    suspend fun getEnhancedPlaceInfo(place: PopularPlace): Result<GeminiPlaceInfo> = withContext(Dispatchers.IO) {
        try {
            when (val response = geminiRepository.getEnhancedPlaceInfo(
                placeName = place.name,
                category = place.category
            )) {
                is GeminiResponse.Success -> Result.success(response.data)
                is GeminiResponse.Error -> Result.failure(Exception(response.message))
                is GeminiResponse.Loading -> Result.failure(Exception("Request in progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate dynamic quiz for a place using Gemini AI
     */
    suspend fun generateDynamicQuiz(
        place: PopularPlace,
        difficulty: String = "medium",
        questionCount: Int = 5
    ): Result<GeminiQuizResponse> = withContext(Dispatchers.IO) {
        try {
            when (val response = geminiRepository.generateQuizForPlace(
                placeName = place.name,
                category = place.category,
                existingInfo = place.description,
                difficulty = difficulty,
                questionCount = questionCount
            )) {
                is GeminiResponse.Success -> Result.success(response.data)
                is GeminiResponse.Error -> Result.failure(Exception(response.message))
                is GeminiResponse.Loading -> Result.failure(Exception("Request in progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if Gemini AI is available
     */
    fun isGeminiAvailable(): Boolean {
        return geminiRepository.isGeminiAvailable()
    }
    
    /**
     * Get Gemini status message
     */
    fun getGeminiStatus(): String {
        return geminiRepository.getGeminiStatus()
    }
}
