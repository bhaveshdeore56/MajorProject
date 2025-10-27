package com.example.edai.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.edai.data.model.PlaceQuizQuestion
import com.example.edai.ui.viewmodel.PopularPlacesViewModel
import com.example.edai.ui.viewmodel.QuizUiState
import com.example.edai.ui.components.WebGLCharacterView
import com.example.edai.ui.components.CharacterAnimationState
import com.example.edai.ui.components.AnimatedLottieCharacter
import com.example.edai.ui.components.LottieAnimationState
import com.example.edai.ui.components.CharacterPosition
import com.example.edai.ui.components.CharacterSize
import com.example.edai.ui.components.rememberAnimatedCharacterPosition
import com.example.edai.ui.components.rememberAnimatedCharacterSize
import com.example.edai.ui.components.CharacterSpeechBubble
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceQuizScreen(
    placeId: Int,
    viewModel: PopularPlacesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeDetailState by viewModel.placeDetailState.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()

    // Load place details when screen is first displayed or placeId changes
    LaunchedEffect(placeId) {
        // Always reload place details for the specific place
        viewModel.loadPlaceDetail(placeId)
    }

    // Start quiz when place is loaded and it's a different place or quiz is empty
    LaunchedEffect(placeDetailState.place, placeId) {
        placeDetailState.place?.let { place ->
            // Start new quiz if:
            // 1. No quiz is currently loaded, OR
            // 2. The quiz is for a different place, OR
            // 3. The quiz questions don't match the current place's questions
            if (quizState.questions.isEmpty() ||
                quizState.currentPlaceId != place.id ||
                (quizState.questions.isNotEmpty() && place.quiz.isNotEmpty() &&
                        place.quiz.first().question != quizState.questions.first().question)) {
                viewModel.startQuiz(place)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (quizState.showResults) "Quiz Results" else "Quiz: ${placeDetailState.place?.name ?: "Loading..."}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    viewModel.resetQuiz()
                    onNavigateBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        when {
            placeDetailState.isLoading || quizState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading quiz...")
                    }
                }
            }

            placeDetailState.error != null || quizState.error != null -> {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = placeDetailState.error ?: quizState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.clearPlaceDetailError()
                                    viewModel.loadPlaceDetail(placeId)
                                }
                            ) {
                                Text(
                                    "Retry",
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            TextButton(
                                onClick = {
                                    viewModel.resetQuiz()
                                    onNavigateBack()
                                }
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

            quizState.showResults -> {
                QuizResultsContent(
                    quizState = quizState,
                    placeName = placeDetailState.place?.name ?: "",
                    onRetakeQuiz = {
                        placeDetailState.place?.let { place ->
                            viewModel.startQuiz(place)
                        }
                    },
                    onFinish = onNavigateBack
                )
            }

            quizState.questions.isNotEmpty() -> {
                QuizContent(
                    quizState = quizState,
                    placeName = placeDetailState.place?.name ?: "",
                    onAnswerSelected = { answerIndex ->
                        viewModel.selectAnswer(quizState.currentQuestionIndex, answerIndex)
                    },
                    onNextQuestion = { viewModel.nextQuestion() },
                    onPreviousQuestion = { viewModel.previousQuestion() },
                    onFinishQuiz = { viewModel.finishQuiz() }
                )
            }
        }
    }
}

@Composable
private fun QuizContent(
    quizState: QuizUiState,
    placeName: String,
    onAnswerSelected: (Int) -> Unit,
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

    // Character animation state management with Lottie
    var currentAnimationState by remember { mutableStateOf<LottieAnimationState>(LottieAnimationState.Idle) }
    var characterPosition by remember { mutableStateOf(CharacterPosition(x = 16.dp, y = 0.dp)) }
    var characterSize by remember { mutableStateOf(CharacterSize(width = 200.dp, height = 200.dp)) }
    var showSpeechBubble by remember { mutableStateOf(false) }
    var speechBubbleMessage by remember { mutableStateOf("") }
    
    // Animation state based on quiz progress
    val targetAnimationState = remember(selectedAnswer, quizState.currentQuestionIndex) {
        when {
            selectedAnswer == -1 -> LottieAnimationState.Idle
            selectedAnswer == currentQuestion.correctAnswer -> LottieAnimationState.Celebrating
            selectedAnswer != currentQuestion.correctAnswer -> LottieAnimationState.Thinking
            else -> LottieAnimationState.Idle
        }
    }
    
    // Update animation state with effects
    LaunchedEffect(targetAnimationState) {
        when (targetAnimationState) {
            is LottieAnimationState.Celebrating -> {
                // Move to center and grow
                characterPosition = CharacterPosition(x = 150.dp, y = 200.dp)
                characterSize = CharacterSize(width = 300.dp, height = 300.dp)
                currentAnimationState = LottieAnimationState.Celebrating
                showSpeechBubble = true
                speechBubbleMessage = "Great job! 🎉"
                delay(2000)
                
                // Return to top
                characterPosition = CharacterPosition(x = 16.dp, y = 0.dp)
                characterSize = CharacterSize(width = 200.dp, height = 200.dp)
                showSpeechBubble = false
                currentAnimationState = LottieAnimationState.Idle
            }
            
            is LottieAnimationState.Thinking -> {
                // Move to wrong answer area and shake
                val shakeOffset = ((-10..10).random()).dp
                characterPosition = CharacterPosition(x = 150.dp, y = 300.dp + shakeOffset)
                currentAnimationState = LottieAnimationState.Thinking
                showSpeechBubble = true
                speechBubbleMessage = "Try again! 💪"
                delay(1500)
                
                // Return to top
                characterPosition = CharacterPosition(x = 16.dp, y = 0.dp)
                characterSize = CharacterSize(width = 200.dp, height = 200.dp)
                showSpeechBubble = false
                currentAnimationState = LottieAnimationState.Idle
            }
            
            is LottieAnimationState.Encouraging -> {
                // Bounce animation for next question
                for (i in 0..2) {
                    characterPosition = CharacterPosition(x = 100.dp, y = (i % 2).times(100).dp)
                    delay(300)
                }
                characterPosition = CharacterPosition(x = 16.dp, y = 0.dp)
                currentAnimationState = LottieAnimationState.Idle
            }
            
            else -> {
                characterPosition = CharacterPosition(x = 16.dp, y = 0.dp)
                characterSize = CharacterSize(width = 200.dp, height = 200.dp)
                currentAnimationState = LottieAnimationState.Idle
            }
        }
    }
    
    // Animate position and size changes smoothly
    val animatedPosition = rememberAnimatedCharacterPosition(characterPosition)
    val animatedSize = rememberAnimatedCharacterSize(characterSize)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background Layer - Quiz Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(top = 220.dp), // Space for character at top
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

                        Text(
                            text = placeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAnswerSelected(index)
                        },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 8.dp else 2.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected)
                        androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.secondary
                        )
                    else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Option letter (A, B, C, D)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + index).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
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

                        // Selection indicator
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            // Empty space to maintain layout consistency
                            Box(modifier = Modifier.size(20.dp))
                        }
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
                // Previous button
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

                // Next/Finish button
                Button(
                    onClick = if (isLastQuestion) onFinishQuiz else onNextQuestion,
                    enabled = selectedAnswer != -1, // Answer must be selected
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

        // Quiz info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (selectedAnswer == -1) {
                        "Select an answer to proceed to the next question"
                    } else {
                        "Answer selected! You can now proceed or change your selection."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
        
        // Foreground Layer - Animated Lottie Character (can move anywhere)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f) // Keep character on top
        ) {
            AnimatedLottieCharacter(
                state = currentAnimationState,
                position = animatedPosition,
                size = animatedSize,
                modifier = Modifier
                    .align(Alignment.TopStart)
            )
        }
        
        // Speech bubble
        CharacterSpeechBubble(
            message = speechBubbleMessage,
            visible = showSpeechBubble,
            position = animatedPosition
        )
    }
}

@Composable
private fun QuizResultsContent(
    quizState: QuizUiState,
    placeName: String,
    onRetakeQuiz: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = (quizState.score.toFloat() / quizState.questions.size.toFloat() * 100).toInt()

    // Determine character animation state based on quiz results
    val characterAnimationState = remember(percentage) {
        when {
            percentage >= 80 -> CharacterAnimationState.CELEBRATING
            percentage >= 60 -> CharacterAnimationState.HAPPY
            else -> CharacterAnimationState.ENCOURAGING
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // WebGL Character Results Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    percentage >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    percentage >= 60 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    else -> Color(0xFF2196F3).copy(alpha = 0.1f)
                }
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                WebGLCharacterView(
                    animationState = characterAnimationState,
                    modifier = Modifier.fillMaxSize(),
                    modelFilename = "ant-character.obj" // Custom 3D model
                )

                // Results message overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (characterAnimationState) {
                                CharacterAnimationState.CELEBRATING -> "🎉 Outstanding!"
                                CharacterAnimationState.HAPPY -> "😊 Great work!"
                                CharacterAnimationState.ENCOURAGING -> "💪 Keep learning!"
                                else -> "👋 Well done!"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Score: ${quizState.score}/${quizState.questions.size} ($percentage%)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Results Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    percentage >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    percentage >= 60 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    else -> Color(0xFFF44336).copy(alpha = 0.1f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
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
                    tint = when {
                        percentage >= 80 -> Color(0xFF4CAF50)
                        percentage >= 60 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
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

                Text(
                    text = placeName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Performance Message
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when {
                        percentage >= 80 -> "Outstanding performance! 🎉"
                        percentage >= 60 -> "Nice work! You're doing great! 👍"
                        else -> "Good effort! Keep exploring to learn more! 📚"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = when {
                        percentage >= 80 -> "You clearly know a lot about $placeName. You're well on your way to becoming a local expert!"
                        percentage >= 60 -> "You have a good grasp of $placeName's key facts. A bit more exploration and you'll be an expert!"
                        else -> "There's so much more to discover about $placeName. Every question is a learning opportunity!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Detailed Results
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Question Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                quizState.questions.forEachIndexed { index, question ->
                    val userAnswer = if (index < quizState.selectedAnswers.size) {
                        quizState.selectedAnswers[index]
                    } else -1
                    val isCorrect = userAnswer == question.correctAnswer

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Q${index + 1}: ${question.question}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (!isCorrect && userAnswer >= 0 && userAnswer < question.options.size) {
                                        Text(
                                            text = "Your answer: ${question.options[userAnswer]}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFF44336)
                                        )
                                        Text(
                                            text = "Correct answer: ${question.options[question.correctAnswer]}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Text(
                                        text = question.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
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
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Place Details")
                }
            }
        }
    }
}