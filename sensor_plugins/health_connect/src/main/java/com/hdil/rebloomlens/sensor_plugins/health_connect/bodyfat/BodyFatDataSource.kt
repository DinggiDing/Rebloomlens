package com.hdil.rebloomlens.sensor_plugins.health_connect.bodyfat

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.BodyFatData
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// ROLE : Called by HealthConnectManager to retrieve body fat data from Health Connect
/**
 * BodyFatDataSource is responsible for reading body fat data from the Health Connect API.
 *
 * @param healthConnectClient The Health Connect client used to interact with the Health Connect API.
 */

class BodyFatDataSource(
    private val healthConnectClient: HealthConnectClient
) {
    suspend fun readBodyFat(): List<BodyFatData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<BodyFatData>()
        val bodyFatRequest = ReadRecordsRequest(
            recordType = BodyFatRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), lastDay.toInstant()),
            ascendingOrder = false
        )

        val bodyFat = healthConnectClient.readRecords(bodyFatRequest)
        bodyFat.records.forEach { session ->
            sessions.add(
                BodyFatData(
                    uid = session.metadata.id,
                    time = session.time,
                    bodyFatPercentage = session.percentage.value
                )
            )
        }

        return sessions
    }
}