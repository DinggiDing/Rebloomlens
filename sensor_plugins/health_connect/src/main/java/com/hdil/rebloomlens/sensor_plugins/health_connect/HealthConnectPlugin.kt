package com.hdil.rebloomlens.sensor_plugins.health_connect

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.utils.Logger
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.ZonedDateTime

class HealthConnectPlugin(
    override val pluginId: String,
    override val config: JSONObject
) : Plugin {

    private lateinit var healthConnectClient: HealthConnectClient

    override fun initialize(context: Context) {
        healthConnectClient = HealthConnectClient.getOrCreate(context)
        Logger.i("[$pluginId] Initialized with config: ${config.toString()}")
    }

    @Composable
    override fun renderUI() {
        val title = config.optString("title", "Health Data Sync")
        val description = config.optString("description", "Sync and view health data")

        var stepsCount by remember { mutableStateOf("N/A") }
        val coroutineScope = rememberCoroutineScope()

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            Logger.i("[$pluginId] Permission granted or denied")
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description)

            Button(
                onClick = {
                    coroutineScope.launch {
                        checkAndRequestPermissions(permissionLauncher)
                        stepsCount = getStepCount()
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Start sync")
            }

            Text(text = "Steps: $stepsCount", modifier = Modifier.padding(16.dp))
        }
    }

    private suspend fun checkAndRequestPermissions(permissionLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val permissions = setOf(
            PermissionController.PERMISSIONS_READ,
            PermissionController.PERMISSIONS_WRITE
        )
        val granted = healthConnectClient.permissionController.getGrantedPermissions(permissions)

        if (!permissions.all { it in granted }) {
            val intent = healthConnectClient.permissionController.createRequestPermissions(permissions)
            permissionLauncher.launch(intent)
        }
    }

    private suspend fun getStepCount(): String {
        val endTime = ZonedDateTime.now().toInstant()
        val startTime = endTime.minusSeconds(60*60*24)

        return try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val steps = response[StepsRecord.COUNT_TOTAL]?.toString() ?: "0"
            Logger.i("[$pluginId] Total steps: $steps")
            steps
        } catch (e: Exception) {
            Logger.e("[$pluginId] Failed to read steps: ${e.message}")
            "Error"
        }
    }

}