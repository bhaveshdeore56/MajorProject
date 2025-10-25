package com.example.edai.ui.components

import android.content.Context
import android.opengl.GLES20
import android.util.Log

/**
 * WebGL Performance Manager for optimizing 3D character rendering
 */
class WebGLPerformanceManager(private val context: Context) {

    companion object {
        private const val TAG = "WebGLPerformance"
        private const val TARGET_FPS = 60
        private const val MAX_VERTICES = 1000
        private const val MAX_TEXTURES = 4
    }

    private var isLowEndDevice = false
    private var maxTextureSize = 1024
    private var maxVertexCount = MAX_VERTICES
    private var frameRate = 60f
    private var frameCount = 0
    private var lastFrameTime = 0L

    init {
        detectDeviceCapabilities()
    }

    private fun detectDeviceCapabilities() {
        // Check device capabilities
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemory = memoryInfo.totalMem
        val availableMemory = memoryInfo.availMem

        // Determine if device is low-end
        isLowEndDevice = totalMemory < 2L * 1024 * 1024 * 1024 // Less than 2GB RAM

        // Adjust settings based on device capabilities
        if (isLowEndDevice) {
            maxVertexCount = MAX_VERTICES / 2
            maxTextureSize = 512
            Log.d(TAG, "Low-end device detected. Reducing quality settings.")
        } else {
            maxVertexCount = MAX_VERTICES
            maxTextureSize = 1024
            Log.d(TAG, "High-end device detected. Using full quality settings.")
        }

        Log.d(TAG, "Device capabilities: RAM=${totalMemory / (1024 * 1024)}MB, " +
                "Available=${availableMemory / (1024 * 1024)}MB, " +
                "LowEnd=$isLowEndDevice")
    }

    fun optimizeForDevice(): WebGLQualitySettings {
        return WebGLQualitySettings(
            maxVertices = maxVertexCount,
            maxTextureSize = maxTextureSize,
            enableLighting = !isLowEndDevice,
            enableShadows = false, // Disabled for performance
            enableAntiAliasing = !isLowEndDevice,
            frameRate = if (isLowEndDevice) 30 else 60,
            enableTransparency = !isLowEndDevice
        )
    }

    fun updateFrameRate() {
        val currentTime = System.currentTimeMillis()
        frameCount++

        if (currentTime - lastFrameTime >= 1000) { // Update every second
            frameRate = frameCount.toFloat()
            frameCount = 0
            lastFrameTime = currentTime

            // Adjust quality based on performance
            if (frameRate < 30 && !isLowEndDevice) {
                Log.w(TAG, "Low frame rate detected: $frameRate FPS. Consider reducing quality.")
            }
        }
    }

    fun shouldReduceQuality(): Boolean {
        return frameRate < 30 || isLowEndDevice
    }

    fun getOptimalRenderMode(): Int {
        return if (shouldReduceQuality()) {
            android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
        } else {
            android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    fun optimizeShaderCode(baseShader: String): String {
        return if (isLowEndDevice) {
            // Remove complex calculations for low-end devices
            baseShader
                .replace("precision highp float;", "precision mediump float;")
                .replace("precision highp int;", "precision mediump int;")
                .replace("varying vec3 vNormal;", "")
                .replace("varying vec3 vLightDirection;", "")
                .replace("uniform vec4 uLightPosition;", "")
                .replace("uniform vec4 uLightColor;", "")
        } else {
            baseShader
        }
    }

    fun getMaxVerticesForPart(): Int {
        return maxVertexCount / 6 // Divide by number of character parts
    }

    fun shouldUseSimplifiedModel(): Boolean {
        return isLowEndDevice || frameRate < 45
    }

    fun getOptimalAnimationSpeed(): Float {
        return if (isLowEndDevice) 0.5f else 1.0f
    }
}

/**
 * WebGL Quality Settings
 */
data class WebGLQualitySettings(
    val maxVertices: Int,
    val maxTextureSize: Int,
    val enableLighting: Boolean,
    val enableShadows: Boolean,
    val enableAntiAliasing: Boolean,
    val frameRate: Int,
    val enableTransparency: Boolean
) {
    fun shouldUseSimplifiedModel(): Boolean {
        return !enableLighting || maxVertices < 500
    }
}

/**
 * Optimized Character Model for different quality levels
 */
class OptimizedCharacterModel(private val qualitySettings: WebGLQualitySettings) {

    fun getHeadVertices(): FloatArray {
        return if (shouldUseSimplifiedModel()) {
            createSimplifiedHeadVertices()
        } else {
            createDetailedHeadVertices()
        }
    }

    fun getBodyVertices(): FloatArray {
        return if (shouldUseSimplifiedModel()) {
            createSimplifiedBodyVertices()
        } else {
            createDetailedBodyVertices()
        }
    }

    private fun createSimplifiedHeadVertices(): FloatArray {
        // Simplified cube head
        return floatArrayOf(
            // Front face
            -0.2f, 0.3f, 0.2f,
            0.2f, 0.3f, 0.2f,
            0.2f, 0.7f, 0.2f,
            0.2f, 0.7f, 0.2f,
            -0.2f, 0.7f, 0.2f,
            -0.2f, 0.3f, 0.2f,

            // Back face
            -0.2f, 0.3f, -0.2f,
            -0.2f, 0.7f, -0.2f,
            0.2f, 0.7f, -0.2f,
            0.2f, 0.7f, -0.2f,
            0.2f, 0.3f, -0.2f,
            -0.2f, 0.3f, -0.2f,

            // Left face
            -0.2f, 0.7f, -0.2f,
            -0.2f, 0.3f, -0.2f,
            -0.2f, 0.3f, 0.2f,
            -0.2f, 0.3f, 0.2f,
            -0.2f, 0.7f, 0.2f,
            -0.2f, 0.7f, -0.2f,

            // Right face
            0.2f, 0.7f, -0.2f,
            0.2f, 0.7f, 0.2f,
            0.2f, 0.3f, 0.2f,
            0.2f, 0.3f, 0.2f,
            0.2f, 0.3f, -0.2f,
            0.2f, 0.7f, -0.2f,

            // Top face
            -0.2f, 0.7f, -0.2f,
            0.2f, 0.7f, -0.2f,
            0.2f, 0.7f, 0.2f,
            0.2f, 0.7f, 0.2f,
            -0.2f, 0.7f, 0.2f,
            -0.2f, 0.7f, -0.2f,

            // Bottom face
            -0.2f, 0.3f, -0.2f,
            -0.2f, 0.3f, 0.2f,
            0.2f, 0.3f, 0.2f,
            0.2f, 0.3f, 0.2f,
            0.2f, 0.3f, -0.2f,
            -0.2f, 0.3f, -0.2f
        )
    }

    private fun createDetailedHeadVertices(): FloatArray {
        // More detailed head with more vertices
        val vertices = mutableListOf<Float>()

        // Create a more spherical head
        val segments = 8
        val rings = 4

        for (ring in 0..rings) {
            val phi = Math.PI * ring / rings
            for (segment in 0..segments) {
                val theta = 2.0 * Math.PI * segment / segments

                val x = (0.2 * Math.sin(phi) * Math.cos(theta)).toFloat()
                val y = (0.2 * Math.cos(phi) + 0.5).toFloat()
                val z = (0.2 * Math.sin(phi) * Math.sin(theta)).toFloat()

                vertices.addAll(listOf(x, y, z))
            }
        }

        return vertices.toFloatArray()
    }

    private fun createSimplifiedBodyVertices(): FloatArray {
        // Simplified body
        return floatArrayOf(
            // Front face
            -0.3f, -0.3f, 0.2f,
            0.3f, -0.3f, 0.2f,
            0.3f, 0.3f, 0.2f,
            0.3f, 0.3f, 0.2f,
            -0.3f, 0.3f, 0.2f,
            -0.3f, -0.3f, 0.2f,

            // Back face
            -0.3f, -0.3f, -0.2f,
            -0.3f, 0.3f, -0.2f,
            0.3f, 0.3f, -0.2f,
            0.3f, 0.3f, -0.2f,
            0.3f, -0.3f, -0.2f,
            -0.3f, -0.3f, -0.2f,

            // Left face
            -0.3f, 0.3f, -0.2f,
            -0.3f, -0.3f, -0.2f,
            -0.3f, -0.3f, 0.2f,
            -0.3f, -0.3f, 0.2f,
            -0.3f, 0.3f, 0.2f,
            -0.3f, 0.3f, -0.2f,

            // Right face
            0.3f, 0.3f, -0.2f,
            0.3f, 0.3f, 0.2f,
            0.3f, -0.3f, 0.2f,
            0.3f, -0.3f, 0.2f,
            0.3f, -0.3f, -0.2f,
            0.3f, 0.3f, -0.2f,

            // Top face
            -0.3f, 0.3f, -0.2f,
            0.3f, 0.3f, -0.2f,
            0.3f, 0.3f, 0.2f,
            0.3f, 0.3f, 0.2f,
            -0.3f, 0.3f, 0.2f,
            -0.3f, 0.3f, -0.2f,

            // Bottom face
            -0.3f, -0.3f, -0.2f,
            -0.3f, -0.3f, 0.2f,
            0.3f, -0.3f, 0.2f,
            0.3f, -0.3f, 0.2f,
            0.3f, -0.3f, -0.2f,
            -0.3f, -0.3f, -0.2f
        )
    }

    private fun createDetailedBodyVertices(): FloatArray {
        // More detailed body with curves
        val vertices = mutableListOf<Float>()

        // Create a more human-like body shape
        val segments = 6
        val rings = 4

        for (ring in 0..rings) {
            val phi = Math.PI * ring / rings
            for (segment in 0..segments) {
                val theta = 2.0 * Math.PI * segment / segments

                val radius = 0.3 * (1.0 - 0.2 * Math.cos(phi)) // Tapered shape
                val x = (radius * Math.sin(phi) * Math.cos(theta)).toFloat()
                val y = (0.3 * Math.cos(phi)).toFloat()
                val z = (radius * Math.sin(phi) * Math.sin(theta)).toFloat()

                vertices.addAll(listOf(x, y, z))
            }
        }

        return vertices.toFloatArray()
    }

    fun shouldUseSimplifiedModel(): Boolean {
        return qualitySettings.shouldUseSimplifiedModel()
    }

    fun getQualitySettings(): WebGLQualitySettings {
        return qualitySettings
    }
}
