import cn.lambdalib2.vis.editor.ImGui

object TestImGui {

    @JvmStatic
    fun main(args: Array<String>) {
        ImGui.Begin("Hello ImGui", 0)

        ImGui.Text("AAA")
        ImGui.Text("BBB")

        ImGui.End()
    }

}