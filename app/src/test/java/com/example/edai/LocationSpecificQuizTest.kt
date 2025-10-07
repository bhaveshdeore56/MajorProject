package com.example.edai

import com.example.edai.data.model.LocationInfo
import com.example.edai.data.repository.TriviaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Simple test to verify location-specific quiz functionality
 */
class LocationSpecificQuizTest {
    
    private val triviaRepository = TriviaRepository()
    
    @Test
    fun testPuneSpecificQuestions() = runBlocking {
        val puneLocation = LocationInfo(
            latitude = 18.5204,
            longitude = 73.8567,
            placeName = "Pune",
            displayName = "Pune, Maharashtra, India",
            country = "India",
            city = "Pune"
        )
        
        val result = triviaRepository.getLocationSpecificQuestions(puneLocation, 5)
        assert(result.isSuccess)
        
        val questions = result.getOrNull()
        assert(questions != null)
        assert(questions!!.isNotEmpty())
        assert(questions.size <= 5)
        
        // Verify questions are about Pune
        val hasPoona = questions.any { it.question.contains("Poona", ignoreCase = true) }
        val hasMaratha = questions.any { it.question.contains("Maratha", ignoreCase = true) }
        assert(hasPoona || hasMaratha) // At least one question should be Pune-specific
        
        println("✅ Pune-specific questions loaded successfully:")
        questions.forEach { question ->
            println("Q: ${question.question}")
            println("A: ${question.correctAnswer}")
            println()
        }
    }
    
    @Test
    fun testMumbaiSpecificQuestions() = runBlocking {
        val mumbaiLocation = LocationInfo(
            latitude = 19.0760,
            longitude = 72.8777,
            placeName = "Mumbai",
            displayName = "Mumbai, Maharashtra, India",
            country = "India",
            city = "Mumbai"
        )
        
        val result = triviaRepository.getLocationSpecificQuestions(mumbaiLocation, 5)
        assert(result.isSuccess)
        
        val questions = result.getOrNull()
        assert(questions != null)
        assert(questions!!.isNotEmpty())
        
        // Verify questions are about Mumbai
        val hasBombay = questions.any { it.question.contains("Bombay", ignoreCase = true) }
        val hasMumbadevi = questions.any { it.question.contains("Mumbadevi", ignoreCase = true) }
        assert(hasBombay || hasMumbadevi)
        
        println("✅ Mumbai-specific questions loaded successfully:")
        questions.forEach { question ->
            println("Q: ${question.question}")
            println("A: ${question.correctAnswer}")
            println()
        }
    }
    
    @Test
    fun testGenericLocationFallback() = runBlocking {
        val genericLocation = LocationInfo(
            latitude = 40.7128,
            longitude = -74.0060,
            placeName = "Unknown Street",
            displayName = "Unknown Street, Unknown City",
            country = "Unknown",
            city = "Unknown"
        )
        
        val result = triviaRepository.getLocationSpecificQuestions(genericLocation, 5)
        assert(result.isSuccess)
        
        val questions = result.getOrNull()
        assert(questions != null)
        assert(questions!!.isNotEmpty())
        
        println("✅ Generic fallback questions loaded successfully:")
        questions.forEach { question ->
            println("Q: ${question.question}")
            println("A: ${question.correctAnswer}")
            println()
        }
    }
}
