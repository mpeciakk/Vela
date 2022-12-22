package render

import ain.Destroyable
import imgui.ImDrawData
import org.lwjgl.opengl.GL30.*

class GuiMesh : Destroyable {
    val vao: Int
    private val indices: Int
    private val vertices: Int

    init {
        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        // Single VBO
        vertices = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vertices)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, ImDrawData.SIZEOF_IM_DRAW_VERT, 0)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, ImDrawData.SIZEOF_IM_DRAW_VERT, 8)
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, ImDrawData.SIZEOF_IM_DRAW_VERT, 16)
        indices = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    override fun destroy() {
        glDeleteBuffers(indices)
        glDeleteBuffers(vertices)
        glDeleteVertexArrays(vao)
    }
}