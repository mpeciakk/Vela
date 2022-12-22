package render.gui

import ain.Window
import ain.mesh.Mesh
import ain.rp.RenderPipeline
import ain.rp.Renderable
import aries.AssetManager
import asset.Texture
import imgui.ImDrawData
import imgui.ImGui
import imgui.type.ImInt
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14.*
import org.lwjgl.opengl.GL30.*
import render.DefaultMeshFactory
import shader.DefaultShader


class GuiRenderPipeline(assetManager: AssetManager, private val window: Window) : RenderPipeline(DefaultShader(assetManager[String::class.java, "gui"]), GuiMeshFactory()) {
    private val scale = Vector2f()
    private val texture: Texture

    init {
        ImGui.createContext()

        val imGuiIO = ImGui.getIO()
        imGuiIO.iniFilename = null
        imGuiIO.setDisplaySize(window.width.toFloat(), window.height.toFloat())

        val fontAtlas = ImGui.getIO().fonts
        val width = ImInt()
        val height = ImInt()
        val buf = fontAtlas.getTexDataAsRGBA32(width, height)
        texture = Texture(width.get(), height.get(), buf)
    }

    override fun render(obj: Renderable, mesh: Mesh) {
        (obj as GuiInstance).render()

        shader.start()

        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)

        mesh.bind()

        glBindBuffer(GL_ARRAY_BUFFER, mesh.getVbo(0, 5).id)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.getVbo(-1, 1).id)

        val io = ImGui.getIO()
        scale.x = 2.0f / io.displaySizeX
        scale.y = -2.0f / io.displaySizeY
        shader.loadVector("scale", scale)

        val drawData = ImGui.getDrawData()
        val numLists = drawData.cmdListsCount
        for (i in 0 until numLists) {
            glBufferData(GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(i), GL_STREAM_DRAW)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(i), GL_STREAM_DRAW)

            val numCmds = drawData.getCmdListCmdBufferSize(i)

            for (j in 0 until numCmds) {
                val elemCount = drawData.getCmdListCmdBufferElemCount(i, j)
                val idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j)
                val indices = idxBufferOffset * ImDrawData.SIZEOF_IM_DRAW_IDX
                glBindTexture(GL_TEXTURE_2D, texture.id)
                glDrawElements(GL_TRIANGLES, elemCount, GL_UNSIGNED_SHORT, indices.toLong())
            }
        }

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glDisable(GL_BLEND)
    }
}