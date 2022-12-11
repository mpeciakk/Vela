import input.InputManager
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Camera(width: Int, height: Int, private val input: InputManager) {
    val projectionMatrix: Matrix4f
    val viewMatrix = Matrix4f()
    val position = Vector3f(0f, 0f, -1f)
    private val speed = 5f / 32f
    private val sensitivity = 0.05f
    private var pitch = 0f
    private var yaw = 0f
    private val roll = 0f
    private var currentSpeed = 0f

    init {
        val aspectRatio = width.toFloat() / height
        val yScale = 1f / (tan(Math.toRadians(FOV / 2.0)).toFloat()) * aspectRatio
        val xScale = yScale / aspectRatio
        val frustumLength = FAR_PLANE - NEAR_PLANE
        projectionMatrix = Matrix4f()
        projectionMatrix.m00(xScale)
        projectionMatrix.m11(yScale)
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustumLength))
        projectionMatrix.m23(-1f)
        projectionMatrix.m32(-(2 * NEAR_PLANE * FAR_PLANE / frustumLength))
        projectionMatrix.m33(0f)
    }

    fun update() {
        currentSpeed = if (input.isKeyPressed(GLFW.GLFW_KEY_W)) {
            -speed
        } else if (input.isKeyPressed(GLFW.GLFW_KEY_S)) {
            speed
        } else {
            0f
        }

        pitch += -input.deltaMousePosition.y * sensitivity
        yaw += input.deltaMousePosition.x * sensitivity

        val dx = -(currentSpeed * sin(Math.toRadians(yaw.toDouble()))).toFloat()
        val dy = (currentSpeed * sin(Math.toRadians(pitch.toDouble()))).toFloat()
        val dz = (currentSpeed * cos(Math.toRadians(yaw.toDouble()))).toFloat()

        position.x += dx
        position.y += dy
        position.z += dz

        viewMatrix.identity()
        viewMatrix.rotate(Math.toRadians(pitch.toDouble()).toFloat(), Vector3f(1f, 0f, 0f))
        viewMatrix.rotate(Math.toRadians(yaw.toDouble()).toFloat(), Vector3f(0f, 1f, 0f))
        val cameraPos = position
        val negativeCameraPos = Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z)
        viewMatrix.translate(negativeCameraPos)
    }

    fun makeCurrent() {
        current = this
    }

    companion object {
        const val FOV = 70f
        const val NEAR_PLANE = 0.1f
        const val FAR_PLANE = 1000f

        lateinit var current: Camera
    }
}