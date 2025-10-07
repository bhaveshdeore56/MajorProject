package com.example.edai.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edai.ui.viewmodel.PlaceInfoViewModel
import com.example.edai.ui.viewmodel.PlaceQuizUiState
import com.example.edai.data.model.LocationInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriviaScreen(
    placeInfoViewModel: PlaceInfoViewModel,
    locationInfo: LocationInfo?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quizState by placeInfoViewModel.quizState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = if (quizState.showResults) "Quiz Results" else "AI Quiz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = {
                    placeInfoViewModel.resetQuiz()
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        when {
            quizState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading quiz questions...")
                        Text(
                            text = "AI is generating custom questions about ${locationInfo?.placeName ?: "this location"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            quizState.error != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
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
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Quiz Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = quizState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { placeInfoViewModel.clearQuizError() }
                            ) {
                                Text(
                                    "Dismiss",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            TextButton(
                                onClick = onNavigateBack
                            ) {
                                Text(
                                    "Go Back",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
            
            quizState.questions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No Quiz Available",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Please generate a quiz from the Place Information screen first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onNavigateBack
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
            
            quizState.showResults -> {
                AiQuizResultsContent(
                    quizState = quizState,
                    onRetakeQuiz = { placeInfoViewModel.retakeQuiz() },
                    onBackToPlace = onNavigateBack
                )
            }
            
            else -> {
                AiQuizContent(
                    quizState = quizState,
                    onAnswerSelected = { questionIndex, answerIndex ->
                        placeInfoViewModel.selectAnswer(questionIndex, answerIndex)
                    },
                    onNextQuestion = { placeInfoViewModel.nextQuestion() },
                    onPreviousQuestion = { placeInfoViewModel.previousQuestion() },
                    onFinishQuiz = { placeInfoViewModel.finishQuiz() }
                )
            }
        }
    }
}

@Composable
private fun AiQuizContent(
    quizState: PlaceQuizUiState,
    onAnswerSelected: (Int, Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onFinishQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
    val selectedAnswer = quizState.selectedAnswers.getOrNull(quizState.currentQuestionIndex) ?: -1
    val isLastQuestion = quizState.currentQuestionIndex == quizState.questions.size - 1
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${quizState.currentQuestionIndex + 1} of ${quizState.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (quizState.isGeminiGenerated) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI Generated",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Progress bar
                val progress = (quizState.currentQuestionIndex + 1).toFloat() / quizState.questions.size.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
            }
        }
        
        // Question Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Options Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            currentQuestion.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == index
                
                ElevatedCard(
                    onClick = { 
                        onAnswerSelected(quizState.currentQuestionIndex, index)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (isSelected) 8.dp else 2.dp
                    ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Option letter
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .animateContentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = ('A' + index).toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onSecondaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        
        // Navigation Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPreviousQuestion,
                    enabled = quizState.currentQuestionIndex > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.NavigateBefore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = if (isLastQuestion) onFinishQuiz else onNextQuestion,
                    enabled = selectedAnswer != -1,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isLastQuestion) "Finish Quiz" else "Next")
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
private fun AiQuizResultsContent(
    quizState: PlaceQuizUiState,
    onRetakeQuiz: () -> Unit,
    onBackToPlace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = (quizState.score.toFloat() / quizState.questions.size.toFloat() * 100).toInt()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Results Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when {
                                percentage >= 80 -> "Excellent!"
                                percentage >= 60 -> "Well Done!"
                                else -> "Good Effort!"
                            },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "AI Quiz Results",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${quizState.score}/${quizState.questions.size}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "($percentage%)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetakeQuiz,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake Quiz")
                }
                
                OutlinedButton(
                    onClick = onBackToPlace,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Place Info")
                }
            }
        }
    }
}
