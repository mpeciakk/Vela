package buffer

import ain.Destroyable
import ain.Window
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer


class GBuffer(window: Window) : Destroyable {
    private var id = 0
    private var textures: IntArray
    private var width = 0
    private var height = 0

    init {
        id = glGenFramebuffers()
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)

        textures = IntArray(TOTAL_TEXTURES)
        glGenTextures(textures)

        width = window.width
        height = window.height

        // Create textures for position, diffuse color, specular color, normal, shadow factor and depth
        // All coordinates are in world coordinates system
        for (i in 0 until TOTAL_TEXTURES) {
            glBindTexture(GL_TEXTURE_2D, textures[i])
            val attachmentType: Int = when (i) {
                TOTAL_TEXTURES - 1 -> {
                    // Depth component
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, ByteBuffer.allocate(0))

                    GL_DEPTH_ATTACHMENT
                }

                else -> {
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_RGB, GL_FLOAT, ByteBuffer.allocate(0))

                    GL_COLOR_ATTACHMENT0 + i
                }
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentType, GL_TEXTURE_2D, textures[i], 0)
        }
        MemoryStack.stackPush().use { stack ->
            val intBuff = stack.mallocInt(TOTAL_TEXTURES)
            val values = intArrayOf(
                GL_COLOR_ATTACHMENT0,
                GL_COLOR_ATTACHMENT1,
                GL_COLOR_ATTACHMENT2,
                GL_COLOR_ATTACHMENT3,
                GL_COLOR_ATTACHMENT4,
                GL_COLOR_ATTACHMENT5
            )
            for (i in values.indices) {
                intBuff.put(values[i])
            }
            intBuff.flip()
            glDrawBuffers(intBuff)
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun destroy() {
        glDeleteFramebuffers(id)

        for (i in 0 until TOTAL_TEXTURES) {
            glDeleteTextures(textures[i])
        }
    }

    companion object {
        private const val TOTAL_TEXTURES = 6
    }
}