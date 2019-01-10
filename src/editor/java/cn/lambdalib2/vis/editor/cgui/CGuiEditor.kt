package cn.lambdalib2.vis.editor.cgui

import cn.lambdalib2.cgui.CGui
import cn.lambdalib2.cgui.CGuiScreen
import cn.lambdalib2.registry.StateEventCallback
import cn.lambdalib2.render.Texture2D
import cn.lambdalib2.render.TextureImportSettings
import cn.lambdalib2.vis.editor.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector4f

object CGuiEditor {

    @JvmStatic
    @StateEventCallback
    fun init(ev: FMLServerStartingEvent) {
        ev.registerServerCommand(object : CommandBase() {
            override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
                Minecraft.getMinecraft().addScheduledTask {
                    Minecraft.getMinecraft().displayGuiScreen(Editor())
                }
            }

            override fun getUsage(sender: ICommandSender?): String = "/cgui"

            override fun getName(): String = "cgui"
        })
    }

    class Editor : GuiScreen() {
        val inputBuffer = ArrayList<Char>()
        var dWheel = 0.0f

        var targetPath: String? = null
        var targetWidget = null

        val cgui = CGui()

        override fun handleKeyboardInput() {
            super.handleKeyboardInput()
            val chr = Keyboard.getEventCharacter()
            if (!chr.isISOControl())
                inputBuffer += chr
        }

        override fun handleMouseInput() {
//            super.handleMouseInput()
            dWheel += Mouse.getEventDWheel()
        }

        override fun drawScreen(mx: Int, my: Int, w: Float) {
            super.drawScreen(mx, my, w)

            ImGui.newFrame(dWheel, inputBuffer.toCharArray())
            dWheel = 0.0f
            inputBuffer.clear()

            ImGui.showDemoWindow(true)

            ImGui.begin("Scene")

            val sceneRect = ImGui.getWindowRect()

            ImGui.end()

            ImGui.render()
        }

        val tex = Texture2D.loadFromResource("/assets/lambdalib2/textures/missing.png", TextureImportSettings(TextureImportSettings.FilterMode.Trilinear, TextureImportSettings.WrapMode.Clamp))

        private fun drawWidget(rect: Vector4f) {
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()
//            GL11.glOrtho(0, Screen)

            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glLoadIdentity()


            // Restore
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()

            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
        }

    }

}