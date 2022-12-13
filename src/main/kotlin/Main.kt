import ain.Window
import ain.rp.MeshRenderer
import ain.rp.Renderable
import aries.AssetManager
import asset.DefaultAssetLoader
import input.InputManager
import model.ObjModel
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL43.*
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

    val obj = GameObject(assetManager[ObjModel::class.java, "magnet"])
    obj.markDirty()

    glEnable(GL_DEPTH_TEST)
    glDepthMask(true)
    glEnable(GL_CULL_FACE)
    glDepthFunc(GL_LESS)

    val NUM_LIGHTS = 1024
    val MAX_LIGHTS_PER_TILE = 1024

    val depthShader = DepthShader(assetManager[String::class.java, "depth"])
    val lightCullingShader = ComputeShader(assetManager[String::class.java, "light_culling"])
    val lightAccumulationShader = LightAccumulationShader(assetManager[String::class.java, "light_accumulation"])
    val hdrShader = HDRShader(assetManager[String::class.java, "hdr"])
    val depthDebugShader = HDRShader(assetManager[String::class.java, "hdr"])
    val lightDebugShader = HDRShader(assetManager[String::class.java, "light_debug"])

    // Create depth map buffer and bind its texture to it
    val depthBuffer = glGenFramebuffers()
    glBindFramebuffer(GL_FRAMEBUFFER, depthBuffer)
    val depthTexture = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, depthTexture)
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

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0)

    glDrawBuffer(GL_NONE)
    glReadBuffer(GL_NONE)


    // Final buffer
    val renderBuffer = glGenFramebuffers()
    val renderTexture = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, renderTexture)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, window.width, window.height, 0, GL_RGB, GL_FLOAT, null as ByteBuffer?)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

    val renderDepthBuffer = glGenRenderbuffers()
    glBindRenderbuffer(GL_RENDERBUFFER, renderDepthBuffer)
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, window.width, window.height)

    glBindFramebuffer(GL_FRAMEBUFFER, renderBuffer)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderTexture, 0)
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderDepthBuffer)
    glBindFramebuffer(GL_FRAMEBUFFER, 0)


    val workGroupsX = (window.width + (window.width % 16)) / 16
    val workGroupsY = (window.height + (window.height % 16)) / 16
    val numberOfTiles = workGroupsX * workGroupsY

    val lightBuffer = glGenBuffers()
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, lightBuffer)
    glBufferData(GL_SHADER_STORAGE_BUFFER, NUM_LIGHTS.toLong() * 12 * 4, GL_DYNAMIC_DRAW)
    glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, lightBuffer)
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    val visibleLightIndicesBuffer = glGenBuffers()
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, visibleLightIndicesBuffer)
    glBufferData(GL_SHADER_STORAGE_BUFFER, workGroupsX * workGroupsY * MAX_LIGHTS_PER_TILE.toLong(), GL_DYNAMIC_DRAW)
    glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, visibleLightIndicesBuffer)
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    var mappedBuffer = BufferUtils.createByteBuffer(NUM_LIGHTS * 12 * 4)

    // setup lights
    glBindBuffer(GL_SHADER_STORAGE_BUFFER, lightBuffer)
    mappedBuffer = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_READ_WRITE, mappedBuffer)!!
//    val fbb = mappedBuffer.asFloatBuffer()
//    val fbb = bb!!.asFloatBuffer()

//    val fb = FloatArray(32 * 12)

    for (i in 0 until 32 * 12 step 12) {
//        mappedBuffer.putFloat(i + 0, Random().nextFloat(20f) - 10f)
//        mappedBuffer.putFloat(i + 1, Random().nextFloat(20f) - 10f)
//        mappedBuffer.putFloat(i + 2, Random().nextFloat(20f) - 10f)
//        mappedBuffer.putFloat(i + 3, 1f)
//        mappedBuffer.putFloat(i + 4, Random().nextFloat() + 1f)
//        mappedBuffer.putFloat(i + 5, Random().nextFloat() + 1f)
//        mappedBuffer.putFloat(i + 6, Random().nextFloat() + 1f)
//        mappedBuffer.putFloat(i + 7, 1f)
//        mappedBuffer.putFloat(i + 8, 0f)
//        mappedBuffer.putFloat(i + 9, 0f)
//        mappedBuffer.putFloat(i + 10, 0f)
//        mappedBuffer.putFloat(i + 11, 30f)

        mappedBuffer.putFloat(Random().nextFloat(20f) - 10f)
        mappedBuffer.putFloat(Random().nextFloat(20f) - 10f)
        mappedBuffer.putFloat(Random().nextFloat(20f) - 10f)
        mappedBuffer.putFloat(1f)
        mappedBuffer.putFloat(Random().nextFloat() + 1f)
        mappedBuffer.putFloat(Random().nextFloat() + 1f)
        mappedBuffer.putFloat(Random().nextFloat() + 1f)
        mappedBuffer.putFloat(1f)
        mappedBuffer.putFloat(0f)
        mappedBuffer.putFloat(0f)
        mappedBuffer.putFloat(0f)
        mappedBuffer.putFloat(30f)
    }

//    fbb.put(fb)
//    fbb.flip()

//    mappedBuffer.flip()

    glBufferData(GL_SHADER_STORAGE_BUFFER, BufferUtils.createByteBuffer(mappedBuffer.capacity()).put(mappedBuffer).flip(), GL_STATIC_DRAW)

    glUnmapBuffer(GL_SHADER_STORAGE_BUFFER)

    glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0)

    val model = Matrix4f().scale(0.1f, 0.1f, 0.1f)

    depthShader.start()
    depthShader.loadMatrix("model", model)

    lightCullingShader.start()
    lightCullingShader.loadInt("lightCount", NUM_LIGHTS)

    lightAccumulationShader.start()
    lightAccumulationShader.loadMatrix("model", model)
    lightAccumulationShader.loadInt("numberOfTilesX", workGroupsX)
    lightAccumulationShader.loadVector("viewPosition", Camera.current.position)

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

        var projection = Matrix4f()
        projection =
            Matrix4f().perspective(45f, window.width.toFloat() / window.height.toFloat(), 0.1f, 300.0f, projection)
        val view = Camera.current.viewMatrix
        val cameraPosition = Camera.current.position

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Depth FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthBuffer)
        glClear(GL_DEPTH_BUFFER_BIT)

        depthShader.start()

        glDrawBuffers(GL_COLOR_ATTACHMENT0)
        glClearBufferfv(GL_COLOR, 0, floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f))
        glClearBufferfv(GL_DEPTH, 0, floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f))

        depthShader.loadMatrix("projection", projection)
        depthShader.loadMatrix("view", view)

        renderer.render(obj)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Light culling
        glDepthFunc(GL_EQUAL)
        glClear(GL_COLOR_BUFFER_BIT)

        lightCullingShader.start()
        lightCullingShader.loadMatrix("projection", projection)
        lightCullingShader.loadMatrix("view", view)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, lightBuffer)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, visibleLightIndicesBuffer)

        glActiveTexture(GL_TEXTURE4)
        lightCullingShader.loadInt("depthMap", 4)
        glBindTexture(GL_TEXTURE_2D, depthTexture)

        glDispatchCompute(workGroupsX, workGroupsY, 1)
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT)

        glActiveTexture(GL_TEXTURE4)
        glBindTexture(GL_TEXTURE_2D, 0)

//        // Render FBO
        glDepthFunc(GL_LESS)

        glBindFramebuffer(GL_FRAMEBUFFER, renderBuffer)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        lightAccumulationShader.start()
        lightAccumulationShader.loadMatrix("projection", projection)
        lightAccumulationShader.loadMatrix("view", view)
        lightAccumulationShader.loadVector("viewPosition", cameraPosition)

        renderer.render(obj)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        // Render quad
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // Weirdly, moving this call drops performance into the floor

        hdrShader.start()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, renderTexture)

        glBindVertexArray(quadVAO)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)
        glBindVertexArray(0)

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, 0)

        glDisable(GL_DEPTH_TEST)

        window.update()
    }
}