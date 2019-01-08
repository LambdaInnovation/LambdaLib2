package cn.lambdalib2.vis.editor.cgui

import cn.lambdalib2.cgui.CGuiScreen
import cn.lambdalib2.registry.StateEventCallback
import cn.lambdalib2.vis.editor.ImGui
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

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

    class Editor : CGuiScreen() {
        val inputBuffer = ArrayList<Char>()
        var dWheel = 0.0f

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

            ImGui.render()
        }


    }

}