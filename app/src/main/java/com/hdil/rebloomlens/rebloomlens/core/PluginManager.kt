package com.hdil.rebloomlens.rebloomlens.core

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.rebloomlens.ui.theme.onPrimaryLight
import com.hdil.rebloomlens.rebloomlens.ui.theme.onSurfaceLight
import com.hdil.rebloomlens.rebloomlens.ui.theme.onSurfaceVariantLight
import com.hdil.rebloomlens.rebloomlens.ui.theme.primaryDark
import com.hdil.rebloomlens.rebloomlens.ui.theme.primaryLight
import com.hdil.rebloomlens.rebloomlens.ui.theme.surfaceVariantLight
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
            "likert_scale" -> com.hdil.rebloomlens.manualInput_plugins.likert_scale.LikertScalePlugin(pluginId, config)
            "text_input" -> com.hdil.rebloomlens.manualInput_plugins.text_input.TextInputPlugin(pluginId, config)
            else -> null
        }
    }

    @Composable
    fun loadPluginsUI(navController: NavController) {
//        plugins.values.forEach { plugin ->
//            plugin.renderUI()
//        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            plugins.forEach { (pluginId, plugin) ->
                Button(
                    onClick = {
                    // Navigate to detailed view of the plugin
                        navController.navigate("pluginDetail/$pluginId")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryLight,
                        contentColor = onPrimaryLight,
                        disabledContainerColor = surfaceVariantLight,
                        disabledContentColor = onSurfaceVariantLight,
                    ),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.wrapContentSize()
                        .padding(8.dp),
                ) {
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