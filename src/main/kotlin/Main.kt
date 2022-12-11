import ain.Window
import ain.rp.MeshRenderer
import ain.rp.Renderable
import aries.AssetManager
import asset.DefaultAssetLoader
import asset.Texture
import input.InputManager
import light.DirectionalLight
import model.ObjModel
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import render.DefaultRenderPipeline
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class GameObject(private val model: ObjModel) : Renderable() {
    override fun rebuild() {
//        getBuilder().drawCube(0f, 0f, -5f, 1f, 1f, 1f, true, true, true, true, true, true)

        getBuilder().setIndices(model.indices.asList())
        getBuilder().setVertices(model.vertices)
    }
}

fun main(args: Array<String>) {
    val window = Window(800, 600, "Ain engine")
    window.create()

    glEnable(GL_DEPTH_TEST)

    val assetManager = AssetManager()
    val assetLoader = DefaultAssetLoader(assetManager)

    assetLoader.load()
    assetLoader.loadAssets()

    val texture = assetManager[Texture::class.java, "texture"]

    val input = InputManager()
    input.start(window)

    val camera = Camera(window.width, window.height, input)
    camera.makeCurrent()

    var lightAngle = -90f
    val lightIntensity = 0.0f
    val dlPosition = Vector3f(0f, 0f, 0f)

    val directionalLight = DirectionalLight(dlPosition, Vector3f(1f, 1f, 1f), lightIntensity)

    val renderer = MeshRenderer<GameObject>(DefaultRenderPipeline(assetManager, directionalLight))

    var lastFrameTime = -1L
    var deltaTime = 0.0f
    var frameCounterStart = 0L
    var fps = 0
    var frames = 0

    val obj = GameObject(assetManager[ObjModel::class.java, "model"])
    obj.markDirty()

    while (!window.shouldClose) {
        window.update()

        val time = System.nanoTime()
        if (lastFrameTime == -1L) lastFrameTime = time

        deltaTime = (time - lastFrameTime) / 1000000000.0f

        lastFrameTime = time

        if (time - frameCounterStart >= 1000000000) {
            fps = frames
            frames = 0
            frameCounterStart = time
        }
        frames++

//        light.position.z += cos(frames.toDouble()).toFloat()

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0f, 0f, 1f, 1f)
        input.update()
        Camera.current.update()

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture.id)

        lightAngle += 0.5f
        if (lightAngle > 90) {
            directionalLight.intensity = 0f

            if (lightAngle >= 360) {
                lightAngle = -90f
            }
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            val factor = 1 - (abs(lightAngle) - 80) / 10f
            directionalLight.intensity = factor
        }

        val ang = Math.toRadians(lightAngle.toDouble())
        directionalLight.direction.x = sin(ang).toFloat()
        directionalLight.direction.y = cos(ang).toFloat()

        renderer.render(obj)
    }
}