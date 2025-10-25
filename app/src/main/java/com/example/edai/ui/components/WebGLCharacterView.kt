package com.example.edai.ui.components

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
// Removed JOML dependency - using Android's built-in OpenGL ES
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * WebGL Character Animation States
 */
enum class CharacterAnimationState {
    IDLE,
    THINKING,
    HAPPY,
    SAD,
    CELEBRATING,
    ENCOURAGING,
    CORRECT_ANSWER,
    WRONG_ANSWER
}

/**
 * WebGL Character View for animated 3D characters in quiz
 */
@Composable
fun WebGLCharacterView(
    animationState: CharacterAnimationState = CharacterAnimationState.IDLE,
    modifier: Modifier = Modifier,
    onCharacterReady: (() -> Unit)? = null
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            WebGLCharacterGLSurfaceView(ctx).apply {
                setAnimationState(animationState)
                onCharacterReady?.let { callback ->
                    setOnCharacterReadyCallback(callback)
                }
            }
        },
        update = { view ->
            view.setAnimationState(animationState)
        },
        modifier = modifier
    )
}

/**
 * Custom GLSurfaceView for WebGL character rendering
 */
class WebGLCharacterGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private var renderer: EnhancedWebGLRenderer? = null
    private var onCharacterReadyCallback: (() -> Unit)? = null

    init {
        // Set OpenGL ES version
        setEGLContextClientVersion(2)

        // Set renderer
        renderer = EnhancedWebGLRenderer()
        setRenderer(renderer)

        // Set render mode to continuous
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun setAnimationState(state: CharacterAnimationState) {
        renderer?.setAnimationState(state)
    }

    fun setOnCharacterReadyCallback(callback: (() -> Unit)?) {
        onCharacterReadyCallback = callback
    }

    fun getOnCharacterReadyCallback(): (() -> Unit)? = onCharacterReadyCallback
}

/**
 * WebGL Character Renderer
 */
class WebGLCharacterRenderer : GLSurfaceView.Renderer {
    private var animationState = CharacterAnimationState.IDLE
    private var animationTime = 0f
    private var characterReady = false

    // Matrices
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Shader programs
    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0
    private var shaderProgram: Int = 0

    // Character properties
    private var characterScale = 1.0f
    private var characterRotation = 0f
    private var characterPosition = floatArrayOf(0f, 0f, -3f)

    // Animation properties
    private var bounceOffset = 0f
    private var rotationSpeed = 0f
    private var scalePulse = 1.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set background color (transparent for overlay)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Enable blending for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Initialize shaders
        initializeShaders()

        characterReady = true
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Create projection matrix
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear the screen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Update animation
        updateAnimation()

        // Set up view matrix
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f)

        // Calculate MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Render character
        renderCharacter()
    }

    private fun initializeShaders() {
        val vertexShaderCode = """
            attribute vec4 vPosition;
            attribute vec4 vColor;
            uniform mat4 uMVPMatrix;
            varying vec4 vColorVarying;
            
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vColorVarying = vColor;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec4 vColorVarying;
            
            void main() {
                gl_FragColor = vColorVarying;
            }
        """.trimIndent()

        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun updateAnimation() {
        animationTime += 0.016f // Assuming 60 FPS

        when (animationState) {
            CharacterAnimationState.IDLE -> {
                // Gentle floating animation
                bounceOffset = (Math.sin(animationTime.toDouble() * 2) * 0.1).toFloat()
                rotationSpeed = 0.5f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 1.5) * 0.05).toFloat()
            }
            CharacterAnimationState.THINKING -> {
                // Slow rotation with slight scale change
                bounceOffset = (Math.sin(animationTime.toDouble() * 3) * 0.15).toFloat()
                rotationSpeed = 1.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 2) * 0.1).toFloat()
            }
            CharacterAnimationState.HAPPY -> {
                // Bouncy animation
                bounceOffset = (Math.sin(animationTime.toDouble() * 4) * 0.2).toFloat()
                rotationSpeed = 2.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 3) * 0.15).toFloat()
            }
            CharacterAnimationState.SAD -> {
                // Slow, droopy animation
                bounceOffset = (Math.sin(animationTime.toDouble() * 1) * 0.05).toFloat()
                rotationSpeed = 0.2f
                scalePulse = 0.9f + (Math.sin(animationTime.toDouble() * 0.5) * 0.05).toFloat()
            }
            CharacterAnimationState.CELEBRATING -> {
                // Fast, energetic animation
                bounceOffset = (Math.sin(animationTime.toDouble() * 6) * 0.3).toFloat()
                rotationSpeed = 4.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 5) * 0.2).toFloat()
            }
            CharacterAnimationState.ENCOURAGING -> {
                // Gentle, supportive animation
                bounceOffset = (Math.sin(animationTime.toDouble() * 2.5) * 0.12).toFloat()
                rotationSpeed = 1.5f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 2) * 0.08).toFloat()
            }
            CharacterAnimationState.CORRECT_ANSWER -> {
                // Quick celebration
                bounceOffset = (Math.sin(animationTime.toDouble() * 8) * 0.25).toFloat()
                rotationSpeed = 3.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 6) * 0.18).toFloat()
            }
            CharacterAnimationState.WRONG_ANSWER -> {
                // Shake animation
                bounceOffset = (Math.sin(animationTime.toDouble() * 10) * 0.1).toFloat()
                rotationSpeed = 0.5f
                scalePulse = 0.95f + (Math.sin(animationTime.toDouble() * 8) * 0.05).toFloat()
            }
        }

        characterRotation += rotationSpeed
    }

    private fun renderCharacter() {
        // Use shader program
        GLES20.glUseProgram(shaderProgram)

        // Create a simple character (cube for now, can be replaced with more complex models)
        val characterVertices = createCharacterVertices()
        val characterColors = createCharacterColors()

        // Set up vertex buffer
        val vertexBuffer = java.nio.ByteBuffer.allocateDirect(characterVertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(characterVertices)
        vertexBuffer.position(0)

        val colorBuffer = java.nio.ByteBuffer.allocateDirect(characterColors.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        colorBuffer.put(characterColors)
        colorBuffer.position(0)

        // Get attribute locations
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(shaderProgram, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")

        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)

        // Set vertex attributes
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

        // Apply transformations
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0,
            characterPosition[0],
            characterPosition[1] + bounceOffset,
            characterPosition[2])
        Matrix.rotateM(modelMatrix, 0, characterRotation, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, characterScale * scalePulse, characterScale * scalePulse, characterScale * scalePulse)

        // Calculate final MVP matrix
        val tempMatrix = FloatArray(16)
        Matrix.multiplyMM(tempMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, tempMatrix, 0)

        // Draw the character
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, characterVertices.size / 3)

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun createCharacterVertices(): FloatArray {
        // Create a simple cube character
        return floatArrayOf(
            // Front face
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,

            // Back face
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,

            // Left face
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,

            // Right face
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,

            // Top face
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,

            // Bottom face
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f
        )
    }

    private fun createCharacterColors(): FloatArray {
        // Create colors based on animation state
        val baseColor = when (animationState) {
            CharacterAnimationState.HAPPY, CharacterAnimationState.CELEBRATING, CharacterAnimationState.CORRECT_ANSWER ->
                floatArrayOf(0.0f, 1.0f, 0.0f, 1.0f) // Green
            CharacterAnimationState.SAD, CharacterAnimationState.WRONG_ANSWER ->
                floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f) // Red
            CharacterAnimationState.THINKING ->
                floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f) // Blue
            CharacterAnimationState.ENCOURAGING ->
                floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f) // Yellow
            else ->
                floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f) // Gray
        }

        // Create color array for all vertices
        val colors = FloatArray(72 * 4) // 36 vertices * 4 components (RGBA)
        for (i in colors.indices step 4) {
            colors[i] = baseColor[0]
            colors[i + 1] = baseColor[1]
            colors[i + 2] = baseColor[2]
            colors[i + 3] = baseColor[3]
        }

        return colors
    }

    fun setAnimationState(state: CharacterAnimationState) {
        animationState = state
        animationTime = 0f // Reset animation time for new state
    }

    fun isCharacterReady(): Boolean = characterReady
}