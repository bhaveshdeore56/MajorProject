package com.example.edai.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.edai.data.model.LocationInfo
import com.example.edai.ui.viewmodel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    locationViewModel: LocationViewModel,
    onNavigateToPlace: () -> Unit,
    onNavigateToPopularPlaces: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by locationViewModel.uiState.collectAsStateWithLifecycle()
    val searchResults by locationViewModel.searchResults.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var showManualSearch by remember { mutableStateOf(false) }
    
    // Location permission
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar with Settings
        TopAppBar(
            title = { Text("Edai - Educational Travel") },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subtitle
            Text(
                text = "Discover fascinating facts about places around you!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Location Detection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose Location Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Auto-detect button
                    Button(
                        onClick = {
                            if (locationPermissionState.status.isGranted) {
                                locationViewModel.detectCurrentLocation()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Detect Current Location")
                    }
                    
                    // Manual search toggle
                    OutlinedButton(
                        onClick = { showManualSearch = !showManualSearch },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enter Location Manually")
                    }
                }
            }
            
            // Popular Places Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Explore Pune",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Discover famous places, colleges, and landmarks in Pune",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    
                    Button(
                        onClick = onNavigateToPopularPlaces,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Popular Places in Pune")
                    }
                }
            }
            
            // Manual Search Section
            if (showManualSearch) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                if (it.isNotBlank()) {
                                    locationViewModel.searchLocation(it)
                                } else {
                                    locationViewModel.clearSearchResults()
                                }
                            },
                            label = { Text("Search for a place...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        locationViewModel.clearSearchResults()
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    if (searchQuery.isNotBlank()) {
                                        locationViewModel.searchLocation(searchQuery)
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isSearching
                        )
                        
                        if (uiState.isSearching) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
            
            // Search Results
            if (searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(searchResults.take(5)) { location ->
                                LocationResultItem(
                                    locationInfo = location,
                                    onSelect = {
                                        locationViewModel.selectLocation(location)
                                        searchQuery = ""
                                        showManualSearch = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Current Location Display
            uiState.currentLocation?.let { location ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Selected Location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = location.placeName,
                            style = MaterialTheme.typography.headlineSmall,
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
                        
                        Text(
                            text = "Lat: ${String.format("%.4f", location.latitude)}, " +
                                    "Lon: ${String.format("%.4f", location.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = onNavigateToPlace,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Explore This Place")
                        }
                    }
                }
            }
            
            // Error Display
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { locationViewModel.clearError() }
                        ) {
                            Text(
                                text = "Dismiss",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Permission explanation
            if (!locationPermissionState.status.isGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Location Permission",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Location permission is required to detect your current location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationResultItem(
    locationInfo: LocationInfo,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelect,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = locationInfo.placeName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            locationInfo.displayName?.let { displayName ->
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
