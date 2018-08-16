import com.typesafe.config.Config

fun Config.getIntOrNull(path: String): Int? =
    if (!this.hasPath(path)) null else this.getInt(path)

fun Config.getStringOrNull(path: String): String? =
    if (!this.hasPath(path)) null else this.getString(path)

fun Config.getStringOrDefault(path: String, default: String) =
    getStringOrNull(path) ?: default