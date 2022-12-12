import ain.Window
import ain.rp.MeshRenderer
import ain.rp.Renderable
import aries.AssetManager
import asset.DefaultAssetLoader
import input.InputManager
import model.ObjModel
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL43.*
import org.lwjgl.system.MemoryUtil
import render.DefaultRenderPipeline
import shader.ComputeShader
import shader.DepthShader
import shader.HDRShader
import shader.LightAccumulationShader
import java.nio.ByteBuffer
import java.util.*


class GameObject(private val model: ObjModel) : Renderable() {
    override fun rebuild() {
        getBuilder().setIndices(model.indices.asList())
        getBuilder().setVertices(model.vertices)
    }
}

fun main(args: Array<String>) {
    val window = Window(800, 600, "Ain engine")
    window.create()

    val assetManager = AssetManager()
    val assetLoader = DefaultAssetLoader(assetManager)

    assetLoader.load()
    assetLoader.loadAssets()

    val input = InputManager()
    input.start(window)

    val camera = Camera(window.width, window.height, input)
    camera.makeCurrent()

    val renderer = MeshRenderer<GameObject>(DefaultRenderPipeline(assetManager))

    val obj = GameObject(assetManager[ObjModel::class.java, "model"])
    obj.markDirty()

    val NUM_LIGHTS = 1024

    val depthShader = DepthShader(assetManager[String::class.java, "depth"])
    val lightCullingShader = ComputeShader(assetManager[String::class.java, "light_culling"])
    val lightAccumulationShader = LightAccumulationShader(assetManager[String::class.java, "light_accumulation"])
    val hdrShader = HDRShader(assetManager[String::class.java, "hdr"])
    val depthDebugShader = HDRShader(assetManager[String::class.java, "hdr"])

    val depthMapFBO = glGenFramebuffers()
    val depthMap = glGenTextures()
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
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)

    val borderColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor)

    glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap, 0)
    glDrawBuffer(GL_NONE)
    glReadBuffer(GL_NONE)
    glBindFramebuffer(GL_FRAMEBUFFER, 0)

    val hdrFBO = glGenFramebuffers()
    val colorBuffer = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, colorBuffer)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, window.width, window.height, 0, GL_RGB, GL_FLOAT, null as ByteBuffer?)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

    val rboDepth = glGenRenderbuffers()
    glBindRenderbuffer(GL_RENDERBUFFER, rboDepth)
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, window.width, window.height)

    glBindFramebuffer(GL_FRAMEBUFFER, hdrFBO)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBuffer, 0)
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth)
    glBindFramebuffer(GL_FRAMEBUFFER, 0)

    val workGroupsX = (window.width + (window.width % 16)) / 16
    val workGroupsY = (window.height + (window.height % 16)) / 16
    val numberOfTiles = workGroupsX * workGroupsY

    val lightBuffer = glGenBuffers()
    val visibleLightIndicesBuffer = glGenBuffers()

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, lightBuffer)
    glBufferData(GL_SHADER_STORAGE_BUFFER, NUM_LIGHTS.toLong() * 12 * 4, GL_DYNAMIC_DRAW)

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, visibleLightIndicesBuffer)
    glBufferData(GL_SHADER_STORAGE_BUFFER, numberOfTiles.toLong() * 4 * 1024, GL_STATIC_DRAW)

    // setup lights
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, lightBuffer)
//    val bb = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_READ_WRITE)
//    val fbb = bb!!.asFloatBuffer()

    val fb = FloatArray(NUM_LIGHTS * 12)

    for (i in 0 until NUM_LIGHTS * 12 step 12) {
        fb[i + 0] = Random().nextFloat(20f) - 10f
        fb[i + 1] = Random().nextFloat(20f) - 10f
        fb[i + 2] = Random().nextFloat(20f) - 10f
        fb[i + 3] = 1f
        fb[i + 4] = Random().nextFloat() + 1f
        fb[i + 5] = Random().nextFloat() + 1f
        fb[i + 6] = Random().nextFloat() + 1f
        fb[i + 7] = 1f
        fb[i + 8] = 0f
        fb[i + 9] = 0f
        fb[i + 10] = 0f
        fb[i + 11] = 30f
    }

//    fbb.put(fb)
//    fbb.flip()

    GL15.glBufferData(GL_SHADER_STORAGE_BUFFER, BufferUtils.createFloatBuffer(fb.size).put(fb).flip(), GL_STATIC_DRAW)

//    glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)
//    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    val model = Matrix4f().scale(0.1f, 0.1f, 0.1f)

    depthShader.start()
    depthShader.loadMatrix("model", model)

    lightCullingShader.start()
    lightCullingShader.loadInt("lightCount", NUM_LIGHTS)

    depthDebugShader.start()
    depthDebugShader.loadMatrix("model", model)
    depthDebugShader.loadFloat("near", 0.1f)
    depthDebugShader.loadFloat("far", 300f)

    lightAccumulationShader.start()
    lightAccumulationShader.loadMatrix("model", model)
    lightAccumulationShader.loadInt("numberOfTilesX", workGroupsX)

    glViewport(0, 0, window.width, window.height)
    glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

    val quadVertices = floatArrayOf(
        -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
    )

    val quadVAO = glGenVertexArrays()
    val quadVBO = glGenBuffers()
    glBindVertexArray(quadVAO)
    glBindBuffer(GL_ARRAY_BUFFER, quadVBO)
    glBufferData(
        GL_ARRAY_BUFFER,
        BufferUtils.createFloatBuffer(quadVertices.size).put(quadVertices).flip(),
        GL_STATIC_DRAW
    )
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * 4, 0)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * 4, 3 * 4)


    println(glGetError())

    while (!window.shouldClose) {
        input.update()
        Camera.current.update()

        val projection = Matrix4f()
        Matrix4f().perspective(45f, window.width.toFloat() / window.height.toFloat(), 0.1f, 300.0f, projection)
        val view = Camera.current.viewMatrix
        val cameraPosition = Camera.current.position

        depthShader.start()
        depthShader.loadMatrix("projection", projection)
        depthShader.loadMatrix("view", view)

        // Bind the depth map's frame buffer and draw the depth map to it
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO)
        glClear(GL_DEPTH_BUFFER_BIT)
        renderer.render(obj)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        lightCullingShader.start()
        lightCullingShader.loadMatrix("projection", projection)
        lightCullingShader.loadMatrix("view", view)

        glActiveTexture(GL_TEXTURE4)
        lightCullingShader.loadInt("depthMap", 4)
        glBindTexture(GL_TEXTURE_2D, depthMap)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, lightBuffer)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, visibleLightIndicesBuffer)

        glDispatchCompute(workGroupsX, workGroupsY, 1)

        // Unbind the depth map
        glActiveTexture(GL_TEXTURE4)
        glBindTexture(GL_TEXTURE_2D, 0)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, 0);

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT);


//
//        depthDebugShader.start();
//        depthDebugShader.loadMatrix("projection", projection)
//        depthDebugShader.loadMatrix("view", view)
//
//        depthDebugShader.start()
//        renderer.render(obj)
//        depthDebugShader.stop()





        glBindFramebuffer(GL_FRAMEBUFFER, hdrFBO)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        lightAccumulationShader.start()
        lightAccumulationShader.loadMatrix("projection", projection)
        lightAccumulationShader.loadMatrix("view", view)
        lightAccumulationShader.loadVector("viewPosition", cameraPosition)

        renderer.render(obj)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // Weirdly, moving this call drops performance into the floor

        hdrShader.start()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, colorBuffer)
        hdrShader.loadFloat("exposure", 1.0f)

        glBindVertexArray(quadVAO)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glBindVertexArray(0)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, 0)


        window.update()
    }
}