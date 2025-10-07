package com.example.edai.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages API keys securely using SharedPreferences
 */
class ApiKeyManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "edai_api_keys", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_CONFIGURED = "key_configured"
        
        // Default API key placeholder
        private const val DEFAULT_PLACEHOLDER = "YOUR_GEMINI_API_KEY_HERE"
        
        @Volatile
        private var INSTANCE: ApiKeyManager? = null
        
        fun getInstance(context: Context): ApiKeyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiKeyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Set the Gemini API key
     */
    fun setGeminiApiKey(apiKey: String) {
        prefs.edit()
            .putString(GEMINI_API_KEY, apiKey)
            .putBoolean(KEY_CONFIGURED, apiKey.isNotEmpty() && apiKey != DEFAULT_PLACEHOLDER)
            .apply()
    }
    
    /**
     * Get the Gemini API key
     */
    fun getGeminiApiKey(): String {
        return prefs.getString(GEMINI_API_KEY, Constants.GEMINI_API_KEY) ?: Constants.GEMINI_API_KEY
    }
    
    /**
     * Check if Gemini API key is properly configured
     */
    fun isGeminiConfigured(): Boolean {
        val key = getGeminiApiKey()
        return key.isNotEmpty() && 
               key != DEFAULT_PLACEHOLDER && 
               key != Constants.GEMINI_API_KEY &&
               key.length >= 20 &&  // Changed from > to >=
               key.startsWith("AIza")  // Gemini keys start with "AIza"
    }
    
    /**
     * Clear all API keys
     */
    fun clearAllKeys() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Get configuration status message
     */
    fun getConfigurationMessage(): String {
        return if (isGeminiConfigured()) {
            "✅ Gemini AI is properly configured and ready to use!"
        } else {
            "⚠️ Please configure your Gemini API key to enable AI features.\n\n" +
            "How to get your API key:\n" +
            "1. Go to aistudio.google.com\n" +
            "2. Sign in with your Google account\n" +
            "3. Click 'Get API key' in the sidebar\n" +
            "4. Create a new API key in new project\n" +
            "5. Copy the key (starts with 'AIza')\n" +
            "6. Enter it in the settings field"
        }
    }
    
    /**
     * Validate API key format
     */
    fun isValidGeminiKey(key: String): Boolean {
        return key.isNotEmpty() && 
               key != DEFAULT_PLACEHOLDER &&
               key.length >= 20 &&  // Changed from > to >=
               key.startsWith("AIza") &&
               key.matches(Regex("^[A-Za-z0-9_-]+$"))
    }
}
