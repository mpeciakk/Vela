package scene

import light.DirectionalLight
import light.PointLight

abstract class Scene {
    val pointLights = mutableListOf<PointLight>()
    var directionalLight: DirectionalLight? = null
    var initialized = false

    abstract fun render(deltaTime: Float)

    open fun create() {}
    fun show() {}
}