package com.hdil.rebloomlens.samsunghealth_data.exercise

import com.hdil.rebloomlens.common.model.ExerciseData
import com.hdil.rebloomlens.common.utils.Logger
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Instant
import java.time.LocalDateTime

class ExerciseDataSource(
    private val healthDataStore: HealthDataStore
) {
    suspend fun readExercises(): List<ExerciseData> {
        val lastDay = java.time.ZonedDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<ExerciseData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), LocalDateTime.ofInstant(
            Instant.now(), firstDay.zone))

        val readRequest = DataTypes.EXERCISE.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()
        val exerciseList = healthDataStore.readData(readRequest).dataList

        exerciseList.forEach { session ->
            val startTime = session.startTime
            val endTime = session.endTime ?: Instant.now()
            var exerciseType = session.getValue(DataType.ExerciseType.EXERCISE_TYPE)?.toString() ?: "Unknown"
            if (exerciseType == "OTHER_WORKOUT") {
                exerciseType = session.getValue(DataType.ExerciseType.CUSTOM_TITLE).toString()
            }
            Logger.e("SamsungHealth_Exercise: $startTime~$endTime, Type: $exerciseType")

            sessions.add(
                ExerciseData(
                    uid = session.uid,
                    startTime = startTime,
                    endTime = endTime,
                    exerciseType = exerciseType
                )
            )

        }

        return sessions
    }
}