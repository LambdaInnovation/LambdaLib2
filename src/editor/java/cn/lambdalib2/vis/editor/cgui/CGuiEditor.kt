package cn.lambdalib2.vis.editor.cgui

import cn.lambdalib2.cgui.CGui
import cn.lambdalib2.cgui.Widget
import cn.lambdalib2.cgui.component.DrawTexture
import cn.lambdalib2.cgui.loader.CGUIDocument
import cn.lambdalib2.registry.StateEventCallback
import cn.lambdalib2.util.Colors
import cn.lambdalib2.util.MathUtils
import cn.lambdalib2.vis.editor.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.lwjgl.BufferUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
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

        init {
//            cgui.addWidget(
//                Widget().size(200.0f, 200.0f).addComponent(
//                    DrawTexture(null, Colors.fromHexColor(0xFF22FFFF.toInt()))
//                )
//            )
            val container = CGUIDocument.read(ResourceLocation("academy", "guis/rework/page_wireless.xml"))
            cgui.addWidget(container.getWidget(0))
        }

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
            sceneRect.y = Display.getHeight() - sceneRect.y - sceneRect.w
            sceneRect.w -= 20

            ImGui.end()

            ImGui.render()

            drawWidget(sceneRect)
        }

        private fun drawWidget(rect: Vector4f) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
//            GL11.glLoadIdentity()
            val aspect = rect.z / rect.w

            val lastViewport = glGetIntegerv(GL11.GL_VIEWPORT, 4);
            GL11.glViewport(rect.x.toInt(), rect.y.toInt(), rect.z.toInt(), rect.w.toInt())

            val strechX = rect.z / Display.getWidth()
            val strechY = rect.w / Display.getHeight()

            val scaledRes = ScaledResolution(Minecraft.getMinecraft())
            val invScaleFactor = 1.0f / scaledRes.scaleFactor.toFloat()
            val virtualScaleFactor = Math.max(Math.max((rect.z / 320).toInt(), (rect.w / 240).toInt()), 1)
//            val virtualScaleFactor = 1
            GL11.glScalef(virtualScaleFactor * invScaleFactor / strechX, virtualScaleFactor * invScaleFactor / strechY, 1.0f)

            cgui.resize(320.0f, 240.0f)
            cgui.draw()

            // Restore
            GL11.glViewport(lastViewport[0], lastViewport[1], lastViewport[2], lastViewport[3])

            GL11.glPopMatrix()
        }


        private val _getIntegervBuf = BufferUtils.createIntBuffer(16)

        private fun glGetIntegerv(name: Int, count: Int): IntArray {
            _getIntegervBuf.clear()
            GL11.glGetInteger(name, _getIntegervBuf)
            val ret = IntArray(count)
            _getIntegervBuf.get(ret)
            return ret
        }

    }

}