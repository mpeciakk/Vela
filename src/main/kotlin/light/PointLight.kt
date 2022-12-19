package light

import org.joml.Vector3f


data class PointLight(val color: Vector3f, val position: Vector3f, val intensity: Float, val constant: Float, val linear: Float, val exponent: Float) {
    fun copy() = PointLight(color, position, intensity, constant, linear, exponent)
}