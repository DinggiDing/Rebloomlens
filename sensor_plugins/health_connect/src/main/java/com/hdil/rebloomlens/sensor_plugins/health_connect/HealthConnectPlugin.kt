package com.hdil.rebloomlens.sensor_plugins.health_connect

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hdil.rebloomlens.common.model.BloodGlucoseData
import com.hdil.rebloomlens.common.model.BloodPressureData
import com.hdil.rebloomlens.common.model.BodyFatData
import com.hdil.rebloomlens.common.model.ExerciseData
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.model.WeightData
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.DateTimeUtils
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
        val lastSyncTime by viewModel.lastSyncTime.collectAsState()

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
                viewModel.loadWeightData()
                viewModel.loadBloodGlucoseData()
                viewModel.loadBloodPressureData()
                viewModel.loadBodyFatData()
                viewModel.loadHeartRateData()
                viewModel.loadExerciseData()
                viewModel.updateLastSyncTime()
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                        // Overview 섹션
                        Text(
                            text = "개요",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = "마지막 동기화: ${lastSyncTime?.let { DateTimeUtils.formatDateTime(it) } ?: "없음"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HealthDataOverview(
                            sleepSessions = uiState.sleepSessions,
                            steps = uiState.steps,
                            weights = uiState.weight,
                            bloodGlucose = uiState.bloodGlucose,
                            bloodPressure = uiState.bloodPressure,
                            bodyFat = uiState.bodyFat,
                            heartRate = uiState.heartRate,
                            exercise = uiState.exercise
                        )

                        Spacer(modifier = Modifier.height(32.dp))

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

@Composable
fun HealthDataOverview(
    sleepSessions: List<SleepSessionData>,
    steps: List<StepData>,
    weights: List<WeightData>,
    bloodGlucose: List<BloodGlucoseData>,
    bloodPressure: List<BloodPressureData>,
    bodyFat: List<BodyFatData>,
    heartRate: List<HeartRateData>,
    exercise: List<ExerciseData>,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier
            .height(450.dp)
    ) {
        item {
            OverviewCard(
                title = "수면",
                value = "${sleepSessions.size}건",
                description = if (sleepSessions.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(sleepSessions.first().startTime)}"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "걸음 수",
                value = "${steps.sumOf { it.stepCount }}걸음",
                description = if (steps.isNotEmpty()) {
                    "오늘: ${steps.first().stepCount}걸음"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "체중",
                value = if (weights.isNotEmpty()) {
                    String.format("%.1f kg", weights.first().weight.inKilograms)
                } else "기록 없음",
                description = if (weights.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(weights.first().time)}"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "혈당",
                value = if (bloodGlucose.isNotEmpty()) {
                    "${bloodGlucose.first().level} mg/dL"
                } else "기록 없음",
                description = if (bloodGlucose.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(bloodGlucose.first().time)}"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "혈압",
                value = if (bloodPressure.isNotEmpty()) {
                    "${bloodPressure.first().systolic}/${bloodPressure.first().diastolic} mmHg"
                } else "기록 없음",
                description = if (bloodPressure.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(bloodPressure.first().time)}"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "체지방",
                value = if (bodyFat.isNotEmpty()) {
                    String.format("%.1f%%", bodyFat.first().bodyFatPercentage)
                } else "기록 없음",
                description = if (bodyFat.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(bodyFat.first().time)}"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "심박수",
                value = if (heartRate.isNotEmpty()) {
                    "${heartRate.first().samples.first().beatsPerMinute} BPM"
                } else "기록 없음",
                description = if (heartRate.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(heartRate.first().startTime)}"
                } else "기록 없음"
            )
        }

        item {
            OverviewCard(
                title = "운동",
                value = if (exercise.isNotEmpty()) {
                    "${exercise.last().exerciseType}"
                } else "기록 없음",
                description = if (exercise.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(exercise.first().startTime)}"
                } else "기록 없음"
            )
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        letterSpacing = 0.sp
                    )
                )
            }
        }
    }
}