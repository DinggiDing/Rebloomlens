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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import com.hdil.rebloomlens.sensor_plugins.health_connect.sleep.SleepSessionsList
import com.hdil.rebloomlens.sensor_plugins.health_connect.step.StepsList
import kotlinx.coroutines.launch
import org.json.JSONObject

class HealthConnectPlugin(
    override val pluginId: String,
    override val config: JSONObject
) : Plugin {

    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var viewModelFactory: HealthConnectViewModelFactory


    override fun initialize(context: Context) {
        val recordTypes = config.optJSONArray("recordTypes") ?: return
        healthConnectManager = HealthConnectManager(context, recordTypes)
        viewModelFactory = HealthConnectViewModelFactory(healthConnectManager)
    }

    @Composable
    override fun renderUI() {

        // Check if Health Connect is installed
        val scope = rememberCoroutineScope()
        var permissionGranted by remember { mutableStateOf(false) }

        val requestPermissions = rememberLauncherForActivityResult(
            contract = healthConnectManager.getPermissionContract()
        ) { granted ->
            scope.launch {
                permissionGranted = granted.containsAll(healthConnectManager.permissions)
            }
        }

        // ViewModel
        val viewModel: HealthConnectViewModel = viewModel(factory = viewModelFactory)
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            scope.launch {
                healthConnectManager.checkPermissionsAndRun(requestPermissions) {
                    permissionGranted = true
                }
            }
        }
        LaunchedEffect(permissionGranted) {
            if (permissionGranted) {
                viewModel.loadSleepData()
                viewModel.loadStepData()
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
            } else {
                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.error != null -> ErrorScreen(message = uiState.error)
                    else -> {
                        Column {
                            // 수면 데이터 섹션
                            Text(
                                text = "수면 기록",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            SleepSessionsList(sessions = uiState.sleepSessions)

                            Spacer(modifier = Modifier.height(24.dp))

                            // 걸음 수 데이터 섹션
                            Text(
                                text = "걸음 수 기록",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            StepsList(steps = uiState.steps)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Text(text = "데이터를 불러오는 중...")
}

@Composable
private fun ErrorScreen(message: String?) {
    Text(
        text = message ?: "알 수 없는 오류가 발생했습니다",
        color = MaterialTheme.colorScheme.error
    )
}