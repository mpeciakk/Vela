package shader

import ain.Destroyable
import ain.shader.GLSLPreprocessor
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20
import kotlin.system.exitProcess

class Shader(shader: String) : Destroyable {
    private var program: Int
    private var vertex: Int
    private val locationCache: MutableMap<String, Int> = HashMap()
    private var matrixBuffer = BufferUtils.createFloatBuffer(16)

    init {
        val glslPreprocessor = GLSLPreprocessor()

        vertex = loadShader(
            shader, GL20.GL_VERTEX_SHADER
        )

        program = GL20.glCreateProgram()

        GL20.glAttachShader(program, vertex);
        GL20.glLinkProgram(program);
        GL20.glValidateProgram(program);
    }

    private fun loadShader(source: String, type: Int): Int {
        val shader = GL20.glCreateShader(type)

        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            println(GL20.glGetShaderInfoLog(shader, 500));
            println("Could not compile shader!");
            exitProcess(-1);
        }

        return shader
    }

    fun start() {
        GL20.glUseProgram(program)
    }

    fun stop() {
        GL20.glUseProgram(0)
    }

    private fun getUniformLocation(name: String): Int {
        return if (locationCache.containsKey(name)) {
            locationCache[name]!!
        } else {
            val location = GL20.glGetUniformLocation(program, name)
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
        GL20.glUniform1f(getUniformLocation(name), value)
    }

    fun loadInt(name: String, value: Int) {
        GL20.glUniform1i(getUniformLocation(name), value)
    }

    fun loadVector(name: String, vector: Vector3f) {
        GL20.glUniform3f(getUniformLocation(name), vector.x, vector.y, vector.z)
    }

    fun loadVector(name: String, vector: Vector3i) {
        GL20.glUniform3i(getUniformLocation(name), vector.x, vector.y, vector.z)
    }

    fun loadBoolean(name: String, value: Boolean) {
        GL20.glUniform1f(getUniformLocation(name), (if (value) 1 else 0).toFloat())
    }

    fun loadMatrix(name: String, matrix: Matrix4f) {
        matrixBuffer = matrix.get(matrixBuffer)
        GL20.glUniformMatrix4fv(getUniformLocation(name), false, matrixBuffer)
        matrixBuffer.clear()
    }

    protected fun bindAttribute(attribute: Int, name: String) {
        GL20.glBindAttribLocation(program, attribute, name)
    }

    override fun destroy() {
        stop()
        GL20.glDetachShader(program, vertex)
        GL20.glDeleteShader(vertex)
        GL20.glDeleteProgram(program)
    }
}