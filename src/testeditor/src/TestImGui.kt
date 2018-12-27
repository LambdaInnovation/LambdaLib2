import cn.lambdalib2.vis.editor.ImGui

object TestImGui {

    @JvmStatic
    fun main(args: Array<String>) {
        ImGui.begin("Hello ImGui", 0)

        ImGui.text("AAA")
        ImGui.text("BBB")

        ImGui.end()
    }

}