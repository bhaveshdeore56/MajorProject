package com.example.edai

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    
    @Test
    fun trivia_score_calculation_isCorrect() {
        val totalQuestions = 5
        val correctAnswers = 4
        val expectedScore = (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100
        assertEquals(80.0, expectedScore, 0.0)
    }
}