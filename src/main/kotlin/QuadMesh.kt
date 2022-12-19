import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class QuadMesh {
    var numVertices = 0
    var vaoId = 0
    private var vboIdList = mutableListOf<Int>()

    init {
        MemoryStack.stackPush().use { stack ->
            val positions = floatArrayOf(
                -1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f
            )
            val textCoords = floatArrayOf(
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
            )
            val indices = intArrayOf(0, 2, 1, 1, 2, 3)
            numVertices = indices.size
            vaoId = GL30.glGenVertexArrays()
            GL30.glBindVertexArray(vaoId)

            // Positions VBO
            var vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            val positionsBuffer = stack.callocFloat(positions.size)
            positionsBuffer.put(0, positions)
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, positionsBuffer, GL30.GL_STATIC_DRAW)
            GL30.glEnableVertexAttribArray(0)
            GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0)

            // Texture coordinates VBO
            vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            val textCoordsBuffer: FloatBuffer = MemoryUtil.memAllocFloat(textCoords.size)
            textCoordsBuffer.put(0, textCoords)
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textCoordsBuffer, GL30.GL_STATIC_DRAW)
            GL30.glEnableVertexAttribArray(1)
            GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0)

            // Index VBO
            vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            val indicesBuffer = stack.callocInt(indices.size)
            indicesBuffer.put(0, indices)
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL30.GL_STATIC_DRAW)
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
            GL30.glBindVertexArray(0)
        }
    }

    fun cleanup() {
        vboIdList.stream().forEach { buffer: Int? ->
            GL30.glDeleteBuffers(
                buffer!!
            )
        }
        GL30.glDeleteVertexArrays(vaoId)
    }
}