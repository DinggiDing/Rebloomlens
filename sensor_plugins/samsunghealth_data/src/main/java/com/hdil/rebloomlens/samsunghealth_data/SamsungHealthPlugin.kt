package com.hdil.rebloomlens.samsunghealth_data

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hdil.rebloomlens.common.model.BloodPressureData
import com.hdil.rebloomlens.common.model.BodyFatData
import com.hdil.rebloomlens.common.model.ExerciseData
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.model.WeightData
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthMainViewModel
import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthViewModelFactory
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Duration

/**
 * ROLE : SamsungHealthPlugin
 *
 * Entry point that connects the Samsung Health SDK with the application.
 * Responsible for permission management, rendering UI elements for data display, and handling user interactions.
 * Integrates Samsung Health functionality into the app through the Plugin system.
 */

class SamsungHealthPlugin(
    override val pluginId: String,
    override val config: JSONObject
) : Plugin {

    private lateinit var healthDataStore: HealthDataStore
    private lateinit var samsungHealthManager: SamsungHealthManager
    private lateinit var viewModelFactory: SamsungHealthViewModelFactory

    override fun initialize(context: Context) {
        val recordTypes = config.optJSONArray("recordTypes") ?: return

        // Samsung Health SDK 초기화
        healthDataStore = HealthDataService.getStore(context)
        samsungHealthManager = SamsungHealthManager(healthDataStore, recordTypes)
        viewModelFactory = SamsungHealthViewModelFactory(samsungHealthManager)
    }

//    

    @Composable
    override fun renderUI() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var permissionGranted by remember { mutableStateOf(false) }

        val viewModel: SamsungHealthMainViewModel = viewModel(factory = viewModelFactory)
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            scope.launch {
                val result = samsungHealthManager.checkPermissionsAndRun(
                    context,
                    0
                ) {
                    permissionGranted = true
                }
                permissionGranted = result.first == AppConstants.SUCCESS
            }
        }

        LaunchedEffect(permissionGranted) {
            if (permissionGranted) {
                viewModel.loadHeartRateData()
                viewModel.loadSleepData()
                viewModel.loadStepData()
                viewModel.loadBloodPressureData()
                viewModel.loadBodyFatData()
                viewModel.loadSkeletalMuscleMassData()
                viewModel.loadWeightData()
                viewModel.loadExerciseData()
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Samsung Health",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    uiState.isLoading -> LoadingScreen()
                    uiState.error != null -> ErrorScreen(message = uiState.error)
                    !permissionGranted -> {
                        Text(
                            text = "Samsung Health 권한을 허용해주세요",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    else -> {
                        // 건강 데이터 요약
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
                            // 동기화 시간 등 추가 가능
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        ModernHealthDataOverview(
                            sleepSessions = uiState.sleep,
                            steps = uiState.step,
                            weights = uiState.weight,
                            bloodPressure = uiState.bloodPressure,
                            bodyFat = uiState.bodyFat,
                            heartRate = uiState.heartRate,
                            exercise = uiState.exercise,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String?) {
    Text(
        text = message ?: "알 수 없는 오류가 발생했습니다",
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
fun ModernHealthDataOverview(
    sleepSessions: List<SleepSessionData>,
    steps: List<StepData>,
    weights: List<WeightData>,
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
            color = Color(0xFF4CAF50)
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
            title = "운동",
            value = if (exercise.isNotEmpty()) {
                "${Duration.between(exercise.last().startTime, exercise.last().endTime).toMinutes()}"
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
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
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