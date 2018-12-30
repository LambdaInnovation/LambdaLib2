import cn.lambdalib2.render.*
import cn.lambdalib2.util.Colors
import cn.lambdalib2.vis.editor.ImGui
import cn.ll2test.common.OfflineTestUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11.*

object TestImGui {

    @JvmStatic
    fun main(args: Array<String>) {
        OfflineTestUtils.hackNatives()
        ImGui.setWithoutMC()

        Display.setDisplayMode(DisplayMode(800, 600))
        Display.create()

        while (!Display.isCloseRequested()) {
            glViewport(0, 0, 800, 600)
            glClearColor(0.1f, 0.1f, 0f, 0f)
            glClear(GL_COLOR_BUFFER_BIT)

            ImGui.newFrame(0.0f, charArrayOf())

            // Show demo window
            ImGui.showDemoWindow(true)

            // Show java window
            ImGui.begin("My Test Window", false)
            ImGui.text("Some Text")
            ImGui.labelText("Label", "Text")
            ImGui.textColored(Colors.fromRGB32(0xEE22CCFF.toInt()), "Text Colored")
            ImGui.textWrapped("asldkfjaksldfjaksld jfasl;kdfj a;sldkjfa;sldjv ao[sidf jaopsdfj[aisdf Some wrapped text")
            ImGui.end()

            ImGui.render()

            val error = glGetError()
            if (error != GL_NO_ERROR) {
                println("Err: " + error)
            }

            Display.update()
        }

        Display.destroy()
    }

}