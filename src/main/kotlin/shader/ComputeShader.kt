package shader

import ain.Destroyable
import ain.shader.GLSLPreprocessor
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER
import kotlin.system.exitProcess


class ComputeShader(shader: String) : Destroyable {
    private var program: Int
    private var computeShader: Int
    private val locationCache: MutableMap<String, Int> = HashMap()
    private var matrixBuffer = BufferUtils.createFloatBuffer(16)

    init {
        val glslPreprocessor = GLSLPreprocessor()

        computeShader = loadShader(shader.trim(), GL_COMPUTE_SHADER)

        program = glCreateProgram()

        glAttachShader(program, computeShader)
        glLinkProgram(program)
        glValidateProgram(program)
    }

    private fun loadShader(source: String, type: Int): Int {
        val shader = glCreateShader(type)

        glShaderSource(shader, source)
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            println(glGetShaderInfoLog(shader, 500))
            println("Could not compile shader!")
            exitProcess(-1)
        }

        return shader
    }

    fun start() {
        glUseProgram(program)
    }

    fun stop() {
        glUseProgram(0)
    }

    private fun getUniformLocation(name: String): Int {
        return if (locationCache.containsKey(name)) {
            locationCache[name]!!
        } else {
            val location = glGetUniformLocation(program, name)
            locationCache[name] = location
            location
        }
    }

    fun loadTransformationMatrix(matrix: Matrix4f) {
        loadMatrix("transformationMatrix", matrix)
    }

    fun loadProjectionMatrix(matrix: Matrix4f) {
        loadMatrix("projectionMatrix", matrix)
    }

    fun loadViewMatrix(matrix: Matrix4f) {
        loadMatrix("viewMatrix", matrix)
    }

    fun loadFloat(name: String, value: Float) {
        glUniform1f(getUniformLocation(name), value)
    }

    fun loadInt(name: String, value: Int) {
        glUniform1i(getUniformLocation(name), value)
    }

    fun loadVector(name: String, vector: Vector3f) {
        glUniform3f(getUniformLocation(name), vector.x, vector.y, vector.z)
    }

    fun loadVector(name: String, vector: Vector3i) {
        glUniform3i(getUniformLocation(name), vector.x, vector.y, vector.z)
    }

    fun loadBoolean(name: String, value: Boolean) {
        glUniform1f(getUniformLocation(name), (if (value) 1 else 0).toFloat())
    }

    fun loadMatrix(name: String, matrix: Matrix4f) {
        matrixBuffer = matrix.get(matrixBuffer)
        glUniformMatrix4fv(getUniformLocation(name), false, matrixBuffer)
        matrixBuffer.clear()
    }

    protected fun bindAttribute(attribute: Int, name: String) {
        glBindAttribLocation(program, attribute, name)
    }

    override fun destroy() {
        stop()
        glDetachShader(program, computeShader)
        glDeleteShader(computeShader)
        glDeleteProgram(program)
    }
}