import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object Main {

    data class ImportConfig(
        val srcDir: String,
        val resourcesDir: String,
        val itemsDataDir: String,
        val domain: String,
        val itemsClassPath: String
    ) {
        fun getItemsPackageName() = itemsClassPath.substringBeforeLast('.')
        fun getItemsClassName() = itemsClassPath.substringAfterLast('.')
    }

    data class ItemMetadata(val id: String, val baseClass: String, val ctorArgs: String)

    val gson = Gson()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    val velocityEngine = VelocityEngine()

    init {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        velocityEngine.init()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val rootDir = if (args.isEmpty()) "" else args.first()
        println("Root dir: ${File(rootDir).absolutePath}")

        val configFile = File(rootDir, "xconf.json")
        if (!configFile.isFile) {
            println("Can't find xconf.json in current directory, quitting...")
            return
        }

        val config = gson.fromJson<ImportConfig>(configFile.readText())
        println("Your config is: $config")

        val items = File(rootDir, config.itemsDataDir).listFiles().map {
            val id = it.nameWithoutExtension
            val json = gson.fromJson<JsonObject>(it.readText())
            ItemMetadata(id, json["baseClass"].asString, json["ctorArgs"].asString ?: "")
        }

        println("\nItem list: \n${items.joinToString(separator = "\n")}\n")

        val srcDir = File(rootDir, config.srcDir)

        println("Writing item class...")
        run {
            val itemClassFile = File(srcDir, config.itemsClassPath.replace('.', '/') + ".java")
            itemClassFile.parentFile.mkdirs()

            val s = renderTemplate("itemClassTemplate.java", mapOf(
                "date" to dateFormat.format(Date()),
                "config" to config,
                "items" to items
            ))
            itemClassFile.writeText(s)
        }
    }

    private fun renderTemplate(templateSource: String, context: Map<String, Any>): String {
        val ctx = VelocityContext(context)
        val template = velocityEngine.getTemplate(templateSource)
        return StringWriter().use {
            template.merge(ctx, it)
            it.toString()
        }
    }
}

