package com.example.edai.data.repository

import com.example.edai.data.api.NominatimApi
import com.example.edai.data.model.LocationInfo
import com.example.edai.data.model.NominatimResult
import com.example.edai.data.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(
    private val nominatimApi: NominatimApi = NetworkModule.nominatimApi
) {
    
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<LocationInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = nominatimApi.reverseGeocode(latitude, longitude)
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null) {
                        val locationInfo = LocationInfo(
                            latitude = latitude,
                            longitude = longitude,
                            placeName = result.name ?: result.displayName.split(",").firstOrNull() ?: "Unknown Location",
                            displayName = result.displayName,
                            country = result.address?.country,
                            city = result.address?.city ?: result.address?.municipality
                        )
                        Result.success(locationInfo)
                    } else {
                        Result.failure(Exception("No location data found"))
                    }
                } else {
                    Result.failure(Exception("Failed to get location: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun forwardGeocode(query: String): Result<List<LocationInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = nominatimApi.forwardGeocode(query)
                if (response.isSuccessful) {
                    val results = response.body() ?: emptyList()
                    val locationInfos = results.map { result ->
                        LocationInfo(
                            latitude = result.lat.toDouble(),
                            longitude = result.lon.toDouble(),
                            placeName = result.name ?: result.displayName.split(",").firstOrNull() ?: "Unknown Location",
                            displayName = result.displayName,
                            country = result.address?.country,
                            city = result.address?.city ?: result.address?.municipality
                        )
                    }
                    Result.success(locationInfos)
                } else {
                    Result.failure(Exception("Failed to search location: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Mock data for fallback
    fun getMockLocationInfo(): LocationInfo {
        return LocationInfo(
            latitude = 18.5204,
            longitude = 73.8567,
            placeName = "Pune",
            displayName = "Pune, Maharashtra, India",
            country = "India",
            city = "Pune"
        )
    }
}