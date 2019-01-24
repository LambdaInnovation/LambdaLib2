package cn.lambdalib2.vis.editor.cgui

import cn.lambdalib2.cgui.CGui
import cn.lambdalib2.cgui.Widget
import cn.lambdalib2.cgui.annotation.CGuiEditorComponent
import cn.lambdalib2.cgui.component.Component
import cn.lambdalib2.cgui.component.Transform
import cn.lambdalib2.cgui.loader.CGUIDocument
import cn.lambdalib2.registry.StateEventCallback
import cn.lambdalib2.render.font.Fonts
import cn.lambdalib2.render.font.IFont
import cn.lambdalib2.s11n.xml.DOMS11n
import cn.lambdalib2.util.*
import cn.lambdalib2.vis.editor.ImGui
import cn.lambdalib2.vis.editor.ObjectEditor
import cn.lambdalib2.vis.editor.ObjectInspection
import com.google.gson.Gson
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.BufferUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL30.*
import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector4f
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.lang.reflect.Field
import java.util.function.Consumer

@SideOnly(Side.CLIENT)
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
        var drawBackground: Boolean = true,
        val sceneConf: SceneConf = SceneConf()
    )

    private val confPath = File("./cgui.json")

    private val gson = Gson()

    internal val compponentTypes = ReflectionUtils.getClasses(CGuiEditorComponent::class.java)

    // The gui and opening file persists as long as you don't quit MC
    private val cgui = CGui()
    private var targetPath: File? = null

    // Config persists in disk
    private fun writeConf(conf: Conf) {
        val json = gson.toJson(conf)
        confPath.writeText(json)
    }

    private fun readConf(): Conf {
        if (!confPath.isFile)
            return Conf()

        return gson.fromJson(confPath.readText(), Conf::class.java)
    }

    private class Editor : GuiScreen() {
        val inputBuffer = ArrayList<Char>()
        var dWheel = 0.0f

        var selectedWidget: Widget? = null
        var reparentingWidget: Widget? = null
        var sceneRect: Vector4f = Vector4f()

        val conf = readConf()
        val saveScheduler = TickScheduler()

        init {
            saveScheduler.everySec(10f).run { writeConf(conf) }
        }

        val inspection = object : ObjectInspection() {
            override fun getExposedFields(klass: Class<*>?): MutableList<Field> {
                return DOMS11n.instance.serHelper.getExposedFields(klass)
            }

            init {
                register(object: ObjectEditor<IFont>() {
                    override fun inspect(target: IFont, fieldName: String): IFont {
                        val fonts = Fonts.getFonts().toList()
                        val fontNames = fonts.map { Fonts.getName(it) }.toTypedArray()
                        val ix = fonts.indexOf(target)
                        val newIx = ImGui.combo(fieldName, ix, fontNames)
                        return fonts[newIx]
                    }
                }, IFont::class.java)
            }
        }

        override fun handleKeyboardInput() {
            val chr = Keyboard.getEventCharacter()
            if (!chr.isISOControl())
                inputBuffer += chr
        }

        override fun handleMouseInput() {
            super.handleMouseInput()
            dWheel += Mouse.getEventDWheel()
        }

        override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
            val mpos = mapMousePos(mouseX, mouseY) ?: return
            cgui.mouseClickMove(mpos.x.toInt(), mpos.y.toInt(), clickedMouseButton, timeSinceLastClick)
        }

        override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            val mpos = mapMousePos(mouseX, mouseY) ?: return
            cgui.mouseClicked(mpos.x.toInt(), mpos.y.toInt(), mouseButton)
        }

        private fun mapMousePos(x: Int, y: Int, clamp: Boolean = false): Vector2f? {
            val realX = x * mc.displayWidth / width
            val realY = y * mc.displayHeight / height
            if (!clamp && (realX < sceneRect.x || realX > sceneRect.x + sceneRect.z))
                return null
            if (!clamp && (realY < sceneRect.y || realY > sceneRect.y + sceneRect.w))
                return null

            val ret = Vector2f(
                cgui.width * MathUtils.clamp01((realX.toFloat() - sceneRect.x) / sceneRect.z),
                cgui.height * MathUtils.clamp01((realY.toFloat() - sceneRect.y) / sceneRect.w)
            )

            if (!conf.sceneConf.simulateMC) {
                ret.x -= conf.sceneConf.offset.x
                ret.y -= conf.sceneConf.offset.y
                ret.x /= conf.sceneConf.scale
                ret.y /= conf.sceneConf.scale
            }

            return ret
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
            if (selectedWidget != null)
                doInspector(selectedWidget!!)
            ImGui.showDemoWindow(true)
            sceneRect = doScene(mx, my)
            // ImGui calls end

            ImGui.render()

//            drawSceneContents(sceneRect)

            GL11.glDisable(GL11.GL_BLEND)
        }

        private fun doScene(mx: Int, my: Int): Vector4f {
            ImGui.begin("Scene")

            val sceneRect = ImGui.getWindowRect()
            sceneRect.y = Display.getHeight() - sceneRect.y - sceneRect.w
            sceneRect.w -= 20

            ImGui.addUserCallback { this.drawSceneContents(sceneRect, mx, my) }

            ImGui.end()

            return sceneRect
        }

        val frame = Frame()

        private fun doMenu() {
            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("File")) {
                    if (ImGui.menuItem("Open")) {
                        val fd = FileDialog(frame, "Open...", FileDialog.LOAD)

                        fd.directory = File(".").absolutePath
                        fd.file = "*.xml"
                        fd.isVisible = true

                        val f = fd.file
                        if (f != null) {
                            cgui.clear()
                            val doc = File(fd.directory + "/" + fd.file).inputStream().use { CGUIDocument.read(it) }
                            for (w in doc) {
                                cgui.addWidget(w.name, w)
                            }
                        }
                    }
                    fun saveAs() {
                        val fd = FileDialog(frame, "Save As...", FileDialog.SAVE)
                        if (targetPath != null) {
                            fd.directory = targetPath!!.parentFile.absolutePath
                            fd.file = targetPath!!.name
                        } else {
                            fd.directory = File("").absolutePath
                            fd.file = "untitled.xml"
                        }
                        fd.isVisible = true

                        if (fd.file != null) {
                            val file = File(fd.directory, fd.file)
                            CGUIDocument.write(cgui, file)
                            targetPath = file
                        }
                    }
                    if (ImGui.menuItem("Save")) {
                        if (targetPath == null)
                            saveAs()
                        else {
                            CGUIDocument.write(cgui, targetPath)
                        }
                    }
                    if (ImGui.menuItem("Save As")) {
                        saveAs()
                    }
                    if (ImGui.menuItem("Exit")) {
                        Minecraft.getMinecraft().displayGuiScreen(null)
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

                ImGui.textColored(Colors.fromRGB32(0xFF8888), if (targetPath == null) "UNNAMED DOCUMENT" else targetPath!!.name)

                ImGui.endMainMenubar()
            }
        }

        override fun onGuiClosed() {
            super.onGuiClosed()
            writeConf(conf)
        }

        private fun doHierarchy() {
            ImGui.begin("Hierarchy", ImGui.WindowFlags_MenuBar)
            if (ImGui.beginMenuBar()) {
                if (ImGui.button("Add")) {
                    val w = Widget()
                    val container = if (selectedWidget == null) cgui else selectedWidget!!
                    container.addWidget(w)

                    selectedWidget = w
                }

                if (selectedWidget != null) {
                    val selected = selectedWidget!!
                    ImGui.text("|")

                    if (ImGui.button("Del")) {
                        selected.dispose()
                        selectedWidget = null
                    }

                    if (ImGui.button("Up")) {
                        val prevIx = selected.abstractParent.indexOf(selected)
                        if (prevIx > 0) {
                            selected.abstractParent.reorder(selected, prevIx - 1)
                        }
                    }

                    if (ImGui.button("Down")) {
                        val prevIx = selected.abstractParent.indexOf(selected)
                        if (prevIx < selected.abstractParent.widgetCount() - 1) {
                            selected.abstractParent.reorder(selected, prevIx + 2)
                        }
                    }

                    if (ImGui.button("Deselect")) {
                        selectedWidget = null
                    }

                    if (reparentingWidget == null && ImGui.button("Reparent")) {
                        reparentingWidget = selectedWidget
                    }
                }

                ImGui.endMenuBar()
            }

            if (reparentingWidget != null) {
                val reparenting = reparentingWidget!!
                ImGui.textColored(Colors.fromRGB32(0xFF8888), "REPARENTING: " + reparenting.fullName)

                val sel = selectedWidget
                val isSelfOrChild = run {
                    var cur: Widget? = sel
                    var ret = false
                    while (cur != null) {
                        if (cur == reparenting) {
                            ret = true
                            break
                        }
                        cur = cur.widgetParent
                    }
                    ret
                }
                if (!isSelfOrChild) {
                    val text = "TO: " + if (sel == null) {
                        "<root>"
                    } else {
                        sel.fullName
                    }
                    ImGui.textColored(Colors.fromRGB32(0xFF8888), text)
                }
                if (ImGui.button("CANCEL")) {
                    reparentingWidget = null
                }
                if (!isSelfOrChild) {
                    ImGui.sameLine()
                    if (ImGui.button("GO")) {
                        var name = reparenting.name
                        val par = reparenting.abstractParent
                        par.forceRemoveWidget(reparenting)

                        val newpar = sel ?: cgui

                        // Find a suitable name for widget
                        if (newpar.hasWidget(name)) {
                            var i = 0
                            while (newpar.hasWidget("$name $i")) {
                                i += 1
                            }
                            name = "$name $i"
                        }

                        // Reparent!
                        newpar.addWidget(name, reparenting)
                        reparentingWidget = null
                    }
                }
                ImGui.separator()
            }

            fun doWidget(w: Widget) {
                val isChild = w.widgetCount() == 0
                val nodeFlags = ImGui.TreeNodeFlags_OpenOnArrow or ImGui.TreeNodeFlags_OpenOnDoubleClick or
                    (if (isChild) ImGui.TreeNodeFlags_Leaf else 0) or
                    (if (selectedWidget == w) ImGui.TreeNodeFlags_Selected else 0)

                val open = ImGui.treeNodeEx(w.name, nodeFlags)
                if (ImGui.isItemClicked())
                    selectedWidget = w
                if (open) {
                    for (child in w) {
                        doWidget(child)
                    }
                    ImGui.treePop()
                }
            }

            for (w in cgui)
                doWidget(w)
            ImGui.end()
        }

        private fun doInspector(target: Widget) {
            ImGui.begin("Inspector")

            ImGui.textColored(Colors.fromRGB32(0x8888FF), "Widget: " + target.fullName)
            var name = target.name
            ImGui.beginChangeCheck()
            name = ImGui.inputText("Name", name)
            if (ImGui.endChangeCheck()) {
                target.rename(name)
            }

            if (ImGui.beginMenu("Add component")) {
                for (comType in CGuiEditor.compponentTypes) {
                    if (ImGui.menuItem(comType.simpleName)) {
                        val instance = comType.getConstructor().newInstance()
                        when (instance) {
                            is Component -> target.addComponent(instance)
                        }
                    }
                }
                ImGui.endMenu()
            }

            var comToRemove: Component? = null

            for (com in target.componentList) {
                ImGui.separator()
                ImGui.pushID(com.name)
                if (com !is Transform) {
                    if (ImGui.button("DELETE")) {
                        comToRemove = com
                    }
                    ImGui.sameLine()
                }
                ImGui.popID()
                ImGui.beginChangeCheck()
                inspection.inspect(com)
                if (ImGui.endChangeCheck() && com is Transform) {
                    target.markDirty()
                }
                inspection.objectInspectCallback = null
            }

            if (comToRemove != null) {
                target.removeComponent(comToRemove)
            }

            ImGui.end()
        }

        private fun drawSceneContents(rect: Vector4f, mx: Int, my: Int) {
            val stored = ImGui.StoredGLState()
            glUseProgram(0)
            glBindVertexArray(0)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
            glBindBuffer(GL_ARRAY_BUFFER, 0)

            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
//            GL11.glLoadIdentity()
            val aspect = rect.z / rect.w

            GL11.glViewport(rect.x.toInt(), rect.y.toInt(), rect.z.toInt(), rect.w.toInt())

            val strechX = rect.z / Display.getWidth()
            val strechY = rect.w / Display.getHeight()

            val virtualScaleFactor: Float = when (conf.sceneConf.simulateMC) {
                true -> Math.max(Math.min((rect.z / 320).toInt(), (rect.w / 240).toInt()), 1).toFloat()
                false -> 1.0f
            }

            val scaledRes = ScaledResolution(Minecraft.getMinecraft())
            val scaleFactor = scaledRes.scaleFactor.toFloat()
//            val virtualScaleFactor = 1
            GL11.glScalef(virtualScaleFactor / strechX / scaleFactor, virtualScaleFactor / strechY / scaleFactor, 1.0f)

            if (!conf.sceneConf.simulateMC) {
                GL11.glTranslatef(conf.sceneConf.offset.x, conf.sceneConf.offset.y, 0f)
                val scl = conf.sceneConf.scale
                GL11.glScalef(scl, scl, 1f)
            }

            if (conf.sceneConf.simulateMC)
                cgui.resize(rect.z / virtualScaleFactor, rect.w / virtualScaleFactor)
            else
                cgui.resize(rect.z, rect.w)

            GL11.glColor4f(1f, 1f, 1f, 1f)
            HudUtils.drawRectOutline(0.0, 0.0, rect.z.toDouble(), rect.w.toDouble(), 5.0f)

            val mappedMousePos = mapMousePos(mx, my, true)!!
            cgui.draw(mappedMousePos.x, mappedMousePos.y)

            // Restore
            GL11.glPopMatrix()

            stored.restore()

            saveScheduler.runFrame(cgui.deltaTime)
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