package com.hdil.rebloomlens.sensor_plugins.health_connect.heartrate

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.utils.Logger
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.text.compareTo

class HeartRateDataSource(
    private val healthConnectClient: HealthConnectClient,
) {
    suspend fun readHeartRate(): List<HeartRateData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<HeartRateData>()
        val heartRateRequest = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), lastDay.toInstant()),
            ascendingOrder = false
        )

        val heartRate = healthConnectClient.readRecords(heartRateRequest)
        heartRate.records.forEach { session ->
            sessions.add(
                HeartRateData(
                    uid = session.metadata.id,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    samples = session.samples
                )
            )
            Logger.e("HeartRateDataSource: start: ${session.startTime} ~ end: ${session.endTime}")
            Logger.e("HeartRateDataSource: readHeartRate: ${session.samples} sessions")

        }

        return sessions
    }
}