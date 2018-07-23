import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import java.io.File

data class ImportConfig(
    val srcDir: String,
    val resourcesDir: String,
    val itemsDataDir: String,
    val domain: String,
    val itemClassPath: String
)

val gson = Gson()

fun main(args: Array<String>) {
    val configFile = File("xconf.json")
    if (!configFile.isFile) {
        println("Can't find xconf.json in current directory, quitting...")
        return
    }

    val config = gson.fromJson<ImportConfig>(configFile.readText())
    println("Your config is: $config")
}