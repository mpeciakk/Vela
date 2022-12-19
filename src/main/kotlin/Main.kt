import ain.Window
import ain.rp.MeshRenderer
import ain.rp.Renderable
import aries.AssetManager
import asset.DefaultAssetLoader
import input.InputManager
import light.PointLight
import model.ObjModel
import org.joml.Vector3f
import org.lwjgl.opengl.GL30.*
import render.DefaultRenderPipeline
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
        getBuilder().drawCube(0f, 0f, 0f, 1f, 1f, 1f, true, true, true, true, true, true)
    }
}

class TestScene(private val app: App) : Scene() {
    private val deferredRenderer = MeshRenderer<GameObject>(DeferredRenderingPipeline(app.assetManager, app.window))
    private val forwardRenderer = MeshRenderer<PointLightObj>(DefaultRenderPipeline(app.assetManager))
    private val obj = GameObject(app.assetManager[ObjModel::class.java, "model"])
    private val light = PointLightObj()

    override fun create() {
        obj.markDirty()
        light.markDirty()
    }

    override fun render(deltaTime: Float) {
        deferredRenderer.render(obj)

//        glBindFramebuffer(GL_READ_FRAMEBUFFER, deferredRenderer);
//        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0); // write to default framebuffer
//        glBlitFramebuffer(
//            0, 0, SCR_WIDTH, SCR_HEIGHT, 0, 0, SCR_WIDTH, SCR_HEIGHT, GL_DEPTH_BUFFER_BIT, GL_NEAREST
//        );
//        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        forwardRenderer.render(light)
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

        sceneManager.changeScene(scene)

        scene.pointLights.add(0, PointLight(Vector3f(1f, 1f, 1f), Vector3f(0f, 0f, 3f), 1f, 0f, 1f, 0f))
    }

    override fun render(deltaTime: Float) {
        window.pollEvents()
        input.update()
        Camera.current.update()

        sceneManager.render(deltaTime)

        window.swapBuffers()
    }

    override fun destroy() {
        window.destroy()
    }
}


fun main(args: Array<String>) {
    launch(App())
}