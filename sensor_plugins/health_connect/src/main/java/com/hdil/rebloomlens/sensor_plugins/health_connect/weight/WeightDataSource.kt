package com.hdil.rebloomlens.sensor_plugins.health_connect.weight

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.WeightData
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// ROLE : Called by HealthConnectManager to retrieve weight data from Health Connect
/**
 * WeightDataSource is responsible for reading weight data from the Health Connect API.
 *
 * @param healthConnectClient The Health Connect client used to interact with the Health Connect API.
 */

class WeightDataSource(
    private val healthConnectClient: HealthConnectClient,
) {
    suspend fun readWeight(): List<WeightData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<WeightData>()
        val weightRequest = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), Instant.now()),
            ascendingOrder = false
        )

        val weight = healthConnectClient.readRecords(weightRequest)
        weight.records.forEach { session ->
            sessions.add(
                WeightData(
                    uid = session.metadata.id,
                    time = session.time,
                    weight = session.weight
                )
            )
        }

        return sessions
    }
}