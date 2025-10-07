package com.example.edai.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edai.ui.viewmodel.LocationViewModel
import com.example.edai.ui.viewmodel.PlaceInfoViewModel
import com.example.edai.ui.components.QuizGenerationCard
import com.example.edai.utils.ApiKeyManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPlaceInfoScreen(
    locationViewModel: LocationViewModel,
    onNavigateToTrivia: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placeInfoViewModel: PlaceInfoViewModel = viewModel()
    val locationState by locationViewModel.uiState.collectAsStateWithLifecycle()
    val placeInfoState by placeInfoViewModel.placeInfoState.collectAsStateWithLifecycle()
    val quizState by placeInfoViewModel.quizState.collectAsStateWithLifecycle()

    val apiKeyManager = remember { ApiKeyManager.getInstance(context) }
    var showQuizOptions by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf("medium") }
    var selectedQuestionCount by remember { mutableStateOf(5) }

    // Load place info when screen is first displayed
    LaunchedEffect(locationState.currentLocation) {
        locationState.currentLocation?.let { location ->
            placeInfoViewModel.loadPlaceInfoByName(
                placeName = location.placeName,
                category = determineCategory(location.placeName)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Enhanced Place Information") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // AI Status Indicator
                Icon(
                    if (placeInfoState.isGeminiAvailable) Icons.Default.AutoAwesome else Icons.Default.Warning,
                    contentDescription = if (placeInfoState.isGeminiAvailable) "AI Enabled" else "AI Disabled",
                    tint = if (placeInfoState.isGeminiAvailable)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (placeInfoState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading place information...")
                        if (placeInfoState.isEnhancementLoading) {
                            Text(
                                "Generating AI insights...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                locationState.currentLocation?.let { location ->

                    // Place Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = location.placeName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            location.displayName?.let { displayName ->
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Lat: ${String.format("%.4f", location.latitude)}, " +
                                            "Lon: ${String.format("%.4f", location.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // AI Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (placeInfoState.isGeminiAvailable) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                if (placeInfoState.isGeminiAvailable) Icons.Default.AutoAwesome else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (placeInfoState.isGeminiAvailable) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (placeInfoState.isGeminiAvailable) {
                                        "AI Features Active"
                                    } else {
                                        "AI Features Unavailable"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (placeInfoState.isGeminiAvailable) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = placeInfoViewModel.getGeminiStatus(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (placeInfoState.isGeminiAvailable) {
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    }
                                )
                            }
                        }
                    }

                    // Enhanced AI Information
                    placeInfoState.enhancedInfo?.let { enhancedInfo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "AI-Enhanced Information",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                // Description
                                Text(
                                    text = enhancedInfo.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Justify,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                // Historical Significance
                                if (enhancedInfo.historicalSignificance.isNotBlank()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))

                                    Text(
                                        text = "Historical Significance",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = enhancedInfo.historicalSignificance,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Justify,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                // Interesting Facts
                                if (enhancedInfo.interestingFacts.isNotEmpty()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))

                                    Text(
                                        text = "Interesting Facts",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    enhancedInfo.interestingFacts.forEach { fact ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "•",
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = fact,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Best Time to Visit
                                if (enhancedInfo.bestTimeToVisit.isNotBlank()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))

                                    Text(
                                        text = "Best Time to Visit",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = enhancedInfo.bestTimeToVisit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                // Nearby Attractions
                                if (enhancedInfo.nearbyAttractions.isNotEmpty()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.3f))

                                    Text(
                                        text = "Nearby Attractions",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    enhancedInfo.nearbyAttractions.forEach { attraction ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Place,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = attraction,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // AI Quiz Generation Section
                    if (placeInfoState.isGeminiAvailable) {
                        QuizGenerationCard(
                            location = location,
                            quizState = quizState,
                            showQuizOptions = showQuizOptions,
                            selectedDifficulty = selectedDifficulty,
                            selectedQuestionCount = selectedQuestionCount,
                            onShowQuizOptionsToggle = { showQuizOptions = !showQuizOptions },
                            onDifficultyChange = { selectedDifficulty = it },
                            onQuestionCountChange = { selectedQuestionCount = it },
                            onGenerateQuiz = {
                                placeInfoViewModel.generateGeminiQuiz(
                                    difficulty = selectedDifficulty,
                                    questionCount = selectedQuestionCount
                                )
                            },
                            onAnswerSelected = { questionIndex, answerIndex ->
                                placeInfoViewModel.selectAnswer(questionIndex, answerIndex)
                            },
                            onNextQuestion = { placeInfoViewModel.nextQuestion() },
                            onPreviousQuestion = { placeInfoViewModel.previousQuestion() },
                            onFinishQuiz = { placeInfoViewModel.finishQuiz() },
                            onRetakeQuiz = { placeInfoViewModel.retakeQuiz() }
                        )
                    }

                    // Regular Trivia Section (fallback)
                    if (locationViewModel.isLocationRelevantForQuiz(locationState.currentLocation)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                        Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "General Knowledge Quiz",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "Take a general trivia quiz related to your location and geography.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = onNavigateToTrivia,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Quiz,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start General Quiz")
                                }
                            }
                        }
                    }
                } ?: run {
                    // No location selected
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "No location selected",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Please go back and select a location to get place information.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Error displays
            ErrorDisplays(
                placeInfoViewModel = placeInfoViewModel,
                placeInfoState = placeInfoState,
                quizState = quizState
            )
        }
    }
}

// Helper function to determine category
private fun determineCategory(placeName: String): String {
    return when {
        placeName.contains("airport", ignoreCase = true) || placeName.contains("station", ignoreCase = true) -> "Transportation"
        placeName.contains("college", ignoreCase = true) || placeName.contains("university", ignoreCase = true) ||
        placeName.contains("institute", ignoreCase = true) || placeName.contains("school", ignoreCase = true) -> "Educational Institute"
        placeName.contains("temple", ignoreCase = true) || placeName.contains("fort", ignoreCase = true) ||
        placeName.contains("palace", ignoreCase = true) || placeName.contains("museum", ignoreCase = true) -> "Tourist Place"
        placeName.contains("office", ignoreCase = true) || placeName.contains("municipal", ignoreCase = true) ||
        placeName.contains("government", ignoreCase = true) -> "Government Office"
        else -> "Location"
    }
}

@Composable
private fun ErrorDisplays(
    placeInfoViewModel: PlaceInfoViewModel,
    placeInfoState: com.example.edai.ui.viewmodel.PlaceInfoUiState,
    quizState: com.example.edai.ui.viewmodel.PlaceQuizUiState
) {
    // Error Display
    placeInfoState.error?.let { error ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { placeInfoViewModel.clearPlaceInfoError() }
                ) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Enhancement Error Display
    placeInfoState.enhancementError?.let { error ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "AI Enhancement: $error",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { placeInfoViewModel.clearEnhancementError() }
                ) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Quiz Error Display
    quizState.error?.let { error ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Quiz Error: $error",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { placeInfoViewModel.clearQuizError() }
                ) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
