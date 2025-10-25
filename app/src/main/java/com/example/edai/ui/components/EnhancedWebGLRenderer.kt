package com.example.edai.ui.components

import android.opengl.GLES20
import android.opengl.Matrix
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Enhanced WebGL Renderer with sophisticated character animations
 */
class EnhancedWebGLRenderer : GLSurfaceView.Renderer {
    private var animationState = CharacterAnimationState.IDLE
    private var animationTime = 0f
    private var characterModel: CharacterModel? = null
    private var performanceManager: WebGLPerformanceManager? = null
    private var qualitySettings: WebGLQualitySettings? = null

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
    private var colorIntensity = 1.0f

    // Lighting properties
    private var lightPosition = floatArrayOf(0f, 2f, 0f, 1f)
    private var lightColor = floatArrayOf(1f, 1f, 1f, 1f)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set background color (transparent for overlay)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Enable blending for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Enable face culling for better performance
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        // Initialize with default quality settings
        qualitySettings = WebGLQualitySettings(
            maxVertices = 1000,
            maxTextureSize = 1024,
            enableLighting = true,
            enableShadows = false,
            enableAntiAliasing = true,
            frameRate = 60,
            enableTransparency = true
        )

        // Initialize character model
        characterModel = CharacterModel()

        // Initialize shaders
        initializeShaders()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Create projection matrix with better perspective
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 2f, 8f)
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

        // Render character parts based on quality settings
        characterModel?.let { model ->
            if (qualitySettings?.enableLighting == true) {
                renderCharacterPartWithLighting(model.getHeadVertices(), model.getHeadColors(),
                    model.getHeadRotation(), floatArrayOf(0f, 0.5f, 0f))
                renderCharacterPartWithLighting(model.getBodyVertices(), model.getBodyColors(),
                    0f, floatArrayOf(0f, 0f, 0f))
                renderCharacterPartWithLighting(model.getArmLeftVertices(), model.getArmColors(),
                    model.getArmLeftRotation(), floatArrayOf(-0.4f, 0.2f, 0f))
                renderCharacterPartWithLighting(model.getArmRightVertices(), model.getArmColors(),
                    model.getArmRightRotation(), floatArrayOf(0.4f, 0.2f, 0f))
                renderCharacterPartWithLighting(model.getLegLeftVertices(), model.getLegColors(),
                    model.getLegLeftRotation(), floatArrayOf(-0.15f, -0.4f, 0f))
                renderCharacterPartWithLighting(model.getLegRightVertices(), model.getLegColors(),
                    model.getLegRightRotation(), floatArrayOf(0.15f, -0.4f, 0f))
            } else {
                renderCharacterPartSimple(model.getHeadVertices(), model.getHeadColors(),
                    model.getHeadRotation(), floatArrayOf(0f, 0.5f, 0f))
                renderCharacterPartSimple(model.getBodyVertices(), model.getBodyColors(),
                    0f, floatArrayOf(0f, 0f, 0f))
                renderCharacterPartSimple(model.getArmLeftVertices(), model.getArmColors(),
                    model.getArmLeftRotation(), floatArrayOf(-0.4f, 0.2f, 0f))
                renderCharacterPartSimple(model.getArmRightVertices(), model.getArmColors(),
                    model.getArmRightRotation(), floatArrayOf(0.4f, 0.2f, 0f))
                renderCharacterPartSimple(model.getLegLeftVertices(), model.getLegColors(),
                    model.getLegLeftRotation(), floatArrayOf(-0.15f, -0.4f, 0f))
                renderCharacterPartSimple(model.getLegRightVertices(), model.getLegColors(),
                    model.getLegRightRotation(), floatArrayOf(0.15f, -0.4f, 0f))
            }
        }
    }

    private fun initializeShaders() {
        val vertexShaderCode = """
            attribute vec4 vPosition;
            attribute vec4 vColor;
            uniform mat4 uMVPMatrix;
            uniform vec4 uLightPosition;
            uniform vec4 uLightColor;
            varying vec4 vColorVarying;
            varying vec3 vNormal;
            varying vec3 vLightDirection;
            
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vColorVarying = vColor;
                
                // Simple normal calculation (for a cube)
                vec3 position = vPosition.xyz;
                vNormal = normalize(position);
                
                // Calculate light direction
                vLightDirection = normalize(uLightPosition.xyz - position);
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec4 vColorVarying;
            varying vec3 vNormal;
            varying vec3 vLightDirection;
            uniform vec4 uLightColor;
            
            void main() {
                // Simple diffuse lighting
                float diff = max(dot(vNormal, vLightDirection), 0.0);
                vec4 lighting = uLightColor * diff;
                
                gl_FragColor = vColorVarying * (0.3 + 0.7 * lighting);
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

        characterModel?.updateAnimation(animationState, animationTime)

        when (animationState) {
            CharacterAnimationState.IDLE -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 2) * 0.1).toFloat()
                rotationSpeed = 0.5f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 1.5) * 0.05).toFloat()
                colorIntensity = 1.0f
            }
            CharacterAnimationState.THINKING -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 3) * 0.15).toFloat()
                rotationSpeed = 1.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 2) * 0.1).toFloat()
                colorIntensity = 0.8f
            }
            CharacterAnimationState.HAPPY -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 4) * 0.2).toFloat()
                rotationSpeed = 2.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 3) * 0.15).toFloat()
                colorIntensity = 1.2f
            }
            CharacterAnimationState.SAD -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 1) * 0.05).toFloat()
                rotationSpeed = 0.2f
                scalePulse = 0.9f + (Math.sin(animationTime.toDouble() * 0.5) * 0.05).toFloat()
                colorIntensity = 0.7f
            }
            CharacterAnimationState.CELEBRATING -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 6) * 0.3).toFloat()
                rotationSpeed = 4.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 5) * 0.2).toFloat()
                colorIntensity = 1.5f
            }
            CharacterAnimationState.ENCOURAGING -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 2.5) * 0.12).toFloat()
                rotationSpeed = 1.5f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 2) * 0.08).toFloat()
                colorIntensity = 1.1f
            }
            CharacterAnimationState.CORRECT_ANSWER -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 8) * 0.25).toFloat()
                rotationSpeed = 3.0f
                scalePulse = 1.0f + (Math.sin(animationTime.toDouble() * 6) * 0.18).toFloat()
                colorIntensity = 1.3f
            }
            CharacterAnimationState.WRONG_ANSWER -> {
                bounceOffset = (Math.sin(animationTime.toDouble() * 10) * 0.1).toFloat()
                rotationSpeed = 0.5f
                scalePulse = 0.95f + (Math.sin(animationTime.toDouble() * 8) * 0.05).toFloat()
                colorIntensity = 0.9f
            }
        }

        // Update light color based on animation state
        updateLightColor()
    }

    private fun updateLightColor() {
        when (animationState) {
            CharacterAnimationState.HAPPY, CharacterAnimationState.CELEBRATING, CharacterAnimationState.CORRECT_ANSWER -> {
                lightColor = floatArrayOf(1f, 1f, 0.5f, 1f) // Warm yellow
            }
            CharacterAnimationState.SAD, CharacterAnimationState.WRONG_ANSWER -> {
                lightColor = floatArrayOf(0.8f, 0.3f, 0.3f, 1f) // Dim red
            }
            CharacterAnimationState.THINKING -> {
                lightColor = floatArrayOf(0.5f, 0.5f, 1f, 1f) // Blue
            }
            CharacterAnimationState.ENCOURAGING -> {
                lightColor = floatArrayOf(1f, 0.8f, 0.3f, 1f) // Orange
            }
            else -> {
                lightColor = floatArrayOf(1f, 1f, 1f, 1f) // White
            }
        }
    }

    private fun renderCharacterPart(vertices: FloatArray, colors: FloatArray,
                                    rotation: Float, offset: FloatArray) {
        // Use shader program
        GLES20.glUseProgram(shaderProgram)

        // Set up vertex buffer
        val vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        val colorBuffer = java.nio.ByteBuffer.allocateDirect(colors.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        // Get attribute locations
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(shaderProgram, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        val lightPositionHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightPosition")
        val lightColorHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightColor")

        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)

        // Set vertex attributes
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

        // Apply transformations
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0,
            characterPosition[0] + offset[0],
            characterPosition[1] + bounceOffset + offset[1],
            characterPosition[2] + offset[2])
        Matrix.rotateM(modelMatrix, 0, characterRotation + rotation, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, characterScale * scalePulse, characterScale * scalePulse, characterScale * scalePulse)

        // Calculate final MVP matrix
        val tempMatrix = FloatArray(16)
        Matrix.multiplyMM(tempMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, tempMatrix, 0)
        GLES20.glUniform4fv(lightPositionHandle, 1, lightPosition, 0)
        GLES20.glUniform4fv(lightColorHandle, 1, lightColor, 0)

        // Draw the character part
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    fun setAnimationState(state: CharacterAnimationState) {
        animationState = state
        animationTime = 0f // Reset animation time for new state
    }

    fun getAnimationState(): CharacterAnimationState = animationState

    private fun renderCharacterPartSimple(vertices: FloatArray, colors: FloatArray,
                                          rotation: Float, offset: FloatArray) {
        // Use shader program
        GLES20.glUseProgram(shaderProgram)

        // Set up vertex buffer
        val vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        val colorBuffer = java.nio.ByteBuffer.allocateDirect(colors.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        colorBuffer.put(colors)
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
            characterPosition[0] + offset[0],
            characterPosition[1] + bounceOffset + offset[1],
            characterPosition[2] + offset[2])
        Matrix.rotateM(modelMatrix, 0, characterRotation + rotation, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, characterScale * scalePulse, characterScale * scalePulse, characterScale * scalePulse)

        // Calculate final MVP matrix
        val tempMatrix = FloatArray(16)
        Matrix.multiplyMM(tempMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        // Set MVP matrix
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, tempMatrix, 0)

        // Draw the character part
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }

    private fun renderCharacterPartWithLighting(vertices: FloatArray, colors: FloatArray,
                                                rotation: Float, offset: FloatArray) {
        // Use shader program
        GLES20.glUseProgram(shaderProgram)

        // Set up vertex buffer
        val vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        val colorBuffer = java.nio.ByteBuffer.allocateDirect(colors.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
        colorBuffer.put(colors)
        colorBuffer.position(0)

        // Get attribute locations
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        val colorHandle = GLES20.glGetAttribLocation(shaderProgram, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix")
        val lightPositionHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightPosition")
        val lightColorHandle = GLES20.glGetUniformLocation(shaderProgram, "uLightColor")

        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(colorHandle)

        // Set vertex attributes
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

        // Apply transformations
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0,
            characterPosition[0] + offset[0],
            characterPosition[1] + bounceOffset + offset[1],
            characterPosition[2] + offset[2])
        Matrix.rotateM(modelMatrix, 0, characterRotation + rotation, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, characterScale * scalePulse, characterScale * scalePulse, characterScale * scalePulse)

        // Calculate final MVP matrix
        val tempMatrix = FloatArray(16)
        Matrix.multiplyMM(tempMatrix, 0, mvpMatrix, 0, modelMatrix, 0)

        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, tempMatrix, 0)
        GLES20.glUniform4fv(lightPositionHandle, 1, lightPosition, 0)
        GLES20.glUniform4fv(lightColorHandle, 1, lightColor, 0)

        // Draw the character part
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.size / 3)

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
