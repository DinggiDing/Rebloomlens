package com.hdil.rebloomlens.sensor_plugins.health_connect.sleep

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_AWAKE
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_AWAKE_IN_BED
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_DEEP
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_LIGHT
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_OUT_OF_BED
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_REM
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_SLEEPING
import androidx.health.connect.client.records.SleepSessionRecord.Companion.STAGE_TYPE_UNKNOWN
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.SleepStage
import com.hdil.rebloomlens.common.model.SleepStageType
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
                    stages = session.stages.map { stage ->
                        SleepStage(
                            startTime = stage.startTime,
                            endTime = stage.endTime,
                            stage = mapSleepStageType(stage.stage)
                        )
                    }
                )
            )
        }
        return sessions
    }

    private fun mapSleepStageType(stage: Int): SleepStageType {
        return when (stage) {
            STAGE_TYPE_AWAKE -> SleepStageType.AWAKE
            STAGE_TYPE_AWAKE_IN_BED -> SleepStageType.AWAKE_IN_BED
            STAGE_TYPE_DEEP -> SleepStageType.DEEP
            STAGE_TYPE_LIGHT -> SleepStageType.LIGHT
            STAGE_TYPE_OUT_OF_BED -> SleepStageType.OUT_OF_BED
            STAGE_TYPE_REM -> SleepStageType.REM
            STAGE_TYPE_SLEEPING -> SleepStageType.SLEEPING
            STAGE_TYPE_UNKNOWN -> SleepStageType.UNKNOWN
            else -> SleepStageType.UNKNOWN
        }
    }
}
