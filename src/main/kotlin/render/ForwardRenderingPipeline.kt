package render

import shader.DefaultShader
import ain.mesh.Mesh
import ain.rp.RenderPipeline
import ain.rp.Renderable
import aries.AssetManager
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glDisableVertexAttribArray
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray

class ForwardRenderingPipeline(assetManager: AssetManager) :
    RenderPipeline(DefaultShader(assetManager[String::class.java, "default"]), DefaultMeshFactory()) {

    override fun render(obj: Renderable, mesh: Mesh) {
        shader.start()

        shader.loadProjectionMatrix(Camera.current.projectionMatrix)
        shader.loadTransformationMatrix(obj.transformationMatrix)
        shader.loadViewMatrix(Camera.current.viewMatrix)

        mesh.bind()
        mesh.vbos.values.forEach {
            glEnableVertexAttribArray(it.attributeNumber)
        }

        glDrawElements(GL_TRIANGLES, mesh.elementsCount, GL_UNSIGNED_INT, 0);

        mesh.vbos.values.forEach {
            glDisableVertexAttribArray(it.attributeNumber)
        }
        mesh.unbind()

        shader.stop()
    }
}