package com.example.edai.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edai.data.model.LocationInfo
import com.example.edai.ui.viewmodel.PlaceQuizUiState

@Composable
fun QuizGenerationCard(
    location: LocationInfo,
    quizState: PlaceQuizUiState,
    showQuizOptions: Boolean,
    selectedDifficulty: String,
    selectedQuestionCount: Int,
    onShowQuizOptionsToggle: () -> Unit,
    onDifficultyChange: (String) -> Unit,
    onQuestionCountChange: (Int) -> Unit,
    onGenerateQuiz: () -> Unit,
    onAnswerSelected: (Int, Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onFinishQuiz: () -> Unit,
    onRetakeQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "AI-Generated Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Text(
                    text = "Test your knowledge with AI-generated questions about ${location.placeName}!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                // Quiz Options
                if (showQuizOptions) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Quiz Options",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            // Difficulty Selection
                            Text(
                                text = "Difficulty: ${selectedDifficulty.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("easy", "medium", "hard").forEach { difficulty ->
                                    FilterChip(
                                        onClick = { onDifficultyChange(difficulty) },
                                        label = { Text(difficulty.replaceFirstChar { it.uppercase() }) },
                                        selected = selectedDifficulty == difficulty
                                    )
                                }
                            }
                            
                            // Question Count Selection
                            Text(
                                text = "Number of Questions: $selectedQuestionCount",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(3, 5, 7, 10).forEach { count ->
                                    FilterChip(
                                        onClick = { onQuestionCountChange(count) },
                                        label = { Text(count.toString()) },
                                        selected = selectedQuestionCount == count
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onShowQuizOptionsToggle,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (showQuizOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showQuizOptions) "Less Options" else "More Options")
                    }
                    
                    Button(
                        onClick = onGenerateQuiz,
                        modifier = Modifier.weight(1f),
                        enabled = !quizState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (quizState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (quizState.isLoading) "Generating..." else "Generate Quiz")
                    }
                }
            }
        }
        
        // Show Generated Quiz
        if (quizState.questions.isNotEmpty() && !quizState.showResults) {
            QuizDisplayCard(
                quizState = quizState,
                onAnswerSelected = onAnswerSelected,
                onNextQuestion = onNextQuestion,
                onPreviousQuestion = onPreviousQuestion,
                onFinishQuiz = onFinishQuiz
            )
        }
        
        // Show Quiz Results
        if (quizState.showResults) {
            QuizResultsCard(
                quizState = quizState,
                onRetakeQuiz = onRetakeQuiz,
                onNewQuiz = onGenerateQuiz
            )
        }
    }
}

@Composable
private fun QuizDisplayCard(
    quizState: PlaceQuizUiState,
    onAnswerSelected: (Int, Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onFinishQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val selectedAnswer = if (quizState.currentQuestionIndex < quizState.selectedAnswers.size) {
        quizState.selectedAnswers[quizState.currentQuestionIndex]
    } else -1
    val isLastQuestion = quizState.currentQuestionIndex == quizState.questions.size - 1

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${quizState.currentQuestionIndex + 1} of ${quizState.questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                if (quizState.isGeminiGenerated) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI Generated",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            val progress = (quizState.currentQuestionIndex + 1).toFloat() / quizState.questions.size.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Question
            Text(
                text = currentQuestion.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            // Options
            currentQuestion.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == index
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            onAnswerSelected(quizState.currentQuestionIndex, index)
                        },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) 
                        androidx.compose.foundation.BorderStroke(
                            2.dp, 
                            MaterialTheme.colorScheme.primary
                        ) 
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = ('A' + index).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPreviousQuestion,
                    enabled = quizState.currentQuestionIndex > 0
                ) {
                    Icon(
                        Icons.Default.NavigateBefore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
                
                Button(
                    onClick = if (isLastQuestion) onFinishQuiz else onNextQuestion,
                    enabled = selectedAnswer != -1
                ) {
                    Text(if (isLastQuestion) "Finish" else "Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isLastQuestion) Icons.Default.CheckCircle else Icons.Default.NavigateNext,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultsCard(
    quizState: PlaceQuizUiState,
    onRetakeQuiz: () -> Unit,
    onNewQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = (quizState.score.toFloat() / quizState.questions.size.toFloat() * 100).toInt()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                percentage >= 80 -> MaterialTheme.colorScheme.primaryContainer
                percentage >= 60 -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                when {
                    percentage >= 80 -> Icons.Default.EmojiEvents
                    percentage >= 60 -> Icons.Default.ThumbUp
                    else -> Icons.Default.School
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = when {
                    percentage >= 80 -> "Excellent!"
                    percentage >= 60 -> "Good Job!"
                    else -> "Keep Learning!"
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${quizState.score}/${quizState.questions.size}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "($percentage%)",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            if (quizState.isGeminiGenerated) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AI-Generated Quiz • Difficulty: ${quizState.difficulty.replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetakeQuiz,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retake")
                }
                
                Button(
                    onClick = onNewQuiz,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Quiz")
                }
            }
        }
    }
}
