package com.example.edai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edai.data.model.LocationInfo
import com.example.edai.data.model.QuizQuestion
import com.example.edai.data.model.QuizResult
import com.example.edai.data.repository.TriviaCategory
import com.example.edai.data.repository.TriviaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TriviaViewModel : ViewModel() {
    
    private val triviaRepository = TriviaRepository()
    
    private val _uiState = MutableStateFlow(TriviaUiState())
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()
    
    fun loadLocationSpecificQuestions(locationInfo: LocationInfo) {
        _uiState.value = _uiState.value.copy(
            isLoading = true, 
            error = null, 
            currentLocation = locationInfo
        )
        
        viewModelScope.launch {
            try {
                val result = triviaRepository.getLocationSpecificQuestions(locationInfo, 5)
                result.fold(
                    onSuccess = { questions ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            questions = questions,
                            currentQuestionIndex = 0,
                            error = null,
                            quizCompleted = false,
                            showAnswer = false,
                            quizResult = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load questions about this place"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load questions about this place"
                )
            }
        }
    }
    
    // Legacy method for backward compatibility
    fun loadTriviaQuestions(category: TriviaCategory = TriviaCategory.GEOGRAPHY) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedCategory = category)
        
        viewModelScope.launch {
            try {
                val result = triviaRepository.getTriviaQuestions(amount = 5, category = category)
                result.fold(
                    onSuccess = { questions ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            questions = questions,
                            currentQuestionIndex = 0,
                            selectedCategory = category,
                            error = null,
                            quizCompleted = false,
                            showAnswer = false,
                            quizResult = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load trivia questions"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load trivia questions"
                )
            }
        }
    }
    
    fun answerQuestion(selectedAnswer: String) {
        val currentState = _uiState.value
        val questions = currentState.questions
        val currentIndex = currentState.currentQuestionIndex
        
        if (currentIndex < questions.size) {
            val updatedQuestions = questions.toMutableList()
            updatedQuestions[currentIndex] = questions[currentIndex].copy(
                selectedAnswer = selectedAnswer,
                isAnswered = true
            )
            
            _uiState.value = currentState.copy(
                questions = updatedQuestions,
                showAnswer = true
            )
        }
    }
    
    fun nextQuestion() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentQuestionIndex + 1
        
        if (nextIndex < currentState.questions.size) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                showAnswer = false
            )
        } else {
            // Quiz completed
            val result = calculateQuizResult(currentState.questions)
            _uiState.value = currentState.copy(
                quizCompleted = true,
                quizResult = result,
                showAnswer = false
            )
        }
    }
    
    fun resetQuiz() {
        val currentLocation = _uiState.value.currentLocation
        _uiState.value = TriviaUiState(
            selectedCategory = _uiState.value.selectedCategory,
            currentLocation = currentLocation
        )
        
        // Reload questions for the same location
        currentLocation?.let { location ->
            loadLocationSpecificQuestions(location)
        }
    }
    
    fun selectCategory(category: TriviaCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    private fun calculateQuizResult(questions: List<QuizQuestion>): QuizResult {
        val correctAnswers = questions.count { question ->
            question.selectedAnswer == question.correctAnswer
        }
        return QuizResult(
            totalQuestions = questions.size,
            correctAnswers = correctAnswers
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getQuizTypeDescription(): String {
        val location = _uiState.value.currentLocation
        return when {
            location == null -> "General Knowledge Quiz"
            else -> "Questions about ${location.placeName}"
        }
    }
}

data class TriviaUiState(
    val isLoading: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val showAnswer: Boolean = false,
    val quizCompleted: Boolean = false,
    val quizResult: QuizResult? = null,
    val selectedCategory: TriviaCategory = TriviaCategory.GEOGRAPHY,
    val currentLocation: LocationInfo? = null,
    val error: String? = null
)
