package com.example.edai.data.api

import android.content.Context
import com.example.edai.data.model.*
import com.example.edai.utils.Constants
import com.example.edai.utils.ApiKeyManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.withTimeout

class GeminiAiService(private val context: Context) {
    
    private val gson = Gson()
    private val apiKeyManager = ApiKeyManager.getInstance(context)
    
    private fun getGenerativeModel(): GenerativeModel? {
        val apiKey = apiKeyManager.getGeminiApiKey()
        
        return if (apiKeyManager.isGeminiConfigured()) {
            GenerativeModel(
                modelName = Constants.GEMINI_MODEL,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 2048
                },
                safetySettings = listOf(
                    SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                    SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                    SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                    SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
                )
            )
        } else {
            null
        }
    }
    
    /**
     * Generate comprehensive place information using Gemini AI
     */
    suspend fun generatePlaceInfo(request: PlaceInfoRequest): GeminiResponse<GeminiPlaceInfo> {
        return try {
            val model = getGenerativeModel() 
                ?: return GeminiResponse.Error("Gemini AI not configured. Please set up your API key in Settings.")
            
            withTimeout(Constants.QUIZ_GENERATION_TIMEOUT) {
                val prompt = GeminiPrompts.generatePlaceInfoPrompt(request)
                val response = model.generateContent(prompt)
                val responseText = response.text ?: throw Exception("Empty response from Gemini")
                
                // Try to parse JSON response
                try {
                    val placeInfo = gson.fromJson(responseText, GeminiPlaceInfo::class.java)
                    GeminiResponse.Success(placeInfo)
                } catch (e: JsonSyntaxException) {
                    // If JSON parsing fails, create a fallback response
                    val fallbackInfo = GeminiPlaceInfo(
                        name = request.placeName,
                        description = responseText,
                        historicalSignificance = "Historical information not available in structured format",
                        interestingFacts = listOf("Generated content available in description"),
                        bestTimeToVisit = "Information available in description",
                        nearbyAttractions = emptyList()
                    )
                    GeminiResponse.Success(fallbackInfo)
                }
            }
        } catch (e: Exception) {
            GeminiResponse.Error("Failed to generate place information: ${e.message}", e)
        }
    }
    
    /**
     * Generate quiz questions using Gemini AI
     */
    suspend fun generateQuiz(request: QuizGenerationRequest): GeminiResponse<GeminiQuizResponse> {
        return try {
            val model = getGenerativeModel() 
                ?: return GeminiResponse.Error("Gemini AI not configured. Please set up your API key in Settings.")
            
            withTimeout(Constants.QUIZ_GENERATION_TIMEOUT) {
                val prompt = GeminiPrompts.generateQuizPrompt(request)
                val response = model.generateContent(prompt)
                val responseText = response.text ?: throw Exception("Empty response from Gemini")
                
                // Clean up the response text (remove markdown formatting if present)
                val cleanedResponse = responseText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()
                
                try {
                    val quizResponse = gson.fromJson(cleanedResponse, GeminiQuizResponse::class.java)
                    
                    // Validate the response
                    if (quizResponse.questions.isEmpty()) {
                        throw Exception("No questions generated")
                    }
                    
                    // Validate each question
                    quizResponse.questions.forEach { question ->
                        if (question.options.size != 4) {
                            throw Exception("Invalid number of options for question: ${question.question}")
                        }
                        if (question.correctAnswerIndex !in 0..3) {
                            throw Exception("Invalid correct answer index for question: ${question.question}")
                        }
                    }
                    
                    GeminiResponse.Success(quizResponse)
                } catch (e: JsonSyntaxException) {
                    GeminiResponse.Error("Failed to parse quiz response: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            GeminiResponse.Error("Failed to generate quiz: ${e.message}", e)
        }
    }
    
    /**
     * Enhance existing place description
     */
    suspend fun enhanceDescription(placeName: String, existingDescription: String): GeminiResponse<String> {
        return try {
            val model = getGenerativeModel() 
                ?: return GeminiResponse.Error("Gemini AI not configured. Please set up your API key in Settings.")
            
            withTimeout(Constants.QUIZ_GENERATION_TIMEOUT) {
                val prompt = GeminiPrompts.generateEnhancedDescriptionPrompt(placeName, existingDescription)
                val response = model.generateContent(prompt)
                val enhancedDescription = response.text ?: throw Exception("Empty response from Gemini")
                
                GeminiResponse.Success(enhancedDescription.trim())
            }
        } catch (e: Exception) {
            GeminiResponse.Error("Failed to enhance description: ${e.message}", e)
        }
    }
    
    /**
     * Generate custom quiz based on place data
     */
    suspend fun generateCustomQuiz(
        placeName: String,
        category: String,
        existingData: String? = null,
        difficulty: String = "medium",
        questionCount: Int = 5
    ): GeminiResponse<List<GeminiQuizQuestion>> {
        
        val request = QuizGenerationRequest(
            placeName = placeName,
            category = category,
            difficulty = difficulty,
            numberOfQuestions = questionCount,
            existingInfo = existingData
        )
        
        return when (val response = generateQuiz(request)) {
            is GeminiResponse.Success -> GeminiResponse.Success(response.data.questions)
            is GeminiResponse.Error -> GeminiResponse.Error(response.message, response.exception)
            is GeminiResponse.Loading -> GeminiResponse.Loading()
        }
    }
    
    /**
     * Check if Gemini AI is properly configured
     */
    fun isConfigured(): Boolean {
        return apiKeyManager.isGeminiConfigured()
    }
    
    /**
     * Get configuration status message
     */
    fun getConfigurationStatus(): String {
        return apiKeyManager.getConfigurationMessage()
    }
}