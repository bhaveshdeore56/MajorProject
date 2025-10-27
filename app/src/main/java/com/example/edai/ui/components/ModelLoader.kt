package com.example.edai.ui.components

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

/**
 * OBJ Model Loader for importing 3D models into the app
 */
class ModelLoader {
    companion object {
        private const val TAG = "ModelLoader"
        
        /**
         * Load an OBJ model from assets
         * Returns a Model3D object with vertices and faces
         */
        fun loadModel(context: Context, filename: String): Model3D? {
            return try {
                val inputStream = context.assets.open(filename)
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                val vertices = mutableListOf<FloatArray>()
                val texCoords = mutableListOf<FloatArray>()
                val normals = mutableListOf<FloatArray>()
                val faces = mutableListOf<Face>()
                
                reader.useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.trim().split("\\s+".toRegex())
                        if (parts.isEmpty()) return@forEach
                        
                        when (parts[0]) {
                            "v" -> {
                                // Vertex: v x y z
                                if (parts.size >= 4) {
                                    vertices.add(floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    ))
                                }
                            }
                            "vt" -> {
                                // Texture coordinates: vt u v
                                if (parts.size >= 3) {
                                    texCoords.add(floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat()
                                    ))
                                }
                            }
                            "vn" -> {
                                // Normal: vn x y z
                                if (parts.size >= 4) {
                                    normals.add(floatArrayOf(
                                        parts[1].toFloat(),
                                        parts[2].toFloat(),
                                        parts[3].toFloat()
                                    ))
                                }
                            }
                            "f" -> {
                                // Face: f v/vt/vn v/vt/vn v/vt/vn
                                if (parts.size >= 4) {
                                    val face = Face()
                                    for (i in 1 until parts.size) {
                                        val indices = parts[i].split("/")
                                        when (indices.size) {
                                            3 -> {
                                                face.vertexIndices.add(indices[0].toInt() - 1)
                                                face.texIndices.add(indices[1].toInt() - 1)
                                                face.normalIndices.add(indices[2].toInt() - 1)
                                            }
                                            2 -> {
                                                face.vertexIndices.add(indices[0].toInt() - 1)
                                                face.texIndices.add(indices[1].toInt() - 1)
                                            }
                                            1 -> {
                                                face.vertexIndices.add(indices[0].toInt() - 1)
                                            }
                                        }
                                    }
                                    faces.add(face)
                                }
                            }
                        }
                    }
                }
                
                Model3D(vertices, texCoords, normals, faces)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading model: $filename", e)
                null
            }
        }
        
        /**
         * Convert Model3D to renderable mesh with vertices and normals
         */
        fun createMeshFromModel(model: Model3D): Mesh {
            val vertices = mutableListOf<Float>()
            val normals = mutableListOf<Float>()
            val texCoords = mutableListOf<Float>()
            
            // Process each face
            for (face in model.faces) {
                val vertexCount = face.vertexIndices.size
                
                // Triangulate the face (simple fan triangulation)
                if (vertexCount >= 3) {
                    for (i in 1 until vertexCount - 1) {
                        // Triangle: 0, i, i+1
                        addVertexToMesh(
                            vertices, normals, texCoords,
                            model,
                            face.vertexIndices[0], face.texIndices[0], face.normalIndices[0]
                        )
                        addVertexToMesh(
                            vertices, normals, texCoords,
                            model,
                            face.vertexIndices[i], face.texIndices[i], face.normalIndices[i]
                        )
                        addVertexToMesh(
                            vertices, normals, texCoords,
                            model,
                            face.vertexIndices[i + 1], face.texIndices[i + 1], face.normalIndices[i + 1]
                        )
                    }
                }
            }
            
            val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            vertexBuffer.put(vertices.toFloatArray())
            vertexBuffer.position(0)
            
            val normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            normalBuffer.put(normals.toFloatArray())
            normalBuffer.position(0)
            
            val texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            texCoordBuffer.put(texCoords.toFloatArray())
            texCoordBuffer.position(0)
            
            return Mesh(
                vertexBuffer,
                normalBuffer,
                texCoordBuffer,
                vertices.size / 3
            )
        }
        
        private fun addVertexToMesh(
            vertices: MutableList<Float>,
            normals: MutableList<Float>,
            texCoords: MutableList<Float>,
            model: Model3D,
            vertexIndex: Int,
            texIndex: Int,
            normalIndex: Int
        ) {
            // Add vertex position
            if (vertexIndex >= 0 && vertexIndex < model.vertices.size) {
                val v = model.vertices[vertexIndex]
                vertices.add(v[0])
                vertices.add(v[1])
                vertices.add(v[2])
            } else {
                vertices.add(0f)
                vertices.add(0f)
                vertices.add(0f)
            }
            
            // Add normal
            if (normalIndex >= 0 && normalIndex < model.normals.size) {
                val n = model.normals[normalIndex]
                normals.add(n[0])
                normals.add(n[1])
                normals.add(n[2])
            } else {
                // Calculate face normal if not provided
                normals.add(0f)
                normals.add(1f)
                normals.add(0f)
            }
            
            // Add texture coordinates
            if (texIndex >= 0 && texIndex < model.texCoords.size) {
                val t = model.texCoords[texIndex]
                texCoords.add(t[0])
                texCoords.add(t[1])
            } else {
                texCoords.add(0f)
                texCoords.add(0f)
            }
        }
        
        /**
         * Create colors for the mesh
         */
        fun createColorBuffer(vertexCount: Int, color: FloatArray): FloatBuffer {
            val colors = FloatArray(vertexCount * 4)
            for (i in 0 until vertexCount) {
                colors[i * 4] = color[0]
                colors[i * 4 + 1] = color[1]
                colors[i * 4 + 2] = color[2]
                colors[i * 4 + 3] = color[3]
            }
            val buffer = ByteBuffer.allocateDirect(colors.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            buffer.put(colors)
            buffer.position(0)
            return buffer
        }
    }
}

/**
 * Represents a 3D model loaded from file
 */
class Model3D(
    val vertices: List<FloatArray>,
    val texCoords: List<FloatArray>,
    val normals: List<FloatArray>,
    val faces: List<Face>
) {
    fun getBoundingBox(): BoundingBox {
        if (vertices.isEmpty()) return BoundingBox(0f, 0f, 0f, 0f, 0f, 0f)
        
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = Float.MIN_VALUE
        
        vertices.forEach { v ->
            minX = min(minX, v[0])
            maxX = max(maxX, v[0])
            minY = min(minY, v[1])
            maxY = max(maxY, v[1])
            minZ = min(minZ, v[2])
            maxZ = max(maxZ, v[2])
        }
        
        return BoundingBox(minX, minY, minZ, maxX, maxY, maxZ)
    }
}

/**
 * Represents a face in the model
 */
class Face {
    val vertexIndices = mutableListOf<Int>()
    val texIndices = mutableListOf<Int>()
    val normalIndices = mutableListOf<Int>()
}

/**
 * Represents a renderable mesh
 */
data class Mesh(
    val vertices: FloatBuffer,
    val normals: FloatBuffer,
    val texCoords: FloatBuffer,
    val vertexCount: Int
) {
    fun scaleToFit(size: Float): Float {
        // Simple scaling - in production, calculate actual bounds
        return size
    }
}

/**
 * Bounding box for the model
 */
data class BoundingBox(
    val minX: Float,
    val minY: Float,
    val minZ: Float,
    val maxX: Float,
    val maxY: Float,
    val maxZ: Float
) {
    val width get() = maxX - minX
    val height get() = maxY - minY
    val depth get() = maxZ - minZ
    
    val centerX get() = (minX + maxX) / 2f
    val centerY get() = (minY + maxY) / 2f
    val centerZ get() = (minZ + maxZ) / 2f
}

