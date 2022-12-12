package render

import Camera
import shader.DefaultShader
import ain.mesh.Mesh
import ain.rp.RenderPipeline
import ain.rp.Renderable
import aries.AssetManager
import light.DirectionalLight
import light.PointLight
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20

class DefaultRenderPipeline(assetManager: AssetManager) : RenderPipeline(DefaultShader(assetManager[String::class.java, "default"]), DefaultMeshFactory()) {

    override fun render(obj: Renderable, mesh: Mesh) {
        mesh.bind()
        mesh.vbos.forEach {
            GL20.glEnableVertexAttribArray(it.attributeNumber)
        }

        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.elementsCount, GL11.GL_UNSIGNED_INT, 0);

        mesh.vbos.forEach {
            GL20.glDisableVertexAttribArray(it.attributeNumber)
        }
        mesh.unbind()

//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }
}