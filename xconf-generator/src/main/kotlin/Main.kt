import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.typesafe.config.*
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object Main {
    data class AdditionalImports(
        val all: Array<String>,
        val item: Array<String>,
        val block: Array<String>
    )

    data class ImportConfig(
        val srcDir: String,
        val resourcesDir: String,
        val locPrefix: String,
        val domain: String,

        val itemsDataFile: String,
        val itemsClassPath: String,

        val blocksDataFile: String,
        val blocksClassPath: String,

        val additionalImports: AdditionalImports
    ) {
        fun getItemsPackageName() = itemsClassPath.substringBeforeLast('.')
        fun getItemsClassName() = itemsClassPath.substringAfterLast('.')

        fun getBlocksPackageName() = blocksClassPath.substringBeforeLast('.')
        fun getBlocksClassName() = blocksClassPath.substringAfterLast('.')
    }

    data class ItemMetadata(
        val id: String,
        val baseClass: String,
        val ctorArgs: String,
        val maxStackSize: Int?,
        val maxDamage: Int?,
        val creativeTab: String?,
        val init: Array<String>?,

        val generateModel: Boolean,
        val model: Config?,
        val extModels: Config?,
        val modelBindings: Map<Int, String>
    )

    data class BlockItemMetadata(
        val model: Config?
    )

    data class BlockMetadata(
        val id: String,
        val baseClass: String,
        val ctorArgs: String,
        val creativeTab: String?,
        val init: Array<String>?,
        val itemBlock: BlockItemMetadata,

        val generateModel: Boolean,
        val model: Config?,
        val extModels: Config?,
        val blockStates: Config?
    )

    val gson = Gson()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    val velocityEngine = VelocityEngine()

    val jsonMark = "__xconfgen" to true

    init {
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        velocityEngine.init()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("LambdaLib2.xconf by WeAthFolD, version 0.1")
        println()

        val rootDir = if (args.isEmpty()) "" else args.first()
        println("Root dir: ${File(rootDir).absolutePath}")

        val configFile = File(rootDir, "xconf.json")
        if (!configFile.isFile) {
            println("Can't find xconf.json in current directory, quitting...")
            return
        }

        val config = gson.fromJson<ImportConfig>(configFile.readText())
        println("config is: $config")


        val srcDir = File(rootDir, config.srcDir)
        val resDir = File(rootDir, config.resourcesDir)
        val assetsRootDir = File(resDir, "assets/${config.domain}")

        println()
        println("Cleaning up...")
        arrayOf(
            File(assetsRootDir, "models/item"),
            File(assetsRootDir, "models/block"),
            File(assetsRootDir, "blockstates")
        )
        .filter { it.isDirectory }
        .flatMap { it.listFiles().toList() }
        .filter {
            val res: JsonObject = gson.fromJson(it.readText())
            res.has(jsonMark.first)
        }
        .forEach {
            println(it.path)
            it.delete()
        }

        val rawItems = ConfigFactory.parseFile(File(rootDir, config.itemsDataFile))
        val itemBase = rawItems.getValue("_base")
        val items = rawItems
            .root()
            .filter { !it.key.startsWith("_") }
            .map { (id, elem) ->
                val obj = (elem as ConfigObject).toConfig().withFallback(itemBase)
                ItemMetadata(
                    id = id,
                    baseClass = obj.getStringOrDefault("baseClass", "net.minecraft.item.Item"),
                    ctorArgs = obj.getStringOrDefault("ctorArgs", ""),
                    maxStackSize = obj.getIntOrNull("maxStackSize"),
                    maxDamage = obj.getIntOrNull("maxDamage"),
                    creativeTab = obj.getStringOrNull("creativeTab"),
                    init = obj.getStrArrOrNull("init"),

                    generateModel = obj.getBooleanOrDefault("generateModel", true),
                    model = obj.getConfigOrNull("model"),
                    extModels = obj.getConfigOrNull("extModels"),
                    modelBindings = obj.getConfigOrNull("modelBindings")?.root()
                        ?.map { it.key.toInt() to it.value.unwrapped() as String }?.toMap() ?:
                        mapOf(0 to "${config.domain}:$id")
                )
            }
        println("\nItem list: \n${items.joinToString(separator = "\n")}")
        println()

        val rawBlocks = ConfigFactory.parseFile(File(rootDir, config.blocksDataFile))
        val blockBase = rawBlocks.getValue("_base")
        val blocks = rawBlocks
            .root()
            .filter { !it.key.startsWith("_") }
            .map { (id, elem) ->
                val obj = (elem as ConfigObject).toConfig().withFallback(blockBase)
                val item = obj.getConfigOrNull("item") ?: ConfigFactory.empty()
                BlockMetadata(
                    id,
                    obj.getStringOrDefault("baseClass", "net.minecraft.block.Block"),
                    obj.getStringOrDefault("ctorArgs", ""),
                    obj.getStringOrNull("creativeTab"),
                    obj.getStrArrOrNull("init"),
                    BlockItemMetadata(item.getConfigOrNull("model")),

                    generateModel = obj.getBooleanOrDefault("generateModel", true),
                    model = obj.getConfigOrNull("model"),
                    extModels = obj.getConfigOrNull("extModels"),
                    blockStates = obj.getConfigOrNull("blockStates")
                )
            }

        val blocksWithItemBlock = blocks
        println("Block list: \n${blocks.joinToString(separator = "\n")}")
        println()

        println("Writing item models...")
        for (item in items) if (item.generateModel) {
            val modelJsonPath = File(assetsRootDir, "models/item/${item.id}.json")
            val modelJsonStr = if (item.model != null) {
                val obj: JsonObject = gson.fromJson(item.model.root().render(ConfigRenderOptions.concise()))
                obj.put(jsonMark)
                obj
            } else {
                jsonObject(
                    jsonMark,
                    "parent" to "item/generated",
                    "textures" to jsonObject("layer0" to "${config.domain}:items/${item.id}")
                )
            }.toString()

            modelJsonPath.parentFile.mkdirs()
            modelJsonPath.writeText(modelJsonStr)
        }

        println("Writing item ext models...")
        for (item in items) {
            if (item.extModels != null) {
                for (m in item.extModels.root()) {
                    val path = File(assetsRootDir, "models/item/${m.key}.json")
                    val conf = (m.value as ConfigObject).withValue(jsonMark.first, ConfigValueFactory.fromAnyRef(jsonMark.second))
                    path.writeText(conf.render(ConfigRenderOptions.concise()))
                }
            }
        }

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


        println("Writing block models...")
        for (block in blocks) if (block.generateModel) {
            val modelJsonPath = File(assetsRootDir, "models/block/${block.id}.json")
            val modelJson = when {
                block.model != null -> {
                    block.model.toJsonObject()
                }
                else -> {
                    jsonObject(
                        "parent" to "block/cube_all",
                        "textures" to jsonObject(
                            "all" to "${config.domain}:blocks/${block.id}"
                        )
                    )
                }
            }
            modelJson.put(jsonMark)
            modelJsonPath.parentFile.mkdirs()
            modelJsonPath.writeText(modelJson.toString())
        }

        println("Writing block item models...")
        for (block in blocksWithItemBlock) {
            val modelJsonPath = File(assetsRootDir, "models/item/${block.id}.json")
            val modelJsonStr = if (block.itemBlock.model != null) {
                val obj: JsonObject = gson.fromJson(block.itemBlock.model.root().render(ConfigRenderOptions.concise()))
                obj[jsonMark.first] = jsonMark.second
                obj.toString()
            } else {
                jsonObject(
                    jsonMark,
                    "parent" to "${config.domain}:block/${block.id}"
                ).toString()
            }
            modelJsonPath.parentFile.mkdirs()
            modelJsonPath.writeText(modelJsonStr)
        }

        println("Writing blockstates...")
        for (block in blocks) {
            val jsonPath = File(assetsRootDir, "blockstates/${block.id}.json")
            val json  = when {
                block.blockStates != null -> block.blockStates.toJsonObject()
                else -> jsonObject(
                    "variants" to jsonObject(
                        "normal" to jsonObject(
                            "model" to "${config.domain}:${block.id}"
                        )
                    )
                )
            }

            json += jsonMark
            jsonPath.parentFile.mkdirs()
            jsonPath.writeText(json.toString())
        }

        println("Writing block ext models...")
        for (block in blocks) {
            if (block.extModels != null) {
                for (m in block.extModels.root()) {
                    val path = File(assetsRootDir, "models/block/${m.key}.json")
                    val conf = (m.value as ConfigObject).withValue(jsonMark.first, ConfigValueFactory.fromAnyRef(jsonMark.second))
                    path.writeText(conf.render(ConfigRenderOptions.concise()))
                }
            }
        }

        println("Writing blocks class...")
        run {
            val blocksClassFile = File(srcDir, config.blocksClassPath.replace('.', '/') + ".java")
            blocksClassFile.parentFile.mkdirs()

            val s = renderTemplate("blockClassTemplate.java", mapOf(
                "date" to dateFormat.format(Date()),
                "config" to config,
                "blocks" to blocks,
                "blocksWithItemBlock" to blocksWithItemBlock
            ))
            blocksClassFile.writeText(s)
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

