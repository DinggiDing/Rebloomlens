package com.hdil.rebloomlens.sensor_plugins.health_connect

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import kotlinx.coroutines.launch
import org.json.JSONObject

class HealthConnectPlugin(
    override val pluginId: String,
    override val config: JSONObject
) : Plugin {

    private lateinit var healthConnectManager: HealthConnectManager

    override fun initialize(context: Context) {
        val recordTypes = config.optJSONArray("recordTypes") ?: return
        healthConnectManager = HealthConnectManager(context, recordTypes)
    }

    @Composable
    override fun renderUI() {
        val scope = rememberCoroutineScope()
        var permissionGranted by remember { mutableStateOf(false) }

        val requestPermissions = rememberLauncherForActivityResult(
            contract = healthConnectManager.getPermissionContract()
        ) { granted ->
            scope.launch {
                permissionGranted = granted.containsAll(healthConnectManager.permissions)
            }
        }

        LaunchedEffect(Unit) {
            scope.launch {
                healthConnectManager.checkPermissionsAndRun(requestPermissions) {
                    permissionGranted = true
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (permissionGranted) "✅ Health Connect 권한 허용됨" else "❌ Health Connect 권한 필요",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (!permissionGranted) {
                Button(onClick = {
                    scope.launch {
                        healthConnectManager.checkPermissionsAndRun(requestPermissions) {
                            permissionGranted = true
                        }
                    }
                }) {
                    Text("권한 요청 다시 시도")
                }
            }
        }
    }
}