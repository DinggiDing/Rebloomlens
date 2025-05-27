package com.hdil.rebloomlens.sensor_plugins.health_connect.step

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.StepData
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// ROLE : Called by HealthConnectManager to retrieve step data from Health Connect
/**
 * StepDataSource is responsible for reading step data from the Health Connect API.
 *
 * @param healthConnectClient The Health Connect client used to interact with the Health Connect API.
 */

class StepDataSource(
    private val healthConnectClient: HealthConnectClient
) {
    suspend fun readSteps(): List<StepData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<StepData>()
        val stepRequest = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), Instant.now()),
            ascendingOrder = false
        )

        val steps = healthConnectClient.readRecords(stepRequest)
        steps.records.forEach { session ->
            val sessionTimeFilter = TimeRangeFilter.between(session.startTime, session.endTime)
            sessions.add(
                StepData(
                    uid = session.metadata.id,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    stepCount = session.count
                )
            )
        }

        return sessions
    }
}