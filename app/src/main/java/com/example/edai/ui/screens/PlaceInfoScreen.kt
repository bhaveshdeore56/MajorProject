package com.example.edai.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.edai.ui.viewmodel.LocationViewModel
import com.example.edai.ui.viewmodel.PlaceInfoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceInfoScreen(
    locationViewModel: LocationViewModel,
    placeInfoViewModel: PlaceInfoViewModel,
    onNavigateToTrivia: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locationState by locationViewModel.uiState.collectAsStateWithLifecycle()
    val placeInfoState by placeInfoViewModel.placeInfoState.collectAsStateWithLifecycle()
    val quizState by placeInfoViewModel.quizState.collectAsStateWithLifecycle()
    
    var selectedDifficulty by remember { mutableStateOf("medium") }
    var showQuizOptions by remember { mutableStateOf(false) }
    
    // Load place information when location changes
    LaunchedEffect(locationState.currentLocation) {
        locationState.currentLocation?.let { location ->
            // Get a meaningful place name from the location
            val placeName = location.displayName?.substringBefore(",") ?: 
                           location.city ?: 
                           location.country ?: 
                           "Current Location"
            
            placeInfoViewModel.loadPlaceInfoByName(
                placeName = placeName,
                category = "Location"
            )
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (placeInfoState.placeName.isNotEmpty()) {
                        "About ${placeInfoState.placeName}"
                    } else {
                        "Place Information"
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Settings button
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
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

            // Gemini AI Status Check
            if (!placeInfoState.isGeminiAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "AI Features Not Available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Text(
                            text = placeInfoViewModel.getGeminiStatus(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = onNavigateToSettings,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                contentColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configure API Key")
                        }
                    }
                }
            }

            // Loading State
            if (placeInfoState.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading enhanced place information...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Our AI is gathering detailed insights about this location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Basic Location Info
            locationState.currentLocation?.let { location ->
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
                            text = location.displayName ?: "Current Location",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

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
                                text = "${
                                    String.format(
                                        "%.4f",
                                        location.latitude
                                    )
                                }, ${String.format("%.4f", location.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Enhanced Place Information from Gemini
            placeInfoState.enhancedInfo?.let { enhancedInfo ->
                // Main Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "About This Place",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = enhancedInfo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                // Historical Significance Card
                if (enhancedInfo.historicalSignificance.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Historical Significance",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = enhancedInfo.historicalSignificance,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )
                        }
                    }
                }

                // Interesting Facts Card
                if (enhancedInfo.interestingFacts.isNotEmpty()) {
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
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Interesting Facts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )


                                enhancedInfo.interestingFacts.forEach { fact ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = fact,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Best Time to Visit Card
                    if (enhancedInfo.bestTimeToVisit.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = "Best Time to Visit",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )


                                    Text(
                                        text = enhancedInfo.bestTimeToVisit,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Nearby Attractions Card
                        if (enhancedInfo.nearbyAttractions.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                            Icons.Default.Explore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Nearby Attractions",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )


                                        enhancedInfo.nearbyAttractions.forEach { attraction ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Place,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = attraction,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // AI-Generated Quiz Section
                        if (placeInfoState.isGeminiAvailable && placeInfoState.placeName.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Quiz,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "AI-Powered Quiz",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Text(
                                        text = "Test your knowledge with AI-generated questions about ${placeInfoState.placeName}!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )

                                    // Difficulty Selection
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Select Difficulty:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            listOf("easy", "medium", "hard").forEach { difficulty ->
                                                FilterChip(
                                                    onClick = { selectedDifficulty = difficulty },
                                                    label = {
                                                        Text(
                                                            text = difficulty.replaceFirstChar {
                                                                if (it.isLowerCase()) it.titlecase(
                                                                    Locale.getDefault()
                                                                )
                                                                else it.toString()
                                                            },
                                                            style = MaterialTheme.typography.labelMedium
                                                        )
                                                    },
                                                    selected = selectedDifficulty == difficulty,
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // Generate Quiz Button
                                    Button(
                                        onClick = {
                                            placeInfoViewModel.generateGeminiQuiz(
                                                difficulty = selectedDifficulty,
                                                questionCount = 5
                                            )
                                            showQuizOptions = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !quizState.isLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        if (quizState.isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onSecondary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Generating Quiz...")
                                        } else {
                                            Icon(
                                                Icons.Default.Psychology,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Generate AI Quiz")
                                        }
                                    }
                                }
                            }
                        }

                        // Quiz Questions Display (Mini Preview)
                        if (quizState.questions.isNotEmpty() && !quizState.showResults) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = "Quiz Generated Successfully!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }

                                    Text(
                                        text = "${quizState.questions.size} questions ready • Difficulty: ${
                                            quizState.difficulty.replaceFirstChar {
                                                if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                                else it.toString()
                                            }
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )

                                    // Sample question preview
                                    if (quizState.questions.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Sample Question:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                            Text(
                                                text = quizState.questions.first().question,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = onNavigateToTrivia,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary
                                        )
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start Quiz")
                                    }
                                }
                            }
                        }

                        // Error States
                        placeInfoState.error?.let { error ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                            text = "Error Loading Information",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TextButton(
                                            onClick = { placeInfoViewModel.clearPlaceInfoError() }
                                        ) {
                                            Text(
                                                "Dismiss",
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        TextButton(
                                            onClick = {
                                                locationState.currentLocation?.let { location ->
                                                    val placeName =
                                                        location.displayName?.substringBefore(",")
                                                            ?: location.city ?: location.country
                                                            ?: "Current Location"
                                                    placeInfoViewModel.loadPlaceInfoByName(
                                                        placeName,
                                                        "Location"
                                                    )
                                                }
                                            }
                                        ) {
                                            Text(
                                                "Retry",
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        quizState.error?.let { error ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                            text = "Quiz Generation Error",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
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
                                            onClick = {
                                                placeInfoViewModel.generateGeminiQuiz(
                                                    difficulty = selectedDifficulty,
                                                    questionCount = 5
                                                )
                                            }
                                        ) {
                                            Text(
                                                "Retry",
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Enhancement Loading State
                        if (placeInfoState.isEnhancementLoading) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Enhancing place information with AI...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Enhancement Error
                        placeInfoState.enhancementError?.let { error ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Enhancement Error",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    TextButton(
                                        onClick = { placeInfoViewModel.clearEnhancementError() }
                                    ) {
                                        Text(
                                            "Dismiss",
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
