package asset

import ain.mesh.Vertex
import aries.AssetLoader
import aries.AssetManager
import de.javagl.obj.FloatTuples
import de.javagl.obj.ObjData
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjUtils
import model.ObjModel
import org.lwjgl.BufferUtils
import org.lwjgl.assimp.*
import org.lwjgl.assimp.Assimp.aiImportFile
import org.lwjgl.assimp.Assimp.aiImportFileEx
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
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
        queueAsset("texture", Texture::class.java)
        queueAsset("model", ObjModel::class.java)
        queueAsset("sponza", ObjModel::class.java)
        queueAsset("magnet", ObjModel::class.java)
        queueAsset("default", String::class.java)
        queueAsset("depth", String::class.java)
        queueAsset("light_accumulation", String::class.java)
        queueAsset("hdr", String::class.java)
        queueAsset("light_culling", String::class.java)
        queueAsset("depth_debug", String::class.java)
        queueAsset("light_debug", String::class.java)
    }

    @Throws(IOException::class)
    fun ioResourceToByteBuffer(resource: String, bufferSize: Int): ByteBuffer {
        var buffer: ByteBuffer
        val url = Thread.currentThread().contextClassLoader.getResource(resource)
            ?: throw IOException("Classpath resource not found: $resource")
        val file = File(url.file)
        if (file.isFile()) {
            val fis = FileInputStream(file)
            val fc: FileChannel = fis.getChannel()
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())
            fc.close()
            fis.close()
        } else {
            buffer = BufferUtils.createByteBuffer(bufferSize)
            val source = url.openStream() ?: throw FileNotFoundException(resource)
            try {
                val buf = ByteArray(8192)
                while (true) {
                    val bytes = source.read(buf, 0, buf.size)
                    if (bytes == -1) break
                    if (buffer.remaining() < bytes) buffer = resizeBuffer(
                        buffer,
                        Math.max(buffer.capacity() * 2, buffer.capacity() - buffer.remaining() + bytes)
                    )
                    buffer.put(buf, 0, bytes)
                }
                buffer.flip()
            } finally {
                source.close()
            }
        }
        return buffer
    }

    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer = BufferUtils.createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }
}