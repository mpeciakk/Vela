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

class DefaultRenderPipeline(assetManager: AssetManager, private val light: DirectionalLight) : RenderPipeline(DefaultShader(assetManager[String::class.java, "default"]), DefaultMeshFactory()) {

    override fun render(obj: Renderable, mesh: Mesh) {
        shader.start()

//        val lightPos = Vector4f(light.position, 1f).mul(Camera.current.viewMatrix)
//        light.position.x = lightPos.x
//        light.position.y = lightPos.y
//        light.position.z = lightPos.z

//        (shader as DefaultShader).loadPointLight("pointLight", light)

        val point = PointLight(Vector3f(1f, 1f, 1f), Vector3f(0f, 10f, 0f), 100.0f, 1f, 0f, 1f)

        shader.loadVector("pointLight.color", point.color)
        shader.loadVector("pointLight.position", point.position)
        shader.loadFloat("pointLight.intensity", point.intensity)
        shader.loadFloat("pointLight.constant", point.constant)
        shader.loadFloat("pointLight.linear", point.linear)
        shader.loadFloat("pointLight.exponent", point.exponent)

//        shader.loadVector("directionalLight.color", light.color)
//        shader.loadVector("directionalLight.direction", light.direction)
//        shader.loadFloat("directionalLight.color", light.intensity)

        shader.loadVector("material.ambient", Vector3f(0f, 0f, 0f))
        shader.loadVector("material.diffuse", Vector3f(1f, 1f, 1f))
        shader.loadVector("material.specular", Vector3f(1f, 1f, 1f))
        shader.loadBoolean("material.hasTexture", false)
        shader.loadFloat("material.reflectance", 0f)

        shader.loadFloat("specularPower", 1f)
        shader.loadVector("ambientLight", Vector3f(0f, 0f, 0f))

        shader.loadProjectionMatrix(Camera.current.projectionMatrix)
        shader.loadTransformationMatrix(obj.transformationMatrix)
        shader.loadViewMatrix(Camera.current.viewMatrix)

        mesh.bind()
        mesh.vbos.forEach {
            GL20.glEnableVertexAttribArray(it.attributeNumber)
        }

        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.elementsCount, GL11.GL_UNSIGNED_INT, 0);

        mesh.vbos.forEach {
            GL20.glDisableVertexAttribArray(it.attributeNumber)
        }
        mesh.unbind()

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        shader.stop()
    }
}