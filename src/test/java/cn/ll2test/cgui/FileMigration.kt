package cn.ll2test.cgui

import cn.lambdalib2.cgui.loader.CGUIDocument
import java.io.File
import java.io.FileInputStream


fun main(args: Array<String>) {
    File(args[0]).listFiles().filter { it.isFile }.forEach {
        val text = it.readText()
        val replaced = text.replace(Regex("""<[^>]*[cC]olor>[^o]+</[^>]*[cC]olor>""")) {
            println(it.value)
            var s = cvt(it.value, "r", "red")
            s = cvt(s, "g", "green")
            s = cvt(s, "b", "blue")
            s = cvt(s, "a", "alpha")
            s
        }
        it.writeText(replaced)
    }
}

fun cvt(s: String, p: String, np: String): String {
    val regex = Regex("<$p>([0-9.]+)</$p>")
    return s.replace(regex) {
        val newColor = (255 * it.groupValues[1].toFloat()).toInt()
        val replace = "<$np>$newColor</$np>"
//        println("Replace: ${it.value} -> $replace")
        replace
    }
}
