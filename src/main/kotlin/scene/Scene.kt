package scene

import light.PointLight

abstract class Scene {
    val pointLights = mutableListOf<PointLight>()
    var initialized = false

    abstract fun render(deltaTime: Float)

    open fun create() {}
    fun show() {}
}