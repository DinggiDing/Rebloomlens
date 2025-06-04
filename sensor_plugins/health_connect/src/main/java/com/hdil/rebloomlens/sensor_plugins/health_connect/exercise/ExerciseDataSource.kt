package com.hdil.rebloomlens.sensor_plugins.health_connect.exercise

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.hdil.rebloomlens.common.model.ExerciseData
import java.time.Instant

class ExerciseDataSource(
    private val healthConnectClient: HealthConnectClient
) {
    suspend fun readExercises(): List<ExerciseData> {
        val lastDay = java.time.ZonedDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<ExerciseData>()

        val exerciseRequest = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(firstDay.toInstant(), Instant.now()),
            ascendingOrder = false
        )

        val exerciseSessions = healthConnectClient.readRecords(exerciseRequest)
        exerciseSessions.records.forEach { session ->
//            Logger.e("HealthConnect_Exercise: ${session.startTime}~${session.endTime}, Type: ${session.exerciseType.toString()}")

            sessions.add(
                ExerciseData(
                    uid = session.metadata.id,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    exerciseType = session.exerciseType.toString(),
                )
            )
        }

        return sessions
    }
}