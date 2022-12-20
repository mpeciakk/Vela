package light

import org.joml.Vector3f

data class DirectionalLight(val color: Vector3f, val ambientIntensity: Float, val direction: Vector3f, val diffuseIntensity: Float) {
}