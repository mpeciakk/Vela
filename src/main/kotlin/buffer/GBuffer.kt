package buffer

import ain.Destroyable
import ain.Window
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer


class GBuffer(window: Window) : Destroyable {
    var id = 0
    lateinit var textures: IntArray
    private var width = 0
    private var height = 0

    init {
        id = glGenFramebuffers()
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)
        textures = IntArray(TOTAL_TEXTURES)
        GL11.glGenTextures(textures)
        width = window.width
        height = window.height
        for (i in 0 until TOTAL_TEXTURES) {
            glBindTexture(GL_TEXTURE_2D, textures[i])
            val attachmentType = if (i == TOTAL_TEXTURES - 1) {
                glTexImage2D(
                    GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT,
                    null as ByteBuffer?
                )
                GL_DEPTH_ATTACHMENT
            } else {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, width, height, 0, GL_RGBA, GL_FLOAT, null as ByteBuffer?)
                GL_COLOR_ATTACHMENT0 + i
            }
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST.toFloat())
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST.toFloat())
            glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentType, GL_TEXTURE_2D, textures[i], 0)
        }

        MemoryStack.stackPush().use { stack ->
            val intBuff = stack.mallocInt(TOTAL_TEXTURES)
            for (i in 0 until TOTAL_TEXTURES) {
                intBuff.put(i, GL_COLOR_ATTACHMENT0 + i)
            }
            glDrawBuffers(intBuff)
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun blit() {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0); // write to default framebuffer
        glBlitFramebuffer(
            0, 0, width, height, 0, 0, width, height, GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    override fun destroy() {
        glDeleteFramebuffers(id)

        for (i in 0 until TOTAL_TEXTURES) {
            glDeleteTextures(textures[i])
        }
    }

    companion object {
        private const val TOTAL_TEXTURES = 4
    }
}