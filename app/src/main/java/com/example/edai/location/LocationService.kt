package com.example.edai.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }
    
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMinUpdateDistanceMeters(10f) // 10 meters
            setMinUpdateIntervalMillis(5000L) // 5 seconds
        }.build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    trySend(location)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    suspend fun requestSingleLocationUpdate(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return suspendCancellableCoroutine { continuation ->
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                0L
            ).build()
            
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(result.lastLocation)
                }
            }
            
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
            
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}