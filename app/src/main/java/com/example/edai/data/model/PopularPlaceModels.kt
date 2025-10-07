package com.example.edai.data.model

import com.google.gson.annotations.SerializedName

data class PopularPlacesResponse(
    @SerializedName("places")
    val places: List<PopularPlace>
)

data class PopularPlace(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("coordinates")
    val coordinates: Coordinates,
    @SerializedName("established")
    val established: String,
    @SerializedName("keyFacts")
    val keyFacts: List<String>,
    @SerializedName("quiz")
    val quiz: List<PlaceQuizQuestion>
)

data class Coordinates(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double
)

data class PlaceQuizQuestion(
    @SerializedName("question")
    val question: String,
    @SerializedName("options")
    val options: List<String>,
    @SerializedName("correctAnswer")
    val correctAnswer: Int,
    @SerializedName("explanation")
    val explanation: String
)
