package input

import ain.Destroyable
import ain.Window
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback

class InputManager : Destroyable {
    val mousePosition = Vector2f()
    val prevMousePosition = Vector2f()
    val deltaMousePosition = Vector2f()

    private val keys = BooleanArray(GLFW_KEY_LAST)
    private val buttons = BooleanArray(GLFW_MOUSE_BUTTON_LAST)

    private lateinit var keyCallback: GLFWKeyCallback
    private lateinit var mouseButtonCallback: GLFWMouseButtonCallback
    private lateinit var mousePositionCallback: GLFWCursorPosCallback

    fun start(window: Window) {
        keyCallback = object : GLFWKeyCallback() {
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
                keys[key] = action != GLFW.GLFW_RELEASE
            }
        }

        mousePositionCallback = object : GLFWCursorPosCallback() {
            override fun invoke(window: Long, x: Double, y: Double) {
                mousePosition.x = x.toFloat()
                mousePosition.y = y.toFloat()
            }
        }

        mouseButtonCallback = object : GLFWMouseButtonCallback() {
            override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
                buttons[button] = action != GLFW.GLFW_RELEASE
            }
        }

        glfwSetKeyCallback(window.id, keyCallback)
        glfwSetMouseButtonCallback(window.id, mouseButtonCallback)
        glfwSetCursorPosCallback(window.id, mousePositionCallback)
    }

    fun update() {
        deltaMousePosition.x = mousePosition.x - prevMousePosition.x
        deltaMousePosition.y = prevMousePosition.y - mousePosition.y

        prevMousePosition.x = mousePosition.x
        prevMousePosition.y = mousePosition.y
    }

    fun isKeyPressed(key: Int): Boolean {
        return keys[key]
    }

    fun isButtonPressed(button: Int): Boolean {
        return buttons[button]
    }

    override fun destroy() {
        keyCallback.free()
        mouseButtonCallback.free()
        mousePositionCallback.free()
    }
}