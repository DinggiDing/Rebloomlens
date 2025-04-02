package com.hdil.rebloomlens.rebloomlens.core

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                val pluginId = pluginConfig.getString("plugin_id") + "_" + i  // Append index to make key unique

                val plugin = createPlugin(pluginConfig.getString("plugin_id"), pluginConfig)
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
            "text_input" -> com.hdil.rebloomlens.manualInput_plugins.text_input.TextInputPlugin(pluginId, config)
            else -> null
        }
    }

    @Composable
    fun loadPluginsUI(navController: NavController) {
//        plugins.values.forEach { plugin ->
//            plugin.renderUI()
//        }
        Column {
            plugins.forEach { (pluginId, plugin) ->
                Button(onClick = {
                    // Navigate to detailed view of the plugin
//                    navigateToPluginDetail(pluginId)
                    navController.navigate("pluginDetail/$pluginId")
                }) {
                    Text(text = "Open ${pluginId.replace("_", " ").capitalize()} Plugin")
                }
            }
        }
    }

    @Composable
    fun navigateToPluginDetail(pluginId: String) {
        plugins[pluginId]?.renderUI() ?: Text("Plugin not found")
    }

    @Composable
    fun PluginNavigation() {
        val navController = rememberNavController()
        NavHost(navController, startDestination = "pluginList") {
            composable("pluginList") {
                loadPluginsUI(navController)
            }
            composable("pluginDetail/{pluginId}") { backStackEntry ->
                val pluginId = backStackEntry.arguments?.getString("pluginId")
                if (pluginId != null) {
                    navigateToPluginDetail(pluginId)
                } else {
                    Text("Invalid Plugin")
                }
            }
        }
    }
}