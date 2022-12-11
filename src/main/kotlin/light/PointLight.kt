package light

import org.joml.Vector3f


class PointLight(val color: Vector3f, val position: Vector3f, val intensity: Float, val constant: Float, val linear: Float, val exponent: Float)