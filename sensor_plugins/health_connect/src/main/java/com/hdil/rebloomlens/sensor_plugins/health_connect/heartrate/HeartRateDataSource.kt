package com.hdil.rebloomlens.sensor_plugins.health_connect.heartrate

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.HeartRateSample
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HeartRateDataSource(
    private val healthConnectClient: HealthConnectClient,
) {
    suspend fun readHeartRate(): List<HeartRateData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<HeartRateData>()
        val heartRateRequest = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), Instant.now()),
            ascendingOrder = false
        )

        val heartRate = healthConnectClient.readRecords(heartRateRequest)
        heartRate.records.forEach { session ->
            val convertedSamples = session.samples.map {
                HeartRateSample(
                    time = it.time,
                    beatsPerMinute = it.beatsPerMinute.toLong()
                )
            }
            sessions.add(
                HeartRateData(
                    uid = session.metadata.id,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    samples = convertedSamples
                )
            )
        }

        return sessions
    }
}