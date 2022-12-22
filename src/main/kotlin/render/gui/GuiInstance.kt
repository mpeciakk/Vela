package render.gui

import ain.Window
import ain.rp.Renderable
import imgui.ImGui
import input.InputManager
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW

abstract class GuiInstance : Renderable() {
    abstract fun render()

    fun update(window: Window, input: InputManager) {
        render()

        val imGuiIO = ImGui.getIO()
        val mousePos = input.mousePosition
        imGuiIO.setMousePos(mousePos.x, mousePos.y)
        imGuiIO.setMouseDown(0, input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT))
        imGuiIO.setMouseDown(1, input.isButtonPressed(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
//        imGuiIO.set
    }

    override fun rebuild() {
        // It must be invoked here so the mesh will be processed by mesh factory
        getBuilder()
    }
}