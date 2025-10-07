package com.example.edai.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edai.data.model.*
import com.example.edai.data.repository.PlaceRepository
import com.example.edai.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val placeRepository = PlaceRepository()
    private val geminiRepository = GeminiRepository(application)
    
    private val _uiState = MutableStateFlow(PlaceUiState())
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()
    
    private val _geminiState = MutableStateFlow(GeminiUiState())
    val geminiState: StateFlow<GeminiUiState> = _geminiState.asStateFlow()
    
    fun loadPlaceInfo(locationInfo: LocationInfo) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val result = placeRepository.getPlaceInfo(locationInfo.placeName, locationInfo)
                result.fold(
                    onSuccess = { placeInfo ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            placeInfo = placeInfo,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load place information"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load place information"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearGeminiError() {
        _geminiState.value = _geminiState.value.copy(error = null)
    }
    
    /**
     * Generate enhanced place information using Gemini AI
     */
    fun generateEnhancedPlaceInfo(placeName: String, category: String = "Historical Place") {
        if (!geminiRepository.isGeminiAvailable()) {
            _geminiState.value = _geminiState.value.copy(
                error = "Gemini AI is not configured. Please add your API key.",
                isGeminiAvailable = false
            )
            return
        }
        
        _geminiState.value = _geminiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            when (val result = geminiRepository.getEnhancedPlaceInfo(placeName, category)) {
                is GeminiResponse.Success -> {
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        enhancedInfo = result.data,
                        error = null
                    )
                }
                is GeminiResponse.Error -> {
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is GeminiResponse.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Generate quiz questions using Gemini AI
     */
    fun generateQuizQuestions(
        placeName: String, 
        category: String = "Historical Place",
        difficulty: String = "medium",
        questionCount: Int = 5
    ) {
        if (!geminiRepository.isGeminiAvailable()) {
            _geminiState.value = _geminiState.value.copy(
                error = "Gemini AI is not configured. Please add your API key.",
                isGeminiAvailable = false
            )
            return
        }
        
        _geminiState.value = _geminiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            when (val result = geminiRepository.generateQuizQuestions(placeName, category, difficulty, questionCount)) {
                is GeminiResponse.Success -> {
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        quizQuestions = result.data,
                        error = null
                    )
                }
                is GeminiResponse.Error -> {
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is GeminiResponse.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Generate comprehensive place data (info + quiz) using Gemini AI
     */
    fun generateComprehensivePlaceData(
        placeName: String,
        category: String = "Historical Place",
        generateQuiz: Boolean = true,
        quizDifficulty: String = "medium",
        questionCount: Int = 5
    ) {
        if (!geminiRepository.isGeminiAvailable()) {
            _geminiState.value = _geminiState.value.copy(
                error = "Gemini AI is not configured. Please add your API key.",
                isGeminiAvailable = false
            )
            return
        }
        
        _geminiState.value = _geminiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            when (val result = geminiRepository.generateComprehensivePlaceData(
                placeName = placeName,
                category = category,
                generateQuiz = generateQuiz,
                quizDifficulty = quizDifficulty,
                quizQuestionCount = questionCount
            )) {
                is GeminiResponse.Success -> {
                    val (placeInfo, quizResponse) = result.data
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        enhancedInfo = placeInfo,
                        quizQuestions = quizResponse?.questions ?: emptyList(),
                        error = null
                    )
                }
                is GeminiResponse.Error -> {
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is GeminiResponse.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Enhance existing description using Gemini AI
     */
    fun enhanceExistingDescription(placeName: String, existingDescription: String) {
        if (!geminiRepository.isGeminiAvailable()) {
            _geminiState.value = _geminiState.value.copy(
                error = "Gemini AI is not configured. Please add your API key.",
                isGeminiAvailable = false
            )
            return
        }
        
        _geminiState.value = _geminiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            when (val result = geminiRepository.enhanceExistingDescription(placeName, existingDescription)) {
                is GeminiResponse.Success -> {
                    // Create enhanced info object with the improved description
                    val currentInfo = _geminiState.value.enhancedInfo
                    val enhancedInfo = currentInfo?.copy(
                        description = result.data
                    ) ?: GeminiPlaceInfo(
                        name = placeName,
                        description = result.data,
                        historicalSignificance = "Enhanced with AI",
                        interestingFacts = emptyList(),
                        bestTimeToVisit = "Information available in description",
                        nearbyAttractions = emptyList()
                    )
                    
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        enhancedInfo = enhancedInfo,
                        error = null
                    )
                }
                is GeminiResponse.Error -> {
                    _geminiState.value = _geminiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is GeminiResponse.Loading -> {
                    // Already handled above
                }
            }
        }
    }
    
    /**
     * Check if Gemini AI is available and configured
     */
    fun checkGeminiAvailability(): Boolean {
        val isAvailable = geminiRepository.isGeminiAvailable()
        _geminiState.value = _geminiState.value.copy(isGeminiAvailable = isAvailable)
        return isAvailable
    }
    
    /**
     * Get Gemini configuration status message
     */
    fun getGeminiStatus(): String {
        return geminiRepository.getGeminiStatus()
    }
}

data class PlaceUiState(
    val isLoading: Boolean = false,
    val placeInfo: PlaceInfo? = null,
    val error: String? = null
)

data class GeminiUiState(
    val isLoading: Boolean = false,
    val enhancedInfo: GeminiPlaceInfo? = null,
    val quizQuestions: List<GeminiQuizQuestion> = emptyList(),
    val error: String? = null,
    val isGeminiAvailable: Boolean = true
)
