import ain.Window
import ain.rp.MeshRenderer
import ain.rp.Renderable
import aries.AssetManager
import asset.DefaultAssetLoader
import asset.Texture
import imgui.ImGui
import imgui.flag.ImGuiCond
import input.InputManager
import light.DirectionalLight
import model.ObjModel
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import render.DefaultRenderPipeline
import render.gui.GuiInstance
import render.gui.GuiRenderPipeline


class GameObject(private val model: ObjModel) : Renderable() {
    override fun rebuild() {
//        getBuilder().drawCube(0f, 0f, -5f, 1f, 1f, 1f, true, true, true, true, true, true)

        getBuilder().setIndices(model.indices.asList())
        getBuilder().setVertices(model.vertices)
    }
}

class TestGui : GuiInstance() {

    private val array = FloatArray(1)

    override fun render() {
        ImGui.newFrame()
        ImGui.setNextWindowPos(0f, 0f, ImGuiCond.Always)

        ImGui.text("Hello, world!")

        if (ImGui.beginPopupModal("popup")) {
            ImGui.text("test123")
            ImGui.separator()
            ImGui.text("321tset")

            if (ImGui.button("x")) {
                ImGui.closeCurrentPopup()
            }

            ImGui.endPopup()
        }


        if (ImGui.button("Przycisk?")) {
            ImGui.openPopup("popup")
        }

        ImGui.sliderFloat("label", array, -21f, 37f)

        ImGui.endFrame()
        ImGui.render()
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
    val guiRenderer = MeshRenderer<GuiInstance>(GuiRenderPipeline(assetManager, window))

    var lastFrameTime = -1L
    var deltaTime = 0.0f
    var frameCounterStart = 0L
    var fps = 0
    var frames = 0

    val obj = GameObject(assetManager[ObjModel::class.java, "model"])
    obj.markDirty()

    val gui = TestGui()
    gui.markDirty()

    while (!window.shouldClose) {
        window.pollEvents()

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

        renderer.render(obj)

        gui.update(window, input)

        guiRenderer.render(gui)

        window.swapBuffers()
    }
}