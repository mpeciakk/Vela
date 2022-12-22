package render.gui

import ain.mesh.IndicesVBO
import ain.mesh.Mesh
import ain.mesh.MeshBuilder
import ain.mesh.MeshFactory
import imgui.ImDrawData
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*

class GuiMeshFactory : MeshFactory() {
    override fun processMesh(meshBuilder: MeshBuilder, mesh: Mesh): Mesh {
        mesh.bind()

        val vertices = mesh.getVbo(0, 8)
        val indices = mesh.addVbo(IndicesVBO())

        glBindBuffer(GL_ARRAY_BUFFER, vertices.id)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, ImDrawData.SIZEOF_IM_DRAW_VERT, 0)
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, ImDrawData.SIZEOF_IM_DRAW_VERT, 8)
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, ImDrawData.SIZEOF_IM_DRAW_VERT, 16)
        glBindBuffer(GL_ARRAY_BUFFER, 0)

        mesh.unbind()

        println(mesh)

        return mesh
    }
}