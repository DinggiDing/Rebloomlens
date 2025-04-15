package com.hdil.rebloomlens.sensor_plugins.health_connect.sleep

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.SleepSessionData
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

// ROLE : Called by HealthConnectManager to retrieve sleep session data from Health Connect
/**
 * SleepSessionDataSource is responsible for reading sleep session data from the Health Connect API.
 *
 * @param healthConnectClient The Health Connect client used to interact with the Health Connect API.
 */

class SleepSessionDataSource(
    private val healthConnectClient: HealthConnectClient
) {
    suspend fun readSleepSessions(): List<SleepSessionData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

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