package com.example.edai.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edai.utils.ApiKeyManager
import com.example.edai.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager.getInstance(context) }
    
    var apiKey by remember { mutableStateOf(apiKeyManager.getGeminiApiKey()) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConfigured by remember { mutableStateOf(apiKeyManager.isGeminiConfigured()) }
    
    // Auto-hide success message after 3 seconds
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(3000)
            showSuccessMessage = false
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(Constants.SETTINGS_SCREEN_TITLE) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            
            // Success Message
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "API key updated successfully! AI features are now available.",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Error Message
            errorMessage?.let { error ->
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
                        IconButton(
                            onClick = { errorMessage = null }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Configuration Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConfigured) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
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
                            if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isConfigured) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "AI Features Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConfigured) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    
                    Text(
                        text = apiKeyManager.getConfigurationMessage(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isConfigured) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // API Key Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Constants.API_KEY_SECTION_TITLE,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // API Key Input
                    OutlinedTextField(
                        value = if (apiKey == Constants.GEMINI_API_KEY) "" else apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("Enter your Gemini API key...") },
                        leadingIcon = { 
                            Icon(Icons.Default.Key, contentDescription = null) 
                        },
                        trailingIcon = {
                            Row {
                                IconButton(
                                    onClick = { isApiKeyVisible = !isApiKeyVisible }
                                ) {
                                    Icon(
                                        if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isApiKeyVisible) "Hide API key" else "Show API key"
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        if (apiKey.isNotBlank()) {
                                            if (apiKeyManager.isValidGeminiKey(apiKey)) {
                                                apiKeyManager.setGeminiApiKey(apiKey)
                                                isConfigured = apiKeyManager.isGeminiConfigured()
                                                showSuccessMessage = true
                                                errorMessage = null
                                            } else {
                                                errorMessage = "Invalid API key format. Please check your key and try again."
                                            }
                                        } else {
                                            errorMessage = "Please enter an API key."
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Save API key")
                                }
                            }
                        },
                        visualTransformation = if (isApiKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (apiKey.isNotBlank()) {
                                    if (apiKeyManager.isValidGeminiKey(apiKey)) {
                                        apiKeyManager.setGeminiApiKey(apiKey)
                                        isConfigured = apiKeyManager.isGeminiConfigured()
                                        showSuccessMessage = true
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Invalid API key format. Please check your key and try again."
                                    }
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text("Your API key is stored securely on your device. Toggle visibility to enter your key.")
                        },
                        singleLine = true
                    )
                    
                    // Quick action buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    apiKeyManager.clearAllKeys()
                                    apiKey = ""
                                    isConfigured = false
                                    errorMessage = null
                                    showSuccessMessage = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Clear, 
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear API Key")
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (apiKey.isNotBlank()) {
                                    if (apiKeyManager.isValidGeminiKey(apiKey)) {
                                        apiKeyManager.setGeminiApiKey(apiKey)
                                        isConfigured = apiKeyManager.isGeminiConfigured()
                                        showSuccessMessage = true
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Invalid API key format. Gemini API keys should start with 'AIza'. Please check your key."
                                    }
                                } else {
                                    errorMessage = "Please enter an API key."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = apiKey.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Save, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save API Key")
                        }
                    }
                }
            }
            
            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "How to Get Your API Key",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val instructions = listOf(
                            "1. Go to aistudio.google.com in your web browser",
                            "2. Sign in with your Google account",
                            "3. Click 'Get API key' in the left sidebar",
                            "4. Click 'Create API key' -> 'Create API key in new project'",
                            "5. Copy the generated API key (starts with 'AIza')",
                            "6. Enter the API key in the field above",
                            "7. Click 'Save API Key' to enable AI features"
                        )
                        
                        instructions.forEach { instruction ->
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Text(
                        text = "⚠️ Important: Keep your API key secure and don't share it with others.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Features Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConfigured) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
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
                            tint = if (isConfigured) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = Constants.ENHANCED_FEATURES_TITLE,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConfigured) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    
                    val features = listOf(
                        "🧠 AI-powered place information and insights",
                        "📝 Detailed historical and cultural context",
                        "❓ Smart quiz questions tailored to each location",
                        "✨ Enhanced descriptions with interesting facts",
                        "🎯 Adaptive difficulty levels for quizzes"
                    )
                    
                    features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isConfigured) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isConfigured) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    if (!isConfigured) {
                        Text(
                            text = "Configure your Gemini API key above to unlock these features!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
