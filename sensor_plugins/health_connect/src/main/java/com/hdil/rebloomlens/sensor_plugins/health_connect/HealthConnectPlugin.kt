package com.hdil.rebloomlens.sensor_plugins.health_connect

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

        // 간단한 상태 관리로 내부 화면 전환 구현 (NavController 대신)
        var currentScreen by remember { mutableStateOf("main") }

        when (currentScreen) {
            "main" -> MainContent(
                viewModelFactory = viewModelFactory,
                healthConnectManager = healthConnectManager,
                onNavigateToStepsList = { currentScreen = "steps_list" }
            )
            "steps_list" -> {
                val viewModel: HealthConnectViewModel = viewModel(factory = viewModelFactory)
                val uiState by viewModel.uiState.collectAsState()
                StepsList(steps = uiState.steps)
            }
        }
    }
}

@Composable
private fun MainContent(
    viewModelFactory: HealthConnectViewModelFactory,
    healthConnectManager: HealthConnectManager,
    onNavigateToStepsList: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var permissionGranted by remember { mutableStateOf(false) }

    val requestPermissions = rememberLauncherForActivityResult(
        contract = healthConnectManager.getPermissionContract()
    ) { granted ->
        scope.launch {
            permissionGranted = granted.containsAll(healthConnectManager.permissions)
        }
    }

    val viewModel: HealthConnectViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    // 권한 체크 및 데이터 로딩 로직
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Health Connect",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (permissionGranted) "✅ Health Connect 권한 허용됨" else "❌ Health Connect 권한 필요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!permissionGranted) {
                PermissionRequestCard(
                    onRequestClick = {
                        scope.launch {
                            healthConnectManager.checkPermissionsAndRun(requestPermissions) {
                                permissionGranted = true
                            }
                        }
                    }
                )
            } else {
                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.error != null -> ErrorScreen(message = uiState.error)
                    else -> {
                        // 마지막 동기화 정보
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "건강 데이터 요약",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            AssistChip(
                                onClick = { },
                                shape = RoundedCornerShape(8.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                label = {
                                    Text(
                                        text = "마지막 동기화: ${lastSyncTime?.let { DateTimeUtils.formatDateTime(it) } ?: "없음"}",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // onNavigateToStepsList 전달
                        ModernHealthDataOverview(
                            onNavigateToStepsList = onNavigateToStepsList,
                            sleepSessions = uiState.sleepSessions,
                            steps = uiState.steps,
                            weights = uiState.weight,
                            bloodGlucose = uiState.bloodGlucose,
                            bloodPressure = uiState.bloodPressure,
                            bodyFat = uiState.bodyFat,
                            heartRate = uiState.heartRate,
                            exercise = uiState.exercise
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestCard(onRequestClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Health Connect 권한이 필요합니다",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "건강 데이터를 수집하고 분석하기 위해 Health Connect에 접근해야 합니다. 아래 버튼을 눌러 권한을 설정해주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "권한 요청하기",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorScreen(message: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오류 발생",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message ?: "알 수 없는 오류가 발생했습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ModernHealthDataOverview(
    onNavigateToStepsList: () -> Unit,
    sleepSessions: List<SleepSessionData>,
    steps: List<StepData>,
    weights: List<WeightData>,
    bloodGlucose: List<BloodGlucoseData>,
    bloodPressure: List<BloodPressureData>,
    bodyFat: List<BodyFatData>,
    heartRate: List<HeartRateData>,
    exercise: List<ExerciseData>,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 건강 데이터를 세로로 한 줄씩 표시
        MinimalHealthDataItem(
            title = "걸음",
            value = "${steps.sumOf { it.stepCount }}",
            suffix = "steps",
            icon = "👣",
            color = Color(0xFF4CAF50),
            onClick = onNavigateToStepsList // 클릭 시 StepsList로 이동
        )

        MinimalHealthDataItem(
            title = "수면",
            value = if (sleepSessions.isNotEmpty()) {
                val duration = sleepSessions.last().duration
                "${duration?.toMinutes()?.div(60)}h ${duration?.toMinutes()?.rem(60)}m"
            } else "0h",
            icon = "😴",
            color = Color(0xFF2196F3)
        )

        MinimalHealthDataItem(
            title = "혈압",
            value = if (bloodPressure.isNotEmpty()) {
                "${bloodPressure.last().systolic}/${bloodPressure.last().diastolic}"
            } else "-",
            suffix = "mmHg",
            icon = "❤️",
            color = Color(0xFFE53935)
        )

        MinimalHealthDataItem(
            title = "혈당",
            value = if (bloodGlucose.isNotEmpty()) {
                "${bloodGlucose.last().level}"
            } else "-",
            suffix = "mg/dL",
            icon = "🩸",
            color = Color(0xFF9C27B0)
        )

        MinimalHealthDataItem(
            title = "운동",
            value = if (exercise.isNotEmpty()) {
                "${java.time.Duration.between(exercise.last().startTime, exercise.last().endTime).toMinutes()}"
            } else "-",
            suffix = "분",
            icon = "🏃",
            color = Color(0xFF795548)
        )

        MinimalHealthDataItem(
            title = "심박수",
            value = if (heartRate.isNotEmpty()) {
                "${heartRate.last().samples.firstOrNull()?.beatsPerMinute ?: "-"}"
            } else "-",
            suffix = "bpm",
            icon = "💓",
            color = Color(0xFFE91E63)
        )

        MinimalHealthDataItem(
            title = "체중",
            value = if (weights.isNotEmpty()) {
                String.format("%.1f", weights.last().getWeightInKg())
            } else "-",
            suffix = "kg",
            icon = "⚖️",
            color = Color(0xFF3F51B5)
        )

        MinimalHealthDataItem(
            title = "체지방",
            value = if (bodyFat.isNotEmpty()) {
                String.format("%.2f", bodyFat.last().bodyFatPercentage)
            } else "-",
            suffix = "%",
            icon = "📊",
            color = Color(0xFF009688)
        )
    }
}

@Composable
fun MinimalHealthDataItem(
    title: String,
    value: String,
    suffix: String = "",
    icon: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp,
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 아이콘 영역
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 제목 영역
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // 값 영역
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )

            if (suffix.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }

    Divider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}
