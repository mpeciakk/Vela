package render

import Camera
import ain.Window
import ain.mesh.Mesh
import ain.rp.RenderPipeline
import ain.rp.Renderable
import aries.AssetManager
import buffer.GBuffer
import org.lwjgl.opengl.GL30.*
import scene.SceneManager
import shader.DefaultShader
import java.nio.ByteBuffer

class DeferredRenderingPipeline(assetManager: AssetManager, private val window: Window) :
    RenderPipeline(DefaultShader(assetManager[String::class.java, "empty"]), DefaultMeshFactory()) {

    val gBuffer = GBuffer(window)

    private val gBufferShader = DefaultShader(assetManager[String::class.java, "gbuffer"])
    private val finalShader = DefaultShader(assetManager[String::class.java, "final"])
    private val depthShader = DefaultShader(assetManager[String::class.java, "depth"])

    private val quad = QuadMesh()

    private val fbo = glGenFramebuffers()
    private val depthMap = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D, depthMap)
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_DEPTH_COMPONENT,
            window.width,
            window.height,
            0,
            GL_DEPTH_COMPONENT,
            GL_FLOAT,
            null as ByteBuffer?
        )
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap, 0)
        glDrawBuffer(GL_NONE)
        glReadBuffer(GL_NONE)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun render(mesh: Mesh) {
        mesh.bind()
        mesh.vbos.forEach {
            glEnableVertexAttribArray(it.attributeNumber)
        }

        glDrawElements(GL_TRIANGLES, mesh.elementsCount, GL_UNSIGNED_INT, 0)

        mesh.vbos.forEach {
            glDisableVertexAttribArray(it.attributeNumber)
        }
        mesh.unbind()
    }

    override fun render(obj: Renderable, mesh: Mesh) {
        // Depth
        depthShader.start()
        depthShader.loadMatrix("viewMatrix", Camera.current.viewMatrix)
        depthShader.loadMatrix("projectionMatrix", Camera.current.projectionMatrix)
        depthShader.loadMatrix("transformationMatrix", obj.transformationMatrix)

        glViewport(0, 0, 1024, 1024)
        glBindFramebuffer(GL_FRAMEBUFFER, fbo)
        glClear(GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)
        render(mesh)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        glBindFramebuffer(GL_FRAMEBUFFER, gBuffer.id)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glViewport(0, 0, window.width, window.height)
        glDisable(GL_BLEND)

        // Scene
        gBufferShader.start()
        gBufferShader.loadMatrix("viewMatrix", Camera.current.viewMatrix)
        gBufferShader.loadMatrix("projectionMatrix", Camera.current.projectionMatrix)
        gBufferShader.loadMatrix("transformationMatrix", obj.transformationMatrix)

        glDepthMask(true)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_BLEND)

        render(mesh)

        glEnable(GL_BLEND)
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        gBufferShader.stop()
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Final
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glViewport(0, 0, window.width, window.height)

        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFunc(GL_ONE, GL_ONE)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.id)
        glClear(GL_COLOR_BUFFER_BIT)

        val textureIds = gBuffer.textures
        val numTextures = textureIds.size
        for (i in 0 until numTextures) {
            glActiveTexture(GL_TEXTURE0 + i)
            glBindTexture(GL_TEXTURE_2D, textureIds[i])
        }

        finalShader.start()
        finalShader.loadInt("positionTexture", 0)
        finalShader.loadInt("diffuseTexture", 1)
        finalShader.loadInt("normalTexture", 2)
        finalShader.loadInt("depthTexture", 3)

//        light.position.add(0f, 0f, cos(j) / 10f)

        val light = SceneManager.scene.pointLights[0]

        finalShader.loadVector("light.position", light.position)
        finalShader.loadVector("light.color", light.color)
        finalShader.loadFloat("light.intensity", light.intensity)
        finalShader.loadFloat("light.constant", light.constant)
        finalShader.loadFloat("light.linear", light.linear)
        finalShader.loadFloat("light.exponent", light.exponent)

        val directionalLight = SceneManager.scene.directionalLight
        if (directionalLight != null) {
            finalShader.loadVector("directionalLight.color", directionalLight.color)
            finalShader.loadVector("directionalLight.direction", directionalLight.direction)
            finalShader.loadFloat("directionalLight.ambientIntensity", directionalLight.ambientIntensity)
            finalShader.loadFloat("directionalLight.diffuseIntensity", directionalLight.diffuseIntensity)
        }

        glBindVertexArray(quad.vaoId)
        glDrawElements(GL_TRIANGLES, quad.numVertices, GL_UNSIGNED_INT, 0)
        glBindVertexArray(0)
        finalShader.stop()
    }
}