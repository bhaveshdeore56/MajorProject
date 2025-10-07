package com.example.edai.data.model

import com.google.gson.annotations.SerializedName

// Open Trivia DB models
data class TriviaResponse(
    @SerializedName("response_code") val responseCode: Int,
    @SerializedName("results") val results: List<TriviaQuestion>
)

data class TriviaQuestion(
    @SerializedName("category") val category: String,
    @SerializedName("type") val type: String,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("question") val question: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>
)

// UI model for quiz
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val selectedAnswer: String? = null,
    val isAnswered: Boolean = false
)

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Double = (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100
)

// Overpass API models for place types
data class OverpassResponse(
    @SerializedName("version") val version: Double? = null,
    @SerializedName("generator") val generator: String? = null,
    @SerializedName("elements") val elements: List<OverpassElement>
)

data class OverpassElement(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: Long,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lon") val lon: Double? = null,
    @SerializedName("tags") val tags: Map<String, String>? = null
)