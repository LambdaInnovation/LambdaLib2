package cn.lambdalib2.vis.editor.cgui

import cn.lambdalib2.cgui.CGui
import cn.lambdalib2.cgui.Widget
import cn.lambdalib2.cgui.loader.CGUIDocument
import cn.lambdalib2.registry.StateEventCallback
import cn.lambdalib2.util.Colors
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
import org.lwjgl.util.Color
import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Vector4f

object CGuiEditor {

    @JvmStatic
    @StateEventCallback
    fun init(ev: FMLServerStartingEvent) {
        ev.registerServerCommand(object : CommandBase() {
            override fun getUsage(sender: ICommandSender?): String = "/cgui"

            override fun getName(): String = "cgui"

            override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<out String>) {
                Minecraft.getMinecraft().addScheduledTask {
                    Minecraft.getMinecraft().displayGuiScreen(Editor())
                }
            }
        })
    }

    private class SceneConf(
        var fullscreen: Boolean = false,
        var simulateMC: Boolean = true,
        val offset: Vector2f = Vector2f(),
        var scale: Float = 1f
    )

    private class Conf(
        var drawBackground: Boolean = false,
        val sceneConf: SceneConf = SceneConf()
    )

    private class Editor : GuiScreen() {
        val inputBuffer = ArrayList<Char>()
        var dWheel = 0.0f

        var targetPath: String? = null
        var targetWidget: Widget = Widget()

        val cgui = CGui()

        val conf = Conf()

        init {
//            cgui.addWidget(
//                Widget().size(200.0f, 200.0f).addComponent(
//                    DrawTexture(null, Colors.fromHexColor(0xFF22FFFF.toInt()))
//                )
//            )
            val container = CGUIDocument.read(ResourceLocation("academy", "guis/rework/page_wireless.xml"))
            targetWidget = container.getWidget(0)
            cgui.addWidget(targetWidget)
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

            if (conf.drawBackground)
                drawDefaultBackground()

            GL11.glEnable(GL11.GL_BLEND);
            ImGui.newFrame(dWheel, inputBuffer.toCharArray())
            dWheel = 0.0f
            inputBuffer.clear()

            // ImGui calss begin
            doMenu()
            doHierarchy()
            ImGui.showDemoWindow(true)
            val sceneRect = doScene()
            // ImGui calls end

            ImGui.render()

            drawSceneContents(sceneRect)

            GL11.glDisable(GL11.GL_BLEND)
        }

        private fun doScene(): Vector4f {
            ImGui.begin("Scene")

            val sceneRect = ImGui.getWindowRect()
            sceneRect.y = Display.getHeight() - sceneRect.y - sceneRect.w
            sceneRect.w -= 20

            ImGui.end()

            return sceneRect
        }

        private fun doMenu() {
            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("File")) {

                    if (ImGui.menuItem("Open")) {

                    }

                    if (ImGui.menuItem("Save")) {

                    }

                    if (ImGui.menuItem("Save As")) {

                    }

                    ImGui.endMenu()
                }

                if (ImGui.beginMenu("View")) {
                    conf.drawBackground = ImGui.checkbox("Black Background", conf.drawBackground)

                    val sceneConf = conf.sceneConf
//                    sceneConf.fullscreen = ImGui.checkbox("Fullscreen", sceneConf.fullscreen)
                    sceneConf.simulateMC = ImGui.checkbox("Simulate MC", sceneConf.simulateMC)

                    if (!sceneConf.simulateMC) {
                        ImGui.sliderVector2("Offset", sceneConf.offset, -1000f, 1000f)
                        ImGui.sameLine()
                        if (ImGui.button("Reset Off")) {
                            sceneConf.offset.set(0f, 0f)
                        }

                        sceneConf.scale = ImGui.sliderFloat("Scale", sceneConf.scale, 0.3f, 3f)
                        ImGui.sameLine()
                        if (ImGui.button("Reset Scl")) {
                            sceneConf.scale = 1f
                        }
                    }

                    ImGui.endMenu()
                }

                if (ImGui.beginMenu("About")) {
                    ImGui.textColored(Colors.fromRGB32(0x8888FF), "CGUIEditor v2.0")
                    ImGui.textColored(Colors.fromRGB32(0x8888FF), "author: WeAthFolD")
                    ImGui.endMenu()
                }


                ImGui.endMainMenubar()
            }
        }

        private fun doHierarchy() {
            ImGui.begin("Hierarchy")
            if (ImGui.beginMenuBar()) {
                if (ImGui.button("+")) {

                }

                if (ImGui.button("-")) {

                }

                if (ImGui.button("↑")) {

                }

                if (ImGui.button("↓")) {

                }

                ImGui.endMenuBar()
            }

            fun doWidget(w: Widget) {
                val isChild = w.widgetCount() == 0
                if (ImGui.treeNodeEx(w.name, if (isChild) ImGui.TreeNodeFlags_Leaf else 0)) {
                    for (child in w) {
                        doWidget(child)
                    }
                    ImGui.treePop()
                }
            }

            doWidget(targetWidget)
            ImGui.end()
        }


        private fun drawSceneContents(rect: Vector4f) {
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
            val virtualScaleFactor = when (conf.sceneConf.simulateMC) {
                true -> Math.max(Math.max((rect.z / 320).toInt(), (rect.w / 240).toInt()), 1)
                false -> 1
            }

//            val virtualScaleFactor = 1
            GL11.glScalef(virtualScaleFactor * invScaleFactor / strechX, virtualScaleFactor * invScaleFactor / strechY, 1.0f)

            if (!conf.sceneConf.simulateMC) {
                GL11.glTranslatef(conf.sceneConf.offset.x, conf.sceneConf.offset.y, 0f)
                val scl = conf.sceneConf.scale
                GL11.glScalef(scl, scl, 1f)
            }

            cgui.resize(320.0f, 240.0f)
            cgui.draw()

            // Restore
            GL11.glViewport(lastViewport[0], lastViewport[1], lastViewport[2], lastViewport[3])

            GL11.glPopMatrix()
        }

        // Utils

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