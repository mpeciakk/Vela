package shader

import ain.shader.Shader
import light.PointLight


class DefaultShader(source: String) : Shader(source) {
    override fun bindAttributes() {
        bindAttribute(0, "position")
        bindAttribute(1, "uv")
        bindAttribute(2, "normal")
    }
}