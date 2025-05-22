package com.hdil.rebloomlens.samsunghealth_data

import android.app.Activity
import android.content.Context
import com.hdil.rebloomlens.common.utils.Logger
import com.hdil.rebloomlens.samsunghealth_data.heartrate.HeartRateDataSource
import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * ROLE : Data Access Layer that Directly Interacts with Samsung Health SDK
 *
 * Handles direct communication with Samsung Health SDK,
 * provides permission requesting, initialization, and health data access functionality.
 * Manages multiple data sources and provides necessary data to ViewModel.
 * Abstracts Samsung Health API calls for use by other parts of the app.
 */

class SamsungHealthManager(
    private val healthDataStore: HealthDataStore,
    private val recordTypes: JSONArray
) {
    // 각 데이터 타입별 데이터 소스
    private val heartRateDataSource = HeartRateDataSource(healthDataStore)

    val permissions: Set<Permission> by lazy { connectToSamsungHealth() }

    suspend fun checkPermissionsAndRun(
        context: Context,
        activityId: Int,
        onGranted: suspend () -> Unit
    ): Pair<String, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val grantedPermissions = healthDataStore.getGrantedPermissions(permissions)
                if (grantedPermissions.containsAll(permissions)) {
                    // 권한이 이미 있으면 작업 실행
                    onGranted()
                    return@withContext Pair(AppConstants.SUCCESS, activityId)
                } else {
                    // 권한 요청
                    val activity = context as? Activity
                        ?: return@withContext Pair("활동 컨텍스트가 필요합니다", -1)

                    val result = healthDataStore.requestPermissions(permissions, activity)
                    Logger.i("requestPermissions: Result size ${result.size}")

                    if (result.containsAll(permissions)) {
                        // 권한이 승인되면 작업 실행
                        onGranted()
                        return@withContext Pair(AppConstants.SUCCESS, activityId)
                    } else {
                        return@withContext Pair(AppConstants.NO_PERMISSION, -1)
                    }
                }
            } catch (e: Exception) {
                Logger.e("권한 확인 및 실행 중 오류 발생: ${e.message}")
                return@withContext Pair("권한 처리 중 오류: ${e.message}", -1)
            }
        }
    }

    private fun connectToSamsungHealth(): Set<Permission> {
        val permSet = mutableSetOf<Permission>()

        // recordTypes에서 필요한 권한을 동적으로 추가
        for (i in 0 until recordTypes.length()) {
            try {
                val recordType = recordTypes.getString(i)
                when (recordType.lowercase()) {
                    "steps" -> permSet.add(Permission.of(DataTypes.STEPS, AccessType.READ))
                    "sleep" -> permSet.add(Permission.of(DataTypes.SLEEP, AccessType.READ))
                    "heart rate" -> permSet.add(Permission.of(DataTypes.HEART_RATE, AccessType.READ))
                    "blood pressure" -> permSet.add(Permission.of(DataTypes.BLOOD_PRESSURE, AccessType.READ))
                    "body composition" -> permSet.add(Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ))
                    "exercise" -> permSet.add(Permission.of(DataTypes.EXERCISE, AccessType.READ))
                    // 필요한 다른 데이터 타입도 여기에 추가할 수 있습니다
                }
            } catch (e: Exception) {
                Logger.e("권한 설정 중 오류 발생: ${e.message}")
            }
        }

        return permSet
    }

    // 외부에서 삼성 헬스 연결 초기화를 위해 호출할 수 있는 public 함수
    suspend fun initSamsungHealthConnection(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val permSet = permissions
                if (permSet.isEmpty()) {
                    Logger.e("요청할 권한이 없습니다. recordTypes를 확인하세요.")
                    return@withContext
                }

                healthDataStore.requestPermissions(permSet, context as Activity)
            } catch (e: Exception) {
                Logger.e("Samsung Health 연결 중 오류 발생: ${e.message}")
            }
        }
    }

    /**
     * 심박수 데이터를 읽습니다.
     */
    suspend fun readHeartRateData() = heartRateDataSource.readHeartRate()

    // 다른 데이터 타입에 대한 메소드도 추가할 수 있습니다.
    // 예: readStepData(), readSleepData() 등
}