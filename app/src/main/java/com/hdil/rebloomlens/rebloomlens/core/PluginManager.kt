package com.hdil.rebloomlens.rebloomlens.core

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.rebloomlens.R
import org.json.JSONObject


//ROLE  dynamically load plugin (init, register, load UI)
/*
 * PluginManager is responsible for loading and managing plugins in the application.
 * It initializes plugins based on a configuration file and provides a UI for interacting with them.
 */

object PluginManager {
    private val plugins = mutableMapOf<String, Plugin>()
    private val categories = mutableMapOf<String, List<PluginInstance>>()

    data class PluginInstance(
        val pluginId: String,
        val title: String,
        val plugin: Plugin
    )

    fun initialize(context: Context) {
        val configJson = ConfigLoader.load(context, "plugin_registry.json")
        plugins.clear()
        categories.clear()

        try {
            // 1. 먼저 모든 플러그인 타입 로드
            val pluginArray = configJson.getJSONArray("plugins")
            val pluginConfigMap = mutableMapOf<String, JSONObject>()

            for (i in 0 until pluginArray.length()) {
                val pluginConfig = pluginArray.getJSONObject(i)
                val pluginId = pluginConfig.getString("plugin_id")
                pluginConfigMap[pluginId] = pluginConfig

                // health_connect와 samsung_health 플러그인은 바로 생성
                if (pluginId == "health_connect" || pluginId == "samsung_health") {
                    val uniquePluginId = "${pluginId}_$i"
                    val plugin = createPlugin(pluginId, pluginConfig)
                    plugin?.let {
                        it.initialize(context)
                        plugins[uniquePluginId] = it
                        Log.d("PluginManager", "Loaded health plugin: $uniquePluginId")
                    }
                }
            }

            // 2. assignment 카테고리별로 플러그인 인스턴스 생성
            val assignmentsArray = configJson.getJSONArray("assignments")
            for (i in 0 until assignmentsArray.length()) {
                val assignment = assignmentsArray.getJSONObject(i)
                val category = assignment.getString("category")
                val categoryPlugins = mutableListOf<PluginInstance>()

                val pluginsArray = assignment.getJSONArray("plugins")
                for (j in 0 until pluginsArray.length()) {
                    val pluginData = pluginsArray.getJSONObject(j)
                    val pluginId = pluginData.getString("plugin_id")

                    // health 플러그인이 아닌 경우만 처리
                    if (pluginId != "health_connect" && pluginId != "samsung_health") {
                        val instances = pluginData.getJSONArray("instances")
                        for (k in 0 until instances.length()) {
                            val instance = instances.getJSONObject(k)
                            val title = instance.getString("title")
                            val uniquePluginId = "${pluginId}_${category}_$k"

                            // 해당 플러그인의 설정정보 가져오기
                            val baseConfig = pluginConfigMap[pluginId] ?: JSONObject()

                            // 인스턴스 정보 추가 (카테고리, 타이틀 정보 추가)
                            val instanceConfig = JSONObject(baseConfig.toString())
                            instanceConfig.put("category", category)
                            instanceConfig.put("title", title)

                            // 넘버패드용
                            if (instance.has("inputType")) {
                                instanceConfig.put("inputType", instance.getString("inputType"))
                            }
                            if (instance.has("min")) {
                                instanceConfig.put("min", instance.getInt("min"))
                            }
                            if (instance.has("max")) {
                                instanceConfig.put("max", instance.getInt("max"))
                            }
                            if (instance.has("mode")) {
                                instanceConfig.put("mode", instance.getString("mode"))
                            }

                            // likert_scale 플러그인에만 scale 파라미터 추가
                            if (pluginId == "likert_scale" && instance.has("scale")) {
                                try {
                                    val scaleArray = instance.getJSONArray("scale")
                                    instanceConfig.put("scale", scaleArray)
                                } catch (e: Exception) {
                                    Log.e("PluginManager", "Error parsing scale for likert_scale: ${e.message}")
                                }
                            }

                            val plugin = createPlugin(pluginId, instanceConfig)
                            plugin?.let {
                                it.initialize(context)
                                plugins[uniquePluginId] = it
                                categoryPlugins.add(PluginInstance(uniquePluginId, title, it))
                                Log.d("PluginManager", "Loaded category plugin: $uniquePluginId, title: $title")
                            }
                        }
                    }
                }

                categories[category] = categoryPlugins
            }
        } catch (e: Exception) {
            Log.e("PluginManager", "Error initializing plugins: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createPlugin(pluginId: String, config: JSONObject): Plugin? {
        return when (pluginId) {
            "likert_scale" -> com.hdil.rebloomlens.manualInput_plugins.likert_scale.LikertScalePlugin(pluginId, config)
            "text_input" -> com.hdil.rebloomlens.manualInput_plugins.text_input.TextInputPlugin(pluginId, config)
            "health_connect" -> com.hdil.rebloomlens.sensor_plugins.health_connect.HealthConnectPlugin(pluginId, config)
            "samsung_health" -> com.hdil.rebloomlens.samsunghealth_data.SamsungHealthPlugin(pluginId, config)
            "voice_input" -> com.hdil.voice_input.VoiceInputPlugin(pluginId, config)
            else -> null
        }
    }

    @Composable
    fun loadPluginsUI(navController: NavController) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. 헬스 플러그인 섹션
            item {
                Text(
                    text = "헬스 데이터",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            val healthPlugins = plugins.filter {
                it.key.startsWith("health_connect") || it.key.startsWith("samsung_health")
            }

            // 수정: 올바른 items 함수 사용법으로 변경
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    healthPlugins.forEach { (pluginId, _) ->
                        PluginItem(
                            pluginId = pluginId,
                            onClick = { navController.navigate("pluginDetail/$pluginId") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. 카테고리별 플러그인 섹션
            categories.forEach { (category, categoryPlugins) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoryPlugins.forEach { pluginInstance ->
                            CategoryPluginItem(
                                pluginId = pluginInstance.pluginId,
                                title = pluginInstance.title,
                                onClick = { navController.navigate("pluginDetail/${pluginInstance.pluginId}") }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PluginItem(pluginId: String, onClick: () -> Unit) {
        val displayName = when {
            pluginId.startsWith("health_connect") -> "Health Connect"
            pluginId.startsWith("samsung_health") -> "Samsung Health"
            else -> pluginId.substringBefore("_").replace("_", " ").capitalize()
        }


        val recordTypesText = plugins[pluginId]?.config?.let { config ->
            if (config.has("recordTypes")) {
                try {
                    val recordTypes = config.getJSONArray("recordTypes")
                    val recordTypeList = mutableListOf<String>()

                    val count = minOf(recordTypes.length(), 3)
                    for (i in 0 until count) {
                        recordTypeList.add(recordTypes.getString(i))
                    }

                    if (recordTypes.length() > 3) {
                        recordTypeList.add("...")
                    }

                    recordTypeList.joinToString(", ")
                } catch (e: Exception) {
                    "no data"
                }
            } else {
                "no data"
            }
        } ?: "no data"

        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                ) {
                    val iconRes = if (pluginId.startsWith("health_connect"))
                        R.drawable.healthconnect_logo
                    else
                        R.drawable.samsunghealth_logo

                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = displayName,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2) 제목 + 설명
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recordTypesText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    @Composable
    private fun CategoryPluginItem(pluginId: String, title: String, onClick: () -> Unit) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )

                val description = when {
                    pluginId.contains("likert_scale") -> {
                        val plugin = plugins[pluginId]
                        val config = plugin?.config
                        if (config?.has("scale") == true) {
                            try {
                                val scaleArray = config.getJSONArray("scale")
                                "리커트 척도 (${scaleArray.length()}점)"
                            } catch (e: Exception) {
                                "리커트 척도"
                            }
                        } else {
                            "리커트 척도"
                        }
                    }
                    pluginId.contains("text_input") -> "텍스트 입력"
                    pluginId.contains("voice_input") -> "음성 입력"
                    else -> "기타"
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
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