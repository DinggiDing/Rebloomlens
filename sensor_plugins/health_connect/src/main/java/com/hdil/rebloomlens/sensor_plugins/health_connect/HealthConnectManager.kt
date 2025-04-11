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
import com.hdil.rebloomlens.sensor_plugins.health_connect.sleep.SleepSessionData
import org.json.JSONArray
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Instant


// Utilized by https://github.com/android/health-samples

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

class HealthConnectManager(
    private val context: Context,
    private val recordTypes: JSONArray
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

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


    suspend fun readSleepSessions(): List<SleepSessionData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
            .minusDays(1)
            .withHour(12)
        val firstDay = lastDay.minusDays(7)

        val sessions = mutableListOf<SleepSessionData>()
        val sleepSessionRequest = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), lastDay.toInstant()),
            ascendingOrder = false
        )
        val sleepSessions = healthConnectClient.readRecords(sleepSessionRequest)
        sleepSessions.records.forEach { session ->
            val sessionTimeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
            val durationAggregateRequest = AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = sessionTimeFilter
            )
            val aggregateResponse = healthConnectClient.aggregate(durationAggregateRequest)
            sessions.add(
                SleepSessionData(
                    uid = session.metadata.id,
                    title = session.title,
                    notes = session.notes,
                    startTime = session.startTime,
                    startZoneOffset = session.startZoneOffset,
                    endTime = session.endTime,
                    endZoneOffset = session.endZoneOffset,
                    duration = aggregateResponse[SleepSessionRecord.SLEEP_DURATION_TOTAL],
                    stages = session.stages
                )
            )
        }
        return sessions
    }
}