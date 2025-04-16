package com.hdil.rebloomlens.sensor_plugins.health_connect

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.NotFoundException
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.sensor_plugins.health_connect.bloodglucose.BloodGlucoseDataSource
import com.hdil.rebloomlens.sensor_plugins.health_connect.bloodpressure.BloodPressureDataSource
import com.hdil.rebloomlens.sensor_plugins.health_connect.bodyfat.BodyFatDataSource
import com.hdil.rebloomlens.sensor_plugins.health_connect.sleep.SleepSessionDataSource
import com.hdil.rebloomlens.sensor_plugins.health_connect.step.StepDataSource
import com.hdil.rebloomlens.sensor_plugins.health_connect.weight.WeightDataSource
import org.json.JSONArray
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Instant


// Utilized by https://github.com/android/health-samples


class HealthConnectManager(
    private val context: Context,
    private val recordTypes: JSONArray
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    // Data source
    private val sleepSessionDataSource by lazy {
        SleepSessionDataSource(healthConnectClient)
    }
    private val stepDataSource by lazy {
        StepDataSource(healthConnectClient)
    }
    private val weightDataSource by lazy {
        WeightDataSource(healthConnectClient)
    }
    private val bloodglucoseDataSource by lazy {
        BloodGlucoseDataSource(healthConnectClient)
    }
    private val bloodPressureDataSource by lazy {
        BloodPressureDataSource(healthConnectClient)
    }
    private val bodyFatDataSource by lazy {
        BodyFatDataSource(healthConnectClient)
    }

    var availability = mutableStateOf(SDK_UNAVAILABLE)
        private set

    val permissions: Set<String> by lazy { buildRequiredPermissions() }

    fun checkAvailability() {
        availability.value = HealthConnectClient.getSdkStatus(context)
    }

    init {
        checkAvailability()
    }

    private fun buildRequiredPermissions(): Set<String> {
        val permissions = mutableSetOf<String>()
        for (i in 0 until recordTypes.length()) {
            when (val type = recordTypes.getString(i)) {
                // Blood Glucose
                "Blood Glucose" -> {
                    permissions.add(HealthPermission.getReadPermission(BloodGlucoseRecord::class))
                }
                // Blood Pressure
                "Blood Pressure" -> {
                    permissions.add(HealthPermission.getReadPermission(BloodPressureRecord::class))
                }
                // Body Fat
                "Body Fat" -> {
                    permissions.add(HealthPermission.getReadPermission(BodyFatRecord::class))
                }
                // Exercise
                "Exercise" -> {
                    permissions.add(HealthPermission.getReadPermission(ExerciseSessionRecord::class))
                }
                // Heart Rate
                "Heart Rate" -> {
                    permissions.add(HealthPermission.getReadPermission(HeartRateRecord::class))
                }
                // Sleep
                "Sleep" -> {
                    permissions.add(HealthPermission.getReadPermission(SleepSessionRecord::class))
                }
                // Steps
                "Steps" -> {
                    permissions.add(HealthPermission.getReadPermission(StepsRecord::class))
                }
                // Weight
                "Weight" -> {
                    permissions.add(HealthPermission.getReadPermission(WeightRecord::class))
                }
            }
        }
        return permissions
    }

    fun getPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun checkPermissionsAndRun(requestPermissions: ActivityResultLauncher<Set<String>>, onGranted: suspend () -> Unit) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(permissions)) {
            onGranted()
        } else {
            requestPermissions.launch(permissions)
        }
    }

    suspend fun revokeAllPermissions() {
        healthConnectClient.permissionController.revokeAllPermissions()
    }

    /**
     * Reads in existing [SleepSessionData]s.
     */
    suspend fun readSleepSessions() = sleepSessionDataSource.readSleepSessions()

    /**
     * Reads in existing [StepData]s.
     */
    suspend fun readStepData() = stepDataSource.readSteps()

    /**
     * Reads in existing [WeightData]s.
     */
    suspend fun readWeightData() = weightDataSource.readWeight()

    /**
     * Reads in existing [BloodGlucoseData]s.
     */
    suspend fun readBloodGlucoseData() = bloodglucoseDataSource.readBloodGlucose()

    /**
     * Reads in existing [BloodPressureData]s.
     */
    suspend fun readBloodPressureData() = bloodPressureDataSource.readBloodPressure()

    /**
     * Reads in existing [BodyFatData]s.
     */
    suspend fun readBodyFatData() = bodyFatDataSource.readBodyFat()
}