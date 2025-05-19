//package com.hdil.rebloomlens.samsunghealth_data.activity
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.ViewModelProvider
//import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
//import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthMainViewModel
//import com.hdil.rebloomlens.samsunghealth_data.viewmodel.SamsungHealthViewModelFactory
//import com.samsung.android.sdk.health.data.helper.SdkVersion
//import com.samsung.android.sdk.health.data.permission.AccessType
//import com.samsung.android.sdk.health.data.permission.Permission
//import com.samsung.android.sdk.health.data.request.DataTypes
//
//class HealthMainActivity : ComponentActivity() {
//
//    private lateinit var viewModel: SamsungHealthMainViewModel
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContent {
//            val context = LocalContext.current
//
//            viewModel = ViewModelProvider(
//                this, SamsungHealthViewModelFactory(this)
//            )[SamsungHealthMainViewModel::class.java]
//
//            // Side-effect: 권한 응답 Flow 수집 → Toast 또는 화면 전환
//            LaunchedEffect(viewModel.permissionResponse) {
//                viewModel.permissionResponse.collect { (code, activityId) ->
//                    when {
//                        code == AppConstants.SUCCESS -> {
//                            launchActivity(context, activityId)
//                        }
//                        code != AppConstants.WAITING -> {
//                            Toast.makeText(context, code, Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    viewModel.resetPermissionResponse()
//                }
//            }
//
//            HealthMainScreen(
//                versionName = SdkVersion.getVersionName(),
//                onPermissionClick = { viewModel.connectToSamsungHealth(context) },
//                onNutritionClick = {
//                    viewModel.checkForPermission(
//                        this,
//                        mutableSetOf(Permission.of(DataTypes.NUTRITION, AccessType.READ)),
//                        AppConstants.NUTRITION_ACTIVITY
//                    )
//                },
//                onStepClick = {
//                    viewModel.checkForPermission(
//                        this,
//                        mutableSetOf(Permission.of(DataTypes.STEPS, AccessType.READ)),
//                        AppConstants.STEP_ACTIVITY
//                    )
//                },
//                onHeartRateClick = {
//                    viewModel.checkForPermission(
//                        this,
//                        mutableSetOf(
//                            Permission.of(DataTypes.HEART_RATE, AccessType.READ),
//                            Permission.of(DataTypes.EXERCISE, AccessType.READ),
//                            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ)
//                        ),
//                        AppConstants.HEART_RATE_ACTIVITY
//                    )
//                },
//                onSleepClick = {
//                    viewModel.checkForPermission(
//                        this,
//                        mutableSetOf(
//                            Permission.of(DataTypes.SLEEP, AccessType.READ),
//                            Permission.of(DataTypes.BLOOD_OXYGEN, AccessType.READ),
//                            Permission.of(DataTypes.SKIN_TEMPERATURE, AccessType.READ)
//                        ),
//                        AppConstants.SLEEP_ACTIVITY
//                    )
//                }
//            )
//        }
//    }
//
//    private fun launchActivity(context: Context, activityId: Int) {
//        val intent = when (activityId) {
//            AppConstants.NUTRITION_ACTIVITY -> Intent(context, NutritionActivity::class.java)
//            AppConstants.STEP_ACTIVITY     -> Intent(context, StepActivity::class.java)
//            AppConstants.HEART_RATE_ACTIVITY -> Intent(context, HeartRateActivity::class.java)
//            AppConstants.SLEEP_ACTIVITY    -> Intent(context, SleepActivity::class.java)
//            AppConstants.EXERCISE_ACTIVITY -> Intent(context, ExerciseActivity::class.java)
//            else                           -> null
//        }
//        intent?.let { context.startActivity(it) }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HealthMainScreen(
//    versionName: String,
//    onPermissionClick: () -> Unit,
//    onNutritionClick: () -> Unit,
//    onStepClick: () -> Unit,
//    onHeartRateClick: () -> Unit,
//    onSleepClick: () -> Unit,
//) {
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Health") },
//                actions = {
//                    IconButton(onClick = onPermissionClick) {
//                        Icon(
//                            imageVector = Icons.Default.Settings,
//                            contentDescription = "권한 요청"
//                        )
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            HealthCard(text = "Nutrition", onClick = onNutritionClick)
//            HealthCard(text = "Step", onClick = onStepClick)
//            HealthCard(text = "Heart Rate", onClick = onHeartRateClick)
//            HealthCard(text = "Sleep", onClick = onSleepClick)
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            Text(
//                text = "SDK Version: $versionName",
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HealthCard(text: String, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(72.dp)
//            .clickable(onClick = onClick),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Box(contentAlignment = Alignment.Center) {
//            Text(text, style = MaterialTheme.typography.bodySmall)
//        }
//    }
//}