package com.hdil.rebloomlens.rebloomlens.core

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import org.json.JSONObject

//ROLE  dynamically load plugin (init, register, load UI)

object PluginManager {
    private val plugins = mutableMapOf<String, Plugin>()

    fun initialize(context: Context) {
        val configJson = ConfigLoader.load(context, "plugin_registry.json")
        plugins.clear()

        try {
            val pluginArray = configJson.getJSONArray("plugins")
            for (i in 0 until pluginArray.length()) {
                val pluginConfig = pluginArray.getJSONObject(i)
                val pluginId = pluginConfig.getString("plugin_id")

                val plugin = createPlugin(pluginId, pluginConfig)
                plugin?.let {
                    it.initialize(context)
                    plugins[pluginId] = it
                    Log.d("PluginManager", "Loaded plugin : $pluginId")
                }
            }
        } catch (e: Exception) {
            Log.e("PluginManger", "Error initializing plugins : ${e.message}")
        }
    }

    private fun createPlugin(pluginId: String, config: JSONObject): Plugin? {
        return when (pluginId) {
            "likert_scale" -> com.hdil.rebloomlens.likert_scale.LikertScalePlugin(pluginId, config)
            else -> null
        }
    }

    @Composable
    fun loadPluginsUI() {
        plugins.values.forEach { plugin ->
            plugin.renderUI()
        }
    }
}