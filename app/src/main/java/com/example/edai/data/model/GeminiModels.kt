package com.example.edai.data.model

import com.google.gson.annotations.SerializedName

// Data classes for Gemini AI responses

data class GeminiPlaceInfo(
    val name: String,
    val description: String,
    val historicalSignificance: String,
    val interestingFacts: List<String>,
    val bestTimeToVisit: String,
    val nearbyAttractions: List<String>
)

data class GeminiQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val difficulty: String = "medium"
)

data class GeminiQuizResponse(
    val questions: List<GeminiQuizQuestion>,
    val totalQuestions: Int,
    val category: String,
    val placeName: String
)

// Request models for structured prompts
data class PlaceInfoRequest(
    val placeName: String,
    val category: String,
    val location: String = "Pune, Maharashtra, India"
)

data class QuizGenerationRequest(
    val placeName: String,
    val category: String,
    val difficulty: String = "medium",
    val numberOfQuestions: Int = 5,
    val existingInfo: String? = null
)

// Response wrapper for error handling
sealed class GeminiResponse<T> {
    data class Success<T>(val data: T) : GeminiResponse<T>()
    data class Error<T>(val message: String, val exception: Exception? = null) : GeminiResponse<T>()
    class Loading<T> : GeminiResponse<T>()
}

// Prompt templates
object GeminiPrompts {
    
    fun generatePlaceInfoPrompt(place: PlaceInfoRequest): String = """
        Generate comprehensive information about "${place.placeName}" in ${place.location}.
        
        Please provide the response in the following JSON format:
        {
            "name": "${place.placeName}",
            "description": "Detailed description (200-300 words)",
            "historicalSignificance": "Historical context and significance (100-150 words)",
            "interestingFacts": ["fact1", "fact2", "fact3", "fact4", "fact5"],
            "bestTimeToVisit": "Best time to visit with reasons",
            "nearbyAttractions": ["attraction1", "attraction2", "attraction3"]
        }
        
        Focus on:
        - Accurate historical information
        - Cultural significance
        - Architecture and unique features
        - Local stories and legends
        - Practical visitor information
        
        Category: ${place.category}
        Make sure all information is factual and well-researched.
    """.trimIndent()
    
    fun generateQuizPrompt(request: QuizGenerationRequest): String = """
        Generate ${request.numberOfQuestions} ${request.difficulty} level quiz questions about "${request.placeName}".
        
        ${request.existingInfo?.let { "Use this additional context: $it" } ?: ""}
        
        Please provide the response in the following JSON format:
        {
            "questions": [
                {
                    "question": "Question text",
                    "options": ["option1", "option2", "option3", "option4"],
                    "correctAnswerIndex": 0,
                    "explanation": "Explanation for the correct answer",
                    "difficulty": "${request.difficulty}"
                }
            ],
            "totalQuestions": ${request.numberOfQuestions},
            "category": "${request.category}",
            "placeName": "${request.placeName}"
        }
        
        Requirements:
        - Each question should have exactly 4 options
        - correctAnswerIndex should be 0-based (0, 1, 2, or 3)
        - Include detailed explanations
        - Mix different types of questions (historical, architectural, cultural, geographical)
        - Ensure questions are factually accurate
        - Difficulty level: ${request.difficulty}
        
        Categories to focus on:
        - Historical facts and dates
        - Architectural features
        - Cultural significance
        - Geography and location
        - Notable events and personalities
    """.trimIndent()
    
    fun generateEnhancedDescriptionPrompt(placeName: String, existingDescription: String): String = """
        Enhance and expand the following description of "$placeName" with more engaging content:
        
        Current description: "$existingDescription"
        
        Please provide an enhanced version that:
        - Makes it more engaging and interesting
        - Adds cultural context
        - Includes lesser-known facts
        - Maintains accuracy
        - Keeps it within 400-500 words
        
        Return only the enhanced description text, no JSON format needed.
    """.trimIndent()
}
