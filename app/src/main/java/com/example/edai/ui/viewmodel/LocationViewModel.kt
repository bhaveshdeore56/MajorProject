package com.example.edai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edai.data.model.LocationInfo
import com.example.edai.data.repository.LocationRepository
import com.example.edai.location.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val locationService = LocationService(application)
    private val locationRepository = LocationRepository()
    
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<LocationInfo>>(emptyList())
    val searchResults: StateFlow<List<LocationInfo>> = _searchResults.asStateFlow()
    
    private var searchJob: Job? = null
    
    fun hasLocationPermission(): Boolean {
        return locationService.hasLocationPermission()
    }
    
    fun detectCurrentLocation() {
        if (!hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location permission is required"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        // Clear search results when detecting current location
        _searchResults.value = emptyList()
        
        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                    ?: locationService.requestSingleLocationUpdate()
                
                if (location != null) {
                    val result = locationRepository.reverseGeocode(location.latitude, location.longitude)
                    result.fold(
                        onSuccess = { locationInfo ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentLocation = locationInfo,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to get location details"
                            )
                        }
                    )
                } else {
                    // Use mock location as fallback
                    val mockLocation = locationRepository.getMockLocationInfo()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentLocation = mockLocation,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to detect location"
                )
            }
        }
    }
    
    fun searchLocation(query: String) {
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _uiState.value = _uiState.value.copy(isSearching = false)
            return
        }
        
        // Debounce search - wait 500ms before searching
        searchJob = viewModelScope.launch {
            delay(500)
            performSearch(query)
        }
    }
    
    private suspend fun performSearch(query: String) {
        _uiState.value = _uiState.value.copy(isSearching = true, error = null)
        
        try {
            val result = locationRepository.forwardGeocode(query)
            result.fold(
                onSuccess = { locations ->
                    _searchResults.value = locations
                    _uiState.value = _uiState.value.copy(isSearching = false)
                },
                onFailure = { error ->
                    _searchResults.value = emptyList()
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = error.message ?: "Search failed"
                    )
                }
            )
        } catch (e: Exception) {
            _searchResults.value = emptyList()
            _uiState.value = _uiState.value.copy(
                isSearching = false,
                error = e.message ?: "Search failed"
            )
        }
    }
    
    fun selectLocation(locationInfo: LocationInfo) {
        _uiState.value = _uiState.value.copy(
            currentLocation = locationInfo,
            error = null
        )
        // Clear search results after selection
        _searchResults.value = emptyList()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSearchResults() {
        // Cancel any ongoing search
        searchJob?.cancel()
        _searchResults.value = emptyList()
        _uiState.value = _uiState.value.copy(isSearching = false)
    }
    
    // Check if location is relevant for quiz
    fun isLocationRelevantForQuiz(locationInfo: LocationInfo?): Boolean {
        if (locationInfo == null) return false
        
        val placeName = locationInfo.placeName.lowercase()
        val displayName = locationInfo.displayName?.lowercase() ?: ""
        
        // Check if it's a notable place (city, landmark, country, etc.)
        val relevantKeywords = listOf(
            // Cities and places
            "city", "town", "village", "district", "municipality", 
            // Countries and states
            "india", "mumbai", "delhi", "pune", "bangalore", "kolkata", "chennai",
            "maharashtra", "gujarat", "rajasthan", "kerala", "goa",
            // Historical places
            "fort", "palace", "monument", "temple", "church", "mosque",
            "museum", "park", "garden", "lake", "river", "mountain",
            // International
            "paris", "london", "tokyo", "new york", "sydney", "rome",
            "egypt", "japan", "france", "italy", "spain", "germany",
            "china", "australia", "canada", "brazil", "russia"
        )
        
        val hasRelevantKeyword = relevantKeywords.any { keyword ->
            placeName.contains(keyword) || displayName.contains(keyword)
        }
        
        // Also check if it's not just a street address or building
        val isSpecificAddress = listOf("road", "street", "lane", "apartment", "building", "shop").any {
            placeName.contains(it) || displayName.contains(it)
        }
        
        return hasRelevantKeyword && !isSpecificAddress
    }
    
    // Get appropriate trivia category based on location
    fun getTriviaCategory(locationInfo: LocationInfo?): com.example.edai.data.repository.TriviaCategory {
        if (locationInfo == null) return com.example.edai.data.repository.TriviaCategory.GENERAL_KNOWLEDGE
        
        val placeName = locationInfo.placeName.lowercase()
        val displayName = locationInfo.displayName?.lowercase() ?: ""
        val country = locationInfo.country?.lowercase() ?: ""
        
        // Determine if it's more historical or geographical
        val historicalKeywords = listOf(
            "fort", "palace", "monument", "temple", "church", "mosque", 
            "museum", "heritage", "ancient", "historic", "medieval", "colonial"
        )
        
        val geographicalKeywords = listOf(
            "mountain", "river", "lake", "desert", "forest", "beach", "island",
            "valley", "hill", "plateau", "coast", "bay", "city", "capital"
        )
        
        return when {
            historicalKeywords.any { keyword ->
                placeName.contains(keyword) || displayName.contains(keyword)
            } -> com.example.edai.data.repository.TriviaCategory.HISTORY
            
            geographicalKeywords.any { keyword ->
                placeName.contains(keyword) || displayName.contains(keyword)
            } -> com.example.edai.data.repository.TriviaCategory.GEOGRAPHY
            
            else -> com.example.edai.data.repository.TriviaCategory.GENERAL_KNOWLEDGE
        }
    }
}

data class LocationUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val currentLocation: LocationInfo? = null,
    val error: String? = null
)
