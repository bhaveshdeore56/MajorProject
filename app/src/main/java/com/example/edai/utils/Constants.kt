package com.example.edai.utils

object Constants {

    // API Endpoints
    const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/api/rest_v1/"
    const val TRIVIA_BASE_URL = "https://opentdb.com/"
    const val OVERPASS_BASE_URL = "https://overpass-api.de/api/"

    // Default values
    const val DEFAULT_TRIVIA_AMOUNT = 5
    const val DEFAULT_SEARCH_LIMIT = 5
    const val DEFAULT_LOCATION_TIMEOUT = 30000L // 30 seconds

    // Location accuracy
    const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
    const val LOCATION_FASTEST_INTERVAL = 5000L // 5 seconds
    const val LOCATION_MIN_DISTANCE = 10f // 10 meters

    // Error messages
    const val ERROR_NO_INTERNET = "No internet connection available"
    const val ERROR_LOCATION_PERMISSION = "Location permission is required"
    const val ERROR_LOCATION_UNAVAILABLE = "Location not available"
    const val ERROR_API_FAILURE = "Failed to fetch data from server"
    const val ERROR_UNKNOWN = "An unknown error occurred"

    // Trivia categories
    const val TRIVIA_CATEGORY_GENERAL = 9
    const val TRIVIA_CATEGORY_GEOGRAPHY = 22
    const val TRIVIA_CATEGORY_HISTORY = 23

    // Gemini AI Configuration
    // IMPORTANT: Replace with your actual Gemini API key from https://aistudio.google.com
    // Or configure through the app's Settings screen
    const val GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE" // Replace with your actual key or configure in Settings
    const val GEMINI_MODEL = "gemini-1.5-flash"

    // Gemini AI Settings
    const val GEMINI_TEMPERATURE = 0.7f
    const val GEMINI_TOP_K = 40
    const val GEMINI_TOP_P = 0.95f
    const val GEMINI_MAX_OUTPUT_TOKENS = 2048
    const val GEMINI_MAX_RETRIES = 3
    const val GEMINI_RETRY_DELAY = 2000L // 2 seconds
    const val QUIZ_GENERATION_TIMEOUT = 30000L // 30 seconds
    const val MAX_QUIZ_QUESTIONS = 5
    const val MIN_QUIZ_QUESTIONS = 3

    // UI Constants
    const val SETTINGS_SCREEN_TITLE = "Settings"
    const val API_KEY_SECTION_TITLE = "Gemini API Key Configuration"
    const val ENHANCED_FEATURES_TITLE = "AI-Enhanced Features"
}