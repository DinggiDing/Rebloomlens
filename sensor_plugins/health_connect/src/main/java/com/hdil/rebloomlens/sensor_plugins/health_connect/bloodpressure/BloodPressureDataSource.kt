package com.hdil.rebloomlens.sensor_plugins.health_connect.bloodpressure

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.BloodPressureData
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// ROLE : Called by HealthConnectManager to retrieve blood pressure data from Health Connect
/**
 * BloodPressureDataSource is responsible for reading blood pressure data from the Health Connect API.
 *
 * @param healthConnectClient The Health Connect client used to interact with the Health Connect API.
 */

class BloodPressureDataSource(
    private val healthConnectClient: HealthConnectClient,
) {
    suspend fun readBloodPressure(): List<BloodPressureData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<BloodPressureData>()
        val bloodPressureRequest = ReadRecordsRequest(
            recordType = BloodPressureRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), lastDay.toInstant()),
            ascendingOrder = false
        )

        val bloodPressure = healthConnectClient.readRecords(bloodPressureRequest)
        bloodPressure.records.forEach { session ->
            sessions.add(
                BloodPressureData(
                    uid = session.metadata.id,
                    time = session.time,
                    systolic = session.systolic,
                    diastolic = session.diastolic,
                    bodyPosition = session.bodyPosition,
                    measurementLocation = session.measurementLocation
                )
            )
        }

        return sessions
    }
}