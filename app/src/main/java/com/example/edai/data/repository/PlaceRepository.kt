package com.example.edai.data.repository

import com.example.edai.data.api.WikipediaApi
import com.example.edai.data.model.PlaceInfo
import com.example.edai.data.model.LocationInfo
import com.example.edai.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlaceRepository(
    private val wikipediaApi: WikipediaApi = NetworkModule.wikipediaApi
) {
    
    suspend fun getPlaceInfo(placeName: String, locationInfo: LocationInfo? = null): Result<PlaceInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Clean the place name for Wikipedia search
                val cleanPlaceName = cleanPlaceNameForWikipedia(placeName)
                val response = wikipediaApi.getPageSummary(cleanPlaceName)
                
                if (response.isSuccessful) {
                    val wikiData = response.body()
                    if (wikiData != null) {
                        val placeInfo = PlaceInfo(
                            name = wikiData.title,
                            description = wikiData.extract ?: wikiData.description ?: "No description available",
                            imageUrl = wikiData.thumbnail?.source ?: wikiData.originalImage?.source,
                            wikiUrl = wikiData.contentUrls?.desktop?.page,
                            location = locationInfo
                        )
                        Result.success(placeInfo)
                    } else {
                        // Fallback to mock data
                        Result.success(getMockPlaceInfo(placeName, locationInfo))
                    }
                } else {
                    // Try alternative searches or return mock data
                    Result.success(getMockPlaceInfo(placeName, locationInfo))
                }
            } catch (e: Exception) {
                // Return mock data as fallback
                Result.success(getMockPlaceInfo(placeName, locationInfo))
            }
        }
    }
    
    private fun cleanPlaceNameForWikipedia(placeName: String): String {
        return placeName
            .replace(Regex("[,].*"), "") // Remove everything after first comma
            .trim()
            .replace(" ", "_") // Wikipedia uses underscores in URLs
    }
    
    private fun getMockPlaceInfo(placeName: String, locationInfo: LocationInfo? = null): PlaceInfo {
        // Mock data based on place name
        return when {
            placeName.contains("Pune", ignoreCase = true) -> PlaceInfo(
                name = "Pune",
                description = "Pune is a sprawling city in the western Indian state of Maharashtra. It was once the base of the Peshwas (prime ministers) of the Maratha Empire, which lasted from 1674 to 1818. It's known for the grand Aga Khan Palace, built in 1892 and now a memorial to Mahatma Gandhi, whose ashes are preserved in the garden. The 8th-century Pataleshwar Cave Temple is dedicated to the Hindu god Shiva.",
                imageUrl = null,
                location = locationInfo
            )
            placeName.contains("Mumbai", ignoreCase = true) -> PlaceInfo(
                name = "Mumbai",
                description = "Mumbai is the capital city of the Indian state of Maharashtra. It is the most populous city in India and the fourth most populous city in the world. Mumbai is the financial, commercial, and entertainment capital of India. The city is famous for being the heart of the Hindi-language film industry, known as Bollywood.",
                imageUrl = null,
                location = locationInfo
            )
            placeName.contains("Delhi", ignoreCase = true) -> PlaceInfo(
                name = "Delhi",
                description = "Delhi, officially the National Capital Territory of Delhi (NCT), is a city and a union territory of India containing New Delhi, the capital of India. It is bordered by Haryana on three sides and by Uttar Pradesh to the east. The city is known for its rich history, culture, and architecture.",
                imageUrl = null,
                location = locationInfo
            )
            else -> PlaceInfo(
                name = placeName,
                description = "This is an interesting place with rich history and culture. Explore more about this location to learn fascinating facts and stories that make it unique. Every place has its own charm and significance in the world.",
                imageUrl = null,
                location = locationInfo
            )
        }
    }
}