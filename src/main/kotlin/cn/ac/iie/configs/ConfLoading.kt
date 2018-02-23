package cn.ac.iie.configs

import com.google.gson.GsonBuilder
import java.io.File

object ConfLoading {
    var config: ConfigAll? = null

    fun init(path: String): ConfigAll {
        val gson = GsonBuilder().disableHtmlEscaping().create()
        val file = File(path)
        config = gson.fromJson(file.readText(Charsets.UTF_8), ConfigAll::class.java)
        return config ?: ConfigAll()
    }
}

data class ConfigAll(
        var mainClientUrl: String = "",
        var newClientUrl: String = "",
        val ipArray: List<String> = arrayListOf(),
        val groupName: String = "",
        val address: String = "",
        val topic: String = ""
)

fun config(init: Boolean = true) = if (init) ConfLoading.config ?: throw RuntimeException("config no init") else ConfigAll()