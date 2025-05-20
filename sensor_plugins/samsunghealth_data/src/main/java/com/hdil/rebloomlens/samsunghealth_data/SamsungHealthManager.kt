package com.hdil.rebloomlens.samsunghealth_data

import android.app.Activity
import android.content.Context
import com.hdil.rebloomlens.common.model.HeartRateData
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

class SamsungHealthManager(
    private val healthDataStore: HealthDataStore,
    private val recordTypes: JSONArray
) {
    // 각 데이터 타입별 데이터 소스
    private val heartRateDataSource = HeartRateDataSource(healthDataStore)

    suspend fun checkAndRequestPermissions(
        context: Context,
        permSet: MutableSet<Permission>,
        activityId: Int
    ): Pair<String, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val grantedPermissions = healthDataStore.getGrantedPermissions(permSet)

                if (grantedPermissions.containsAll(permSet)) {
                    return@withContext Pair(AppConstants.SUCCESS, activityId)
                } else {
                    val activity = context as Activity
                    val result = healthDataStore.requestPermissions(permSet, activity)
                    Logger.i("requestPermissions: Success ${result.size}")

                    if (result.containsAll(permSet)) {
                        return@withContext Pair(AppConstants.SUCCESS, activityId)
                    } else {
                        return@withContext Pair(AppConstants.NO_PERMISSION, -1)
                    }
                }
            } catch (e: Exception) {
                Logger.e("권한 요청 중 오류 발생: ${e.message}")
                return@withContext Pair("권한 요청 중 오류 발생: ${e.message}", -1)
            }
        }
    }

    suspend fun connectToSamsungHealth(context: Context) {
        withContext(Dispatchers.IO) {
            val permSet = setOf(
                Permission.of(DataTypes.STEPS, AccessType.READ),
                Permission.of(DataTypes.SLEEP, AccessType.READ),
                Permission.of(DataTypes.HEART_RATE, AccessType.READ),
                Permission.of(DataTypes.BLOOD_PRESSURE, AccessType.READ),
                Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ),
                Permission.of(DataTypes.EXERCISE, AccessType.READ)
            )

            try {
                healthDataStore.requestPermissions(permSet, context as Activity)
            } catch (e: Exception) {
                Logger.e("Samsung Health 연결 중 오류 발생: ${e.message}")
            }
        }
    }

    /**
     * 심박수 데이터를 읽습니다.
     */
    suspend fun readHeartRateData(): List<HeartRateData> {
        return withContext(Dispatchers.IO) {
            heartRateDataSource.readHeartRate()
        }
    }

    // 다른 데이터 타입에 대한 메소드도 추가할 수 있습니다.
    // 예: readStepData(), readSleepData() 등
}