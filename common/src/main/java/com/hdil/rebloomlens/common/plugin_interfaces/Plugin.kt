package com.hdil.rebloomlens.common.plugin_interfaces

import android.content.Context
import androidx.compose.runtime.Composable
import org.json.JSONObject

interface Plugin {
    val pluginId: String
    val config: JSONObject

    fun initialize(context: Context)
    @Composable
    fun renderUI()
}