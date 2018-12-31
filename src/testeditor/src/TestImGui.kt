import cn.lambdalib2.render.*
import cn.lambdalib2.util.Colors
import cn.lambdalib2.vis.editor.ImGui
import cn.lambdalib2.vis.editor.ImGuiDir
import cn.ll2test.common.OfflineTestUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Vector2f

object TestImGui {
    enum class TestEnum {
        Fly, Walk, Dive, Crawl
    }

    @JvmStatic
    fun main(args: Array<String>) {
        OfflineTestUtils.hackNatives()
        ImGui.setWithoutMC()

        Display.setDisplayMode(DisplayMode(1280, 720))
        Display.create()

        val tex = Texture2D.loadFromResource(
            "/testeditor/miku.png",
            TextureImportSettings(
                TextureImportSettings.FilterMode.Blinear, TextureImportSettings.WrapMode.Clamp
            )
        )

        var testChkbox = false
        var testEnum = TestEnum.Crawl
        var testFloat = 0.0f
        var testFloat2 = floatArrayOf(1.0f, 2.0f)
        var testInt = 1
        var testAngle = 0.0f

        while (!Display.isCloseRequested()) {
            glViewport(0, 0, 1280, 720)
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

            ImGui.separator()
            ImGui.text("After separator")
            ImGui.newLine()
            ImGui.text("After newLine")
            ImGui.spacing()
            ImGui.text("After spacing")

            ImGui.bulletText("Hello bullet text!")
            ImGui.button("Button")
            if (ImGui.button("Sized button", Vector2f(100f, 100f))) {
                println("I'm clicked")
            }
            ImGui.arrowButton("Arrow Button", ImGuiDir.Left)
            ImGui.arrowButton("Arrow Btn2", ImGuiDir.Down)

            ImGui.image(tex.textureID, Vector2f(100f, 100f))
            ImGui.imageButton(tex.textureID, Vector2f(50f, 50f))

            testChkbox = ImGui.checkbox("Checkbox", testChkbox)

            ImGui.radioButton("RadioButton", true)

            val ix = ImGui.combo("TestEnum", testEnum.ordinal,
                TestEnum.values().map { it.name }.toTypedArray())
            testEnum = TestEnum.values()[ix]

            testFloat = ImGui.sliderFloat("SliderFloat", testFloat, 0.0f, 10.0f)
            ImGui.sliderFloat2("SliderFloat2", testFloat2, 0.0f, 10.0f)
            testInt = ImGui.sliderInt("SliderInt", testInt, 0, 24)

            testAngle = ImGui.sliderAngle("TestAngle", testAngle)

            ImGui.end()

            ImGui.render()

            val error = glGetError()
            if (error != GL_NO_ERROR) {
                println("Err: $error")
            }

            Display.update()
        }

        Display.destroy()
    }

}