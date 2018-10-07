import com.typesafe.config.Config

fun Config.getConfigOrNull(path: String): Config? =
    if (!this.hasPath(path)) null else this.getConfig(path)

fun Config.getBooleanOrDefault(path: String, default: Boolean): Boolean =
    if (!this.hasPath(path)) default else this.getBoolean(path)

fun Config.getIntOrNull(path: String): Int? =
    if (!this.hasPath(path)) null else this.getInt(path)

fun Config.getStringOrNull(path: String): String? =
    if (!this.hasPath(path)) null else this.getString(path)

fun Config.getStringOrDefault(path: String, default: String) =
    getStringOrNull(path) ?: default

fun Config.getStrArrOrNull(path: String) =
    if (!this.hasPath(path)) null else this.getStringList(path).toTypedArray()

