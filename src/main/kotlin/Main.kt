import ain.Window
import ain.rp.MeshRenderer
import ain.rp.Renderable
import aries.AssetManager
import asset.DefaultAssetLoader
import input.InputManager
import light.PointLight
import model.ObjModel
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL30.*
import render.ForwardRenderingPipeline
import render.DeferredRenderingPipeline
import scene.Scene
import scene.SceneManager


class GameObject(private val model: ObjModel) : Renderable() {
    override fun rebuild() {
        getBuilder().setIndices(model.indices.asList())
        getBuilder().setVertices(model.vertices)
    }
}

class PointLightObj : Renderable() {
    override fun rebuild() {
        getBuilder().drawCube(3f, 0f, 0f, 1f, 1f, 1f, true, true, true, true, true, true)
    }
}

class TestScene2(private val app: App) : Scene() {
    private val forwardRenderingPipeline = ForwardRenderingPipeline(app.assetManager)
    private val forwardRenderer = MeshRenderer<PointLightObj>(forwardRenderingPipeline)

    private val light = PointLightObj()

    override fun create() {
        light.markDirty()
    }

    override fun render(deltaTime: Float) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        forwardRenderer.render(light)
    }
}

class TestScene(private val app: App) : Scene() {
    private val deferredRenderingPipeline = DeferredRenderingPipeline(app.assetManager, app.window)
    private val deferredRenderer = MeshRenderer<GameObject>(deferredRenderingPipeline)

    private val obj = GameObject(app.assetManager[ObjModel::class.java, "model"])

    override fun create() {
        obj.markDirty()
    }

    override fun render(deltaTime: Float) {
        deferredRenderer.render(obj)
    }
}

class App : VelaApplication() {
    val window = Window(800, 600, "Ain engine")
    val assetManager = AssetManager()
    private val assetLoader = DefaultAssetLoader(assetManager)
    val input = InputManager()

    val camera = Camera(window.width, window.height, input)

    private val sceneManager = SceneManager()

    private lateinit var scene: Scene
    private lateinit var scene2: Scene

    override fun create() {
        window.create()

        glEnable(GL_MULTISAMPLE)
        glEnable(GL_DEPTH_TEST)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        assetLoader.load()
        assetLoader.loadAssets()

        input.start(window)

        camera.makeCurrent()

        scene = TestScene(this)
        scene2 = TestScene2(this)

        sceneManager.changeScene(scene)

        scene.pointLights.add(0, PointLight(Vector3f(1f, 1f, 1f), Vector3f(0f, 0f, 3f), 1f, 0f, 1f, 0f))
    }

    override fun render(deltaTime: Float) {
        window.pollEvents()
        input.update()
        Camera.current.update()

        sceneManager.render(deltaTime)

        window.swapBuffers()

        if (input.isKeyPressed(GLFW_KEY_Q)) {
            sceneManager.changeScene(scene2)
        }

        if (input.isKeyPressed(GLFW_KEY_E)) {
            sceneManager.changeScene(scene)
        }

        if (input.isKeyPressed(GLFW_KEY_ESCAPE)) {
            close()
        }
    }

    override fun destroy() {
        window.destroy()
    }
}

fun main(args: Array<String>) {
    launch(App())
}