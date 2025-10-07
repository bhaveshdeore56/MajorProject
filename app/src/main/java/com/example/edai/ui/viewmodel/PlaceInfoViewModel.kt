package com.example.edai.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.edai.data.model.*
import com.example.edai.data.repository.GeminiRepository
import com.example.edai.data.repository.PopularPlacesRepository
import com.example.edai.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaceInfoUiState(
    val isLoading: Boolean = false,
    val placeName: String = "",
    val placeCategory: String = "",
    val basicInfo: PopularPlace? = null,
    val enhancedInfo: GeminiPlaceInfo? = null,
    val isGeminiAvailable: Boolean = false,
    val error: String? = null,
    val isEnhancementLoading: Boolean = false,
    val enhancementError: String? = null
)

data class PlaceQuizUiState(
    val isLoading: Boolean = false,
    val questions: List<GeminiQuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: MutableList<Int> = mutableListOf(),
    val showResults: Boolean = false,
    val score: Int = 0,
    val placeName: String = "",
    val error: String? = null,
    val isGeminiGenerated: Boolean = false,
    val difficulty: String = "medium"
)

class PlaceInfoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val popularPlacesRepository = PopularPlacesRepository(application)
    private val geminiRepository = GeminiRepository(application)
    
    private val _placeInfoState = MutableStateFlow(PlaceInfoUiState())
    val placeInfoState: StateFlow<PlaceInfoUiState> = _placeInfoState.asStateFlow()
    
    private val _quizState = MutableStateFlow(PlaceQuizUiState())
    val quizState: StateFlow<PlaceQuizUiState> = _quizState.asStateFlow()
    
    init {
        // Check if Gemini is available
        _placeInfoState.value = _placeInfoState.value.copy(
            isGeminiAvailable = geminiRepository.isGeminiAvailable()
        )
    }
    
    /**
     * Load place information (basic + enhanced with Gemini if available)
     */
    fun loadPlaceInfo(placeId: Int) {
        viewModelScope.launch {
            _placeInfoState.value = _placeInfoState.value.copy(
                isLoading = true, 
                error = null
            )
            
            try {
                // Load basic place information
                val basicInfoResult = popularPlacesRepository.getPlaceById(placeId)
                
                basicInfoResult.fold(
                    onSuccess = { place ->
                        if (place != null) {
                            _placeInfoState.value = _placeInfoState.value.copy(
                                isLoading = false,
                                basicInfo = place,
                                placeName = place.name,
                                placeCategory = place.category,
                                error = null
                            )
                            
                            // If Gemini is available, get enhanced information
                            if (_placeInfoState.value.isGeminiAvailable) {
                                loadEnhancedInfo(place.name, place.category)
                            }
                        } else {
                            _placeInfoState.value = _placeInfoState.value.copy(
                                isLoading = false,
                                error = "Place not found"
                            )
                        }
                    },
                    onFailure = { exception ->
                        _placeInfoState.value = _placeInfoState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load place information"
                        )
                    }
                )
            } catch (e: Exception) {
                _placeInfoState.value = _placeInfoState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Load place information by name (for current location-based queries)
     */
    fun loadPlaceInfoByName(placeName: String, category: String = "Location") {
        viewModelScope.launch {
            _placeInfoState.value = _placeInfoState.value.copy(
                isLoading = true,
                error = null,
                placeName = placeName,
                placeCategory = category
            )
            
            try {
                // If Gemini is available, get enhanced information
                if (_placeInfoState.value.isGeminiAvailable) {
                    loadEnhancedInfo(placeName, category)
                } else {
                    _placeInfoState.value = _placeInfoState.value.copy(
                        isLoading = false,
                        error = "Gemini AI not available. Please configure API key for enhanced place information."
                    )
                }
            } catch (e: Exception) {
                _placeInfoState.value = _placeInfoState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Load enhanced information using Gemini AI
     */
    private suspend fun loadEnhancedInfo(placeName: String, category: String) {
        _placeInfoState.value = _placeInfoState.value.copy(
            isEnhancementLoading = true,
            enhancementError = null
        )
        
        try {
            var retryCount = 0
            var lastException: Exception? = null
            
            while (retryCount < Constants.GEMINI_MAX_RETRIES) {
                try {
                    when (val result = geminiRepository.getEnhancedPlaceInfo(placeName, category)) {
                        is GeminiResponse.Success -> {
                            _placeInfoState.value = _placeInfoState.value.copy(
                                isLoading = false,
                                isEnhancementLoading = false,
                                enhancedInfo = result.data,
                                enhancementError = null
                            )
                            return
                        }
                        is GeminiResponse.Error -> {
                            lastException = result.exception
                            Log.w("PlaceInfoViewModel", "Gemini API error (attempt ${retryCount + 1}): ${result.message}")
                            
                            if (retryCount < Constants.GEMINI_MAX_RETRIES - 1) {
                                delay(Constants.GEMINI_RETRY_DELAY)
                                retryCount++
                            } else {
                                throw Exception(result.message)
                            }
                        }
                        is GeminiResponse.Loading -> {
                            // Continue waiting
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
                    if (retryCount < Constants.GEMINI_MAX_RETRIES - 1) {
                        Log.w("PlaceInfoViewModel", "Gemini request failed (attempt ${retryCount + 1}), retrying...", e)
                        delay(Constants.GEMINI_RETRY_DELAY)
                        retryCount++
                    } else {
                        throw e
                    }
                }
            }
            
            // If we reach here, all retries failed
            throw lastException ?: Exception("Failed to get enhanced information after $retryCount attempts")
            
        } catch (e: Exception) {
            Log.e("PlaceInfoViewModel", "Failed to load enhanced info for $placeName", e)
            _placeInfoState.value = _placeInfoState.value.copy(
                isLoading = false,
                isEnhancementLoading = false,
                enhancementError = "Failed to get enhanced information: ${e.message}"
            )
        }
    }
    
    /**
     * Generate Gemini-powered quiz for the current place
     */
    fun generateGeminiQuiz(difficulty: String = "medium", questionCount: Int = 5) {
        viewModelScope.launch {
            val currentPlace = _placeInfoState.value
            if (currentPlace.placeName.isEmpty()) {
                _quizState.value = _quizState.value.copy(
                    error = "No place selected for quiz generation"
                )
                return@launch
            }
            
            _quizState.value = _quizState.value.copy(
                isLoading = true,
                error = null,
                placeName = currentPlace.placeName,
                difficulty = difficulty
            )
            
            try {
                val existingInfo = buildString {
                    currentPlace.basicInfo?.description?.let { append(it) }
                    currentPlace.enhancedInfo?.description?.let { 
                        append(" ")
                        append(it)
                    }
                    currentPlace.enhancedInfo?.historicalSignificance?.let {
                        append(" ")
                        append(it)
                    }
                }
                
                var retryCount = 0
                var lastException: Exception? = null
                
                while (retryCount < Constants.GEMINI_MAX_RETRIES) {
                    try {
                        when (val result = geminiRepository.generateQuizForPlace(
                            placeName = currentPlace.placeName,
                            category = currentPlace.placeCategory,
                            existingInfo = existingInfo.ifEmpty { null },
                            difficulty = difficulty,
                            questionCount = questionCount
                        )) {
                            is GeminiResponse.Success -> {
                                val initialAnswers = MutableList(result.data.questions.size) { -1 }
                                _quizState.value = _quizState.value.copy(
                                    isLoading = false,
                                    questions = result.data.questions,
                                    selectedAnswers = initialAnswers,
                                    currentQuestionIndex = 0,
                                    showResults = false,
                                    score = 0,
                                    error = null,
                                    isGeminiGenerated = true
                                )
                                Log.d("PlaceInfoViewModel", "Generated ${result.data.questions.size} quiz questions")
                                return@launch
                            }
                            is GeminiResponse.Error -> {
                                lastException = result.exception
                                Log.w("PlaceInfoViewModel", "Quiz generation error (attempt ${retryCount + 1}): ${result.message}")
                                
                                if (retryCount < Constants.GEMINI_MAX_RETRIES - 1) {
                                    delay(Constants.GEMINI_RETRY_DELAY)
                                    retryCount++
                                } else {
                                    throw Exception(result.message)
                                }
                            }
                            is GeminiResponse.Loading -> {
                                // Continue waiting
                            }
                        }
                    } catch (e: Exception) {
                        lastException = e
                        if (retryCount < Constants.GEMINI_MAX_RETRIES - 1) {
                            Log.w("PlaceInfoViewModel", "Quiz generation failed (attempt ${retryCount + 1}), retrying...", e)
                            delay(Constants.GEMINI_RETRY_DELAY)
                            retryCount++
                        } else {
                            throw e
                        }
                    }
                }
                
                throw lastException ?: Exception("Failed to generate quiz after $retryCount attempts")
                
            } catch (e: Exception) {
                Log.e("PlaceInfoViewModel", "Failed to generate quiz for ${currentPlace.placeName}", e)
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    error = "Failed to generate quiz: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Load existing quiz from place data
     */
    fun loadExistingQuiz(placeId: Int) {
        viewModelScope.launch {
            try {
                val place = popularPlacesRepository.getPlaceById(placeId).getOrNull()
                if (place != null && place.quiz.isNotEmpty()) {
                    val initialAnswers = MutableList(place.quiz.size) { -1 }
                    _quizState.value = _quizState.value.copy(
                        questions = place.quiz.map { placeQuizQuestion ->
                            GeminiQuizQuestion(
                                question = placeQuizQuestion.question,
                                options = placeQuizQuestion.options,
                                correctAnswerIndex = placeQuizQuestion.correctAnswer,
                                explanation = placeQuizQuestion.explanation,
                                difficulty = "medium"
                            )
                        },
                        selectedAnswers = initialAnswers,
                        currentQuestionIndex = 0,
                        showResults = false,
                        score = 0,
                        placeName = place.name,
                        error = null,
                        isGeminiGenerated = false
                    )
                } else {
                    _quizState.value = _quizState.value.copy(
                        error = "No quiz available for this place"
                    )
                }
            } catch (e: Exception) {
                _quizState.value = _quizState.value.copy(
                    error = "Failed to load quiz: ${e.message}"
                )
            }
        }
    }
    
    // Quiz interaction methods
    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        val currentState = _quizState.value
        if (questionIndex >= 0 && questionIndex < currentState.selectedAnswers.size) {
            val updatedAnswers = currentState.selectedAnswers.toMutableList()
            updatedAnswers[questionIndex] = answerIndex
            _quizState.value = currentState.copy(selectedAnswers = updatedAnswers)
        }
    }
    
    fun nextQuestion() {
        val currentState = _quizState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _quizState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1
            )
        } else {
            finishQuiz()
        }
    }
    
    fun previousQuestion() {
        val currentState = _quizState.value
        if (currentState.currentQuestionIndex > 0) {
            _quizState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1
            )
        }
    }
    
    fun finishQuiz() {
        val currentState = _quizState.value
        val score = currentState.questions.indices.count { index ->
            val userAnswer = currentState.selectedAnswers.getOrNull(index) ?: -1
            val correctAnswer = currentState.questions[index].correctAnswerIndex
            userAnswer == correctAnswer
        }
        
        _quizState.value = currentState.copy(
            showResults = true,
            score = score
        )
    }
    
    fun retakeQuiz() {
        val currentState = _quizState.value
        val initialAnswers = MutableList(currentState.questions.size) { -1 }
        _quizState.value = currentState.copy(
            selectedAnswers = initialAnswers,
            currentQuestionIndex = 0,
            showResults = false,
            score = 0
        )
    }
    
    fun resetQuiz() {
        _quizState.value = PlaceQuizUiState()
    }
    
    // Error handling methods
    fun clearPlaceInfoError() {
        _placeInfoState.value = _placeInfoState.value.copy(error = null)
    }
    
    fun clearEnhancementError() {
        _placeInfoState.value = _placeInfoState.value.copy(enhancementError = null)
    }
    
    fun clearQuizError() {
        _quizState.value = _quizState.value.copy(error = null)
    }
    
    // Utility methods
    fun isCurrentQuestionAnswered(): Boolean {
        val currentState = _quizState.value
        val currentIndex = currentState.currentQuestionIndex
        return currentIndex >= 0 && 
               currentIndex < currentState.selectedAnswers.size && 
               currentState.selectedAnswers[currentIndex] != -1
    }
    
    fun getCurrentQuestionAnswer(): Int {
        val currentState = _quizState.value
        val currentIndex = currentState.currentQuestionIndex
        return if (currentIndex >= 0 && currentIndex < currentState.selectedAnswers.size) {
            currentState.selectedAnswers[currentIndex]
        } else -1
    }
    
    fun getGeminiStatus(): String {
        return geminiRepository.getGeminiStatus()
    }
    
    /**
     * Refresh Gemini availability status
     */
    fun refreshGeminiAvailability() {
        _placeInfoState.value = _placeInfoState.value.copy(
            isGeminiAvailable = geminiRepository.isGeminiAvailable()
        )
    }
}