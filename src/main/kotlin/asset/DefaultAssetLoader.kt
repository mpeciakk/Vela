package asset

import model.ObjModel
import ain.mesh.Vertex
import aries.AssetLoader
import aries.AssetManager
import de.javagl.obj.FloatTuples
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import javax.imageio.ImageIO


class DefaultAssetLoader(assetManager: AssetManager) : AssetLoader(assetManager) {

    init {
        deserializers[Texture::class.java] = object : Deserializer<Texture>() {
            override fun deserialize(name: String): Texture {
                val textureId = glGenTextures()
                glBindTexture(GL_TEXTURE_2D, textureId)
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

                val image = ImageIO.read(getFileStream("/textures/$name.png"))

                val width = image.width
                val height = image.height

                val pixelsRaw = image.getRGB(0, 0, width, height, null, 0, height)

                val pixels = BufferUtils.createByteBuffer(width * height * 4)

                try {
                    for (i in 0 until width) {
                        for (j in 0 until height) {
                            val pixel = pixelsRaw[i * width + j]
                            pixels.put((pixel shr 16 and 0xFF).toByte())
                            pixels.put((pixel shr 8 and 0xFF).toByte())
                            pixels.put((pixel and 0xFF).toByte())
                            pixels.put((pixel shr 24 and 0xFF).toByte())
                        }
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                    pixels.put(0x88.toByte())
                    pixels.put(0x88.toByte())
                    pixels.put(0x88.toByte())
                    pixels.put(0x00.toByte())
                }

                pixels.flip()

                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

                glBindTexture(GL_TEXTURE_2D, 0);

                return Texture(textureId)
            }
        }

        deserializers[ObjModel::class.java] = object : Deserializer<ObjModel>() {
            override fun deserialize(name: String): ObjModel {
                val obj = ObjUtils.convertToRenderable(
                    ObjReader.read(getFileStream("/models/$name.obj"))
                )

                val vertices = mutableListOf<Vertex>()

                var ti = 0
                var vi = 0
                var ni = 0

                for (i in 0 until obj.numVertices) {
                    val vertex = obj.getVertex(vi++)
                    val uv = if (obj.numTexCoords > ti) obj.getTexCoord(ti++) else FloatTuples.create(0f, 0f, 0f)
                    val normal = if (obj.numNormals > ni) obj.getNormal(ni++) else FloatTuples.create(0f, 0f, 0f)

                    vertices.add(Vertex(vertex.x, vertex.y, vertex.z, uv.x, uv.y, null, normal.x, normal.y, normal.z))
                }

                return ObjModel(vertices, ObjData.getFaceVertexIndicesArray(obj))
            }
        }

        deserializers[String::class.java] = object : Deserializer<String>() {
            override fun deserialize(name: String): String {
                return getTextFile("/shaders/$name.glsl")
            }
        }
    }

    override fun load() {
        queueAsset("model", ObjModel::class.java)
        queueAsset("default", String::class.java)
        queueAsset("empty", String::class.java)
        queueAsset("gbuffer", String::class.java)
        queueAsset("depth", String::class.java)
        queueAsset("final", String::class.java)
    }
}