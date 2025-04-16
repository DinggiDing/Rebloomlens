package com.hdil.rebloomlens.sensor_plugins.health_connect.bloodglucose

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.BloodGlucoseData
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// ROLE : Called by HealthConnectManager to retrieve blood glucose data from Health Connect
/**
 * BloodGlucoseDataSource is responsible for reading blood glucose data from the Health Connect API.
 *
 * @param healthConnectClient The Health Connect client used to interact with the Health Connect API.
 */

class BloodGlucoseDataSource(
    private val healthConnectClient: HealthConnectClient
) {
    suspend fun readBloodGlucose(): List<BloodGlucoseData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<BloodGlucoseData>()
        val bloodGlucoseRequest = ReadRecordsRequest(
            recordType = BloodGlucoseRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), lastDay.toInstant()),
            ascendingOrder = false
        )

        val bloodGlucose = healthConnectClient.readRecords(bloodGlucoseRequest)
        bloodGlucose.records.forEach { session ->
            sessions.add(
                BloodGlucoseData(
                    uid = session.metadata.id,
                    time = session.time,
                    level = session.level,
                    specimenSource = session.specimenSource,
                    mealType = session.mealType,
                    relationToMeal = session.relationToMeal
                )
            )
        }

        return sessions
    }
}