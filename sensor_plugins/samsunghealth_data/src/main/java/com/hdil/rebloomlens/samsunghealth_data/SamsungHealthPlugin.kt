package com.hdil.rebloomlens.samsunghealth_data

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hdil.rebloomlens.common.model.BloodPressureData
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.DateTimeUtils
import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthMainViewModel
import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthViewModelFactory
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.helper.SdkVersion
import kotlinx.coroutines.launch
import org.json.JSONObject

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

    @Composable
    override fun renderUI() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var permissionGranted by remember { mutableStateOf(false) }

        val viewModel: SamsungHealthMainViewModel = viewModel(factory = viewModelFactory)
        val uiState by viewModel.uiState.collectAsState()

        // 초기 권한 확인 및 처리
        LaunchedEffect(Unit) {
            scope.launch {
                val result = samsungHealthManager.checkPermissionsAndRun(
                    context,
                    0 // 초기 액티비티 ID
                ) {
                    permissionGranted = true
                }
                permissionGranted = result.first == AppConstants.SUCCESS
            }
        }

        // 권한이 부여되면 데이터 로드
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
                // 다른 데이터 로드 메서드 추가 가능
                // viewModel.loadStepData()
                // viewModel.loadSleepData()
                // 등등...
            }
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Samsung Health",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    scope.launch {
                        samsungHealthManager.initSamsungHealthConnection(context)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "권한 요청"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SDK 버전: ${SdkVersion.getVersionName()}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    // 개요 섹션
                    Text(
                        text = "개요",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HealthDataOverview(
                        heartRate = uiState.heartRate,
                        sleep = uiState.sleep,
                        step = uiState.step,
                        bloodPressure = uiState.bloodPressure,
                        onHeartRateClick = {
                            viewModel.runWithPermissions(
                                context,
                                AppConstants.HEART_RATE_ACTIVITY
                            ) {
                                viewModel.loadHeartRateData()
                            }
                        }
                    )

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
fun HealthDataOverview(
    heartRate: List<HeartRateData>,
    sleep: List<SleepSessionData>,
    step: List<StepData>,
    bloodPressure: List<BloodPressureData>,
    onHeartRateClick: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(450.dp)
    ) {
        item {
            OverviewCard(
                title = "심박수",
                value = if (heartRate.isNotEmpty() && heartRate.last().samples.isNotEmpty()) {
                    "${heartRate.last().samples.last().beatsPerMinute} BPM"
                } else "기록 없음",
                description = if (heartRate.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(heartRate.last().startTime)}"
                } else "기록을 불러오려면 클릭하세요",
                onClick = onHeartRateClick
            )
        }
        item {
            OverviewCard(
                title = "수면",
                value = if (sleep.isNotEmpty()) {
                    "${sleep.size} 세션"
                } else "기록 없음",
                description = if (sleep.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(sleep.first().startTime)}"
                } else "기록을 불러오려면 클릭하세요",
                onClick = { /* TODO: 수면 데이터 클릭 시 동작 */ }
            )
        }
        item {
            OverviewCard(
                title = "걸음 수",
                value = if (step.isNotEmpty()) {
                    "${step.last().stepCount} 걸음"
                } else "기록 없음",
                description = if (step.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(step.first().startTime)}"
                } else "기록을 불러오려면 클릭하세요",
                onClick = { /* TODO: 걸음 수 데이터 클릭 시 동작 */ }
            )
        }
        item {
            OverviewCard(
                title = "혈압",
                value = if (bloodPressure.isNotEmpty()) {
                    "${bloodPressure.last().systolic}~${bloodPressure.last().diastolic} 걸음"
                } else "기록 없음",
                description = "기록을 불러오려면 클릭하세요",
                onClick = { /* TODO: 혈압 데이터 클릭 시 동작 */ }
            )
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick),
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