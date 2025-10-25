package com.example.edai.ui.components

import android.opengl.GLES20
// Removed JOML dependency - using Android's built-in OpenGL ES
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 3D Character Model with vertices, colors, and animations
 */
class CharacterModel {

    // Character parts
    private val headVertices: FloatArray
    private val bodyVertices: FloatArray
    private val armLeftVertices: FloatArray
    private val armRightVertices: FloatArray
    private val legLeftVertices: FloatArray
    private val legRightVertices: FloatArray

    // Colors for different parts
    private val headColors: FloatArray
    private val bodyColors: FloatArray
    private val armColors: FloatArray
    private val legColors: FloatArray

    // Animation properties
    private var headRotation = 0f
    private var armLeftRotation = 0f
    private var armRightRotation = 0f
    private var legLeftRotation = 0f
    private var legRightRotation = 0f

    init {
        // Initialize character parts
        headVertices = createHeadVertices()
        bodyVertices = createBodyVertices()
        armLeftVertices = createArmVertices()
        armRightVertices = createArmVertices()
        legLeftVertices = createLegVertices()
        legRightVertices = createLegVertices()

        // Initialize colors
        headColors = createHeadColors()
        bodyColors = createBodyColors()
        armColors = createArmColors()
        legColors = createLegColors()
    }

    private fun createHeadVertices(): FloatArray {
        // Create a sphere-like head (simplified as cube for now)
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

    private fun createBodyVertices(): FloatArray {
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

    private fun createArmVertices(): FloatArray {
        return floatArrayOf(
            // Front face
            -0.1f, -0.1f, 0.1f,
            0.1f, -0.1f, 0.1f,
            0.1f, 0.4f, 0.1f,
            0.1f, 0.4f, 0.1f,
            -0.1f, 0.4f, 0.1f,
            -0.1f, -0.1f, 0.1f,

            // Back face
            -0.1f, -0.1f, -0.1f,
            -0.1f, 0.4f, -0.1f,
            0.1f, 0.4f, -0.1f,
            0.1f, 0.4f, -0.1f,
            0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f,

            // Left face
            -0.1f, 0.4f, -0.1f,
            -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, 0.1f,
            -0.1f, -0.1f, 0.1f,
            -0.1f, 0.4f, 0.1f,
            -0.1f, 0.4f, -0.1f,

            // Right face
            0.1f, 0.4f, -0.1f,
            0.1f, 0.4f, 0.1f,
            0.1f, -0.1f, 0.1f,
            0.1f, -0.1f, 0.1f,
            0.1f, -0.1f, -0.1f,
            0.1f, 0.4f, -0.1f,

            // Top face
            -0.1f, 0.4f, -0.1f,
            0.1f, 0.4f, -0.1f,
            0.1f, 0.4f, 0.1f,
            0.1f, 0.4f, 0.1f,
            -0.1f, 0.4f, 0.1f,
            -0.1f, 0.4f, -0.1f,

            // Bottom face
            -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, 0.1f,
            0.1f, -0.1f, 0.1f,
            0.1f, -0.1f, 0.1f,
            0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f
        )
    }

    private fun createLegVertices(): FloatArray {
        return floatArrayOf(
            // Front face
            -0.1f, -0.4f, 0.1f,
            0.1f, -0.4f, 0.1f,
            0.1f, 0.0f, 0.1f,
            0.1f, 0.0f, 0.1f,
            -0.1f, 0.0f, 0.1f,
            -0.1f, -0.4f, 0.1f,

            // Back face
            -0.1f, -0.4f, -0.1f,
            -0.1f, 0.0f, -0.1f,
            0.1f, 0.0f, -0.1f,
            0.1f, 0.0f, -0.1f,
            0.1f, -0.4f, -0.1f,
            -0.1f, -0.4f, -0.1f,

            // Left face
            -0.1f, 0.0f, -0.1f,
            -0.1f, -0.4f, -0.1f,
            -0.1f, -0.4f, 0.1f,
            -0.1f, -0.4f, 0.1f,
            -0.1f, 0.0f, 0.1f,
            -0.1f, 0.0f, -0.1f,

            // Right face
            0.1f, 0.0f, -0.1f,
            0.1f, 0.0f, 0.1f,
            0.1f, -0.4f, 0.1f,
            0.1f, -0.4f, 0.1f,
            0.1f, -0.4f, -0.1f,
            0.1f, 0.0f, -0.1f,

            // Top face
            -0.1f, 0.0f, -0.1f,
            0.1f, 0.0f, -0.1f,
            0.1f, 0.0f, 0.1f,
            0.1f, 0.0f, 0.1f,
            -0.1f, 0.0f, 0.1f,
            -0.1f, 0.0f, -0.1f,

            // Bottom face
            -0.1f, -0.4f, -0.1f,
            -0.1f, -0.4f, 0.1f,
            0.1f, -0.4f, 0.1f,
            0.1f, -0.4f, 0.1f,
            0.1f, -0.4f, -0.1f,
            -0.1f, -0.4f, -0.1f
        )
    }

    private fun createHeadColors(): FloatArray {
        val color = floatArrayOf(1.0f, 0.8f, 0.6f, 1.0f) // Skin color
        return createColorArray(color, headVertices.size / 3)
    }

    private fun createBodyColors(): FloatArray {
        val color = floatArrayOf(0.2f, 0.4f, 0.8f, 1.0f) // Blue shirt
        return createColorArray(color, bodyVertices.size / 3)
    }

    private fun createArmColors(): FloatArray {
        val color = floatArrayOf(1.0f, 0.8f, 0.6f, 1.0f) // Skin color
        return createColorArray(color, armLeftVertices.size / 3)
    }

    private fun createLegColors(): FloatArray {
        val color = floatArrayOf(0.3f, 0.3f, 0.3f, 1.0f) // Dark pants
        return createColorArray(color, legLeftVertices.size / 3)
    }

    private fun createColorArray(color: FloatArray, vertexCount: Int): FloatArray {
        val colors = FloatArray(vertexCount * 4)
        for (i in 0 until vertexCount) {
            colors[i * 4] = color[0]
            colors[i * 4 + 1] = color[1]
            colors[i * 4 + 2] = color[2]
            colors[i * 4 + 3] = color[3]
        }
        return colors
    }

    fun updateAnimation(animationState: CharacterAnimationState, time: Float) {
        when (animationState) {
            CharacterAnimationState.IDLE -> {
                headRotation = (Math.sin(time.toDouble() * 0.5) * 5).toFloat()
                armLeftRotation = (Math.sin(time.toDouble() * 0.3) * 10).toFloat()
                armRightRotation = (Math.sin(time.toDouble() * 0.3 + Math.PI) * 10).toFloat()
                legLeftRotation = (Math.sin(time.toDouble() * 0.4) * 5).toFloat()
                legRightRotation = (Math.sin(time.toDouble() * 0.4 + Math.PI) * 5).toFloat()
            }
            CharacterAnimationState.THINKING -> {
                headRotation = (Math.sin(time.toDouble() * 2) * 15).toFloat()
                armLeftRotation = 30f + (Math.sin(time.toDouble() * 1) * 10).toFloat()
                armRightRotation = 30f + (Math.sin(time.toDouble() * 1) * 10).toFloat()
                legLeftRotation = 0f
                legRightRotation = 0f
            }
            CharacterAnimationState.HAPPY -> {
                headRotation = (Math.sin(time.toDouble() * 3) * 20).toFloat()
                armLeftRotation = 45f + (Math.sin(time.toDouble() * 4) * 30).toFloat()
                armRightRotation = 45f + (Math.sin(time.toDouble() * 4) * 30).toFloat()
                legLeftRotation = (Math.sin(time.toDouble() * 2) * 15).toFloat()
                legRightRotation = (Math.sin(time.toDouble() * 2 + Math.PI) * 15).toFloat()
            }
            CharacterAnimationState.SAD -> {
                headRotation = -10f
                armLeftRotation = -20f
                armRightRotation = -20f
                legLeftRotation = 0f
                legRightRotation = 0f
            }
            CharacterAnimationState.CELEBRATING -> {
                headRotation = (Math.sin(time.toDouble() * 5) * 30).toFloat()
                armLeftRotation = 90f + (Math.sin(time.toDouble() * 6) * 45).toFloat()
                armRightRotation = 90f + (Math.sin(time.toDouble() * 6) * 45).toFloat()
                legLeftRotation = (Math.sin(time.toDouble() * 4) * 20).toFloat()
                legRightRotation = (Math.sin(time.toDouble() * 4 + Math.PI) * 20).toFloat()
            }
            CharacterAnimationState.ENCOURAGING -> {
                headRotation = (Math.sin(time.toDouble() * 1) * 10).toFloat()
                armLeftRotation = 60f + (Math.sin(time.toDouble() * 2) * 20).toFloat()
                armRightRotation = 60f + (Math.sin(time.toDouble() * 2) * 20).toFloat()
                legLeftRotation = (Math.sin(time.toDouble() * 1.5) * 8).toFloat()
                legRightRotation = (Math.sin(time.toDouble() * 1.5 + Math.PI) * 8).toFloat()
            }
            CharacterAnimationState.CORRECT_ANSWER -> {
                headRotation = (Math.sin(time.toDouble() * 8) * 25).toFloat()
                armLeftRotation = 90f + (Math.sin(time.toDouble() * 10) * 30).toFloat()
                armRightRotation = 90f + (Math.sin(time.toDouble() * 10) * 30).toFloat()
                legLeftRotation = (Math.sin(time.toDouble() * 6) * 15).toFloat()
                legRightRotation = (Math.sin(time.toDouble() * 6 + Math.PI) * 15).toFloat()
            }
            CharacterAnimationState.WRONG_ANSWER -> {
                headRotation = (Math.sin(time.toDouble() * 12) * 10).toFloat()
                armLeftRotation = -30f + (Math.sin(time.toDouble() * 15) * 20).toFloat()
                armRightRotation = -30f + (Math.sin(time.toDouble() * 15) * 20).toFloat()
                legLeftRotation = (Math.sin(time.toDouble() * 8) * 5).toFloat()
                legRightRotation = (Math.sin(time.toDouble() * 8 + Math.PI) * 5).toFloat()
            }
        }
    }

    fun getHeadVertices(): FloatArray = headVertices
    fun getBodyVertices(): FloatArray = bodyVertices
    fun getArmLeftVertices(): FloatArray = armLeftVertices
    fun getArmRightVertices(): FloatArray = armRightVertices
    fun getLegLeftVertices(): FloatArray = legLeftVertices
    fun getLegRightVertices(): FloatArray = legRightVertices

    fun getHeadColors(): FloatArray = headColors
    fun getBodyColors(): FloatArray = bodyColors
    fun getArmColors(): FloatArray = armColors
    fun getLegColors(): FloatArray = legColors

    fun getHeadRotation(): Float = headRotation
    fun getArmLeftRotation(): Float = armLeftRotation
    fun getArmRightRotation(): Float = armRightRotation
    fun getLegLeftRotation(): Float = legLeftRotation
    fun getLegRightRotation(): Float = legRightRotation
}
