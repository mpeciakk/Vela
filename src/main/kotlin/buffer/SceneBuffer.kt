package buffer

import ain.Destroyable
import ain.Window
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class SceneBuffer(window: Window) : Destroyable {
    private var id: Int
    private var textureId: Int

    init {
        id = glGenFramebuffers()
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, id)

        val textures = IntArray(1)
        glGenTextures(textures)
        textureId = textures[0]
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D,0, GL_RGB32F, window.width, window.height, 0, GL_RGB, GL_FLOAT, ByteBuffer.allocate(0))

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun destroy() {
        glDeleteFramebuffers(id)
        glDeleteTextures(textureId)
    }
}