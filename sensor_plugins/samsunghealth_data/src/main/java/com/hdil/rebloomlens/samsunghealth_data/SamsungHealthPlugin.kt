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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.DateTimeUtils
import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthMainViewModel
import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthViewModelFactory
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.helper.SdkVersion
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes
import org.json.JSONObject


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
        val viewModel: SamsungHealthMainViewModel = viewModel(factory = viewModelFactory)
        val uiState by viewModel.uiState.collectAsState()
        val context = androidx.compose.ui.platform.LocalContext.current

        var permissionState by remember { mutableStateOf(false) }

        // 권한 응답 처리
        LaunchedEffect(viewModel.permissionResponse) {
            viewModel.permissionResponse.collect { (code, activityId) ->
                when {
                    code == AppConstants.SUCCESS -> {
                        permissionState = true
                        // 심박수 데이터 로드
                        viewModel.loadHeartRateData()
                    }
                    code != AppConstants.WAITING -> {
                        android.widget.Toast.makeText(context, code, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.resetPermissionResponse()
            }
        }

        // 초기 데이터 로드 시도
        LaunchedEffect(Unit) {
            val permSet = mutableSetOf(
                Permission.of(DataTypes.HEART_RATE, AccessType.READ),
                Permission.of(DataTypes.EXERCISE, AccessType.READ),
                Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ)
            )

            // 이미 권한이 있는지 확인하고 자동으로 데이터 로드
            viewModel.checkForPermission(context, permSet, AppConstants.HEART_RATE_ACTIVITY)
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

                IconButton(onClick = { viewModel.connectToSamsungHealth(context) }) {
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
                else -> {
                    // 개요 섹션
                    Text(
                        text = "개요",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HealthDataOverview(
                        heartRate = uiState.heartRate,
                        onHeartRateClick = {
                            viewModel.checkForPermission(
                                context,
                                mutableSetOf(
                                    Permission.of(DataTypes.HEART_RATE, AccessType.READ),
                                    Permission.of(DataTypes.EXERCISE, AccessType.READ),
                                    Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ)
                                ),
                                AppConstants.HEART_RATE_ACTIVITY
                            )
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
                value = if (heartRate.isNotEmpty() && heartRate.first().samples.isNotEmpty()) {
                    "${heartRate.first().samples.first().beatsPerMinute} BPM"
                } else "기록 없음",
                description = if (heartRate.isNotEmpty()) {
                    "최근: ${DateTimeUtils.formatDateTime(heartRate.first().startTime)}"
                } else "기록을 불러오려면 클릭하세요",
                onClick = onHeartRateClick
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