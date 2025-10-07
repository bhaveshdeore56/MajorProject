package com.example.edai.data.repository

import android.content.Context
import com.example.edai.data.api.GeminiAiService
import com.example.edai.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(private val context: Context) {
    
    private val geminiService = GeminiAiService(context)
    
    /**
     * Generate enhanced place information
     */
    suspend fun getEnhancedPlaceInfo(
        placeName: String,
        category: String,
        location: String = "Pune, Maharashtra, India"
    ): GeminiResponse<GeminiPlaceInfo> = withContext(Dispatchers.IO) {
        if (!geminiService.isConfigured()) {
            return@withContext GeminiResponse.Error("Gemini AI not configured. Please add your API key in Settings.")
        }
        
        val request = PlaceInfoRequest(
            placeName = placeName,
            category = category,
            location = location
        )
        
        geminiService.generatePlaceInfo(request)
    }
    
    /**
     * Generate quiz questions for a specific place
     */
    suspend fun generateQuizForPlace(
        placeName: String,
        category: String,
        existingInfo: String? = null,
        difficulty: String = "medium",
        questionCount: Int = 5
    ): GeminiResponse<GeminiQuizResponse> = withContext(Dispatchers.IO) {
        if (!geminiService.isConfigured()) {
            return@withContext GeminiResponse.Error("Gemini AI not configured. Please add your API key in Settings.")
        }
        
        val request = QuizGenerationRequest(
            placeName = placeName,
            category = category,
            difficulty = difficulty,
            numberOfQuestions = questionCount,
            existingInfo = existingInfo
        )
        
        geminiService.generateQuiz(request)
    }
    
    /**
     * Enhance existing place description
     */
    suspend fun enhanceExistingDescription(
        placeName: String,
        existingDescription: String
    ): GeminiResponse<String> = withContext(Dispatchers.IO) {
        if (!geminiService.isConfigured()) {
            return@withContext GeminiResponse.Error("Gemini AI not configured. Please add your API key in Settings.")
        }
        
        geminiService.enhanceDescription(placeName, existingDescription)
    }
    
    /**
     * Generate quiz questions only
     */
    suspend fun generateQuizQuestions(
        placeName: String,
        category: String,
        difficulty: String = "medium",
        questionCount: Int = 5
    ): GeminiResponse<List<GeminiQuizQuestion>> = withContext(Dispatchers.IO) {
        if (!geminiService.isConfigured()) {
            return@withContext GeminiResponse.Error("Gemini AI not configured. Please add your API key in Settings.")
        }
        
        geminiService.generateCustomQuiz(
            placeName = placeName,
            category = category,
            difficulty = difficulty,
            questionCount = questionCount
        )
    }
    
    /**
     * Check if Gemini AI is available
     */
    fun isGeminiAvailable(): Boolean {
        return geminiService.isConfigured()
    }
    
    /**
     * Get Gemini configuration status
     */
    fun getGeminiStatus(): String {
        return geminiService.getConfigurationStatus()
    }
    
    /**
     * Generate comprehensive place data (info + quiz)
     */
    suspend fun generateComprehensivePlaceData(
        placeName: String,
        category: String,
        generateQuiz: Boolean = true,
        quizDifficulty: String = "medium",
        quizQuestionCount: Int = 5
    ): GeminiResponse<Pair<GeminiPlaceInfo, GeminiQuizResponse?>> = withContext(Dispatchers.IO) {
        
        if (!geminiService.isConfigured()) {
            return@withContext GeminiResponse.Error("Gemini AI not configured. Please add your API key in Settings.")
        }
        
        try {
            // Generate place information first
            val placeInfoResponse = getEnhancedPlaceInfo(placeName, category)
            
            if (placeInfoResponse is GeminiResponse.Error) {
                return@withContext GeminiResponse.Error("Failed to generate place info: ${placeInfoResponse.message}")
            }
            
            val placeInfo = (placeInfoResponse as GeminiResponse.Success).data
            
            // Generate quiz if requested
            val quizResponse = if (generateQuiz) {
                val existingInfo = "${placeInfo.description} ${placeInfo.historicalSignificance}"
                when (val quiz = generateQuizForPlace(
                    placeName = placeName,
                    category = category,
                    existingInfo = existingInfo,
                    difficulty = quizDifficulty,
                    questionCount = quizQuestionCount
                )) {
                    is GeminiResponse.Success -> quiz.data
                    is GeminiResponse.Error -> {
                        // Log error but don't fail the entire request
                        null
                    }
                    is GeminiResponse.Loading -> null
                }
            } else {
                null
            }
            
            GeminiResponse.Success(Pair(placeInfo, quizResponse))
            
        } catch (e: Exception) {
            GeminiResponse.Error("Failed to generate comprehensive data: ${e.message}", e)
        }
    }
}