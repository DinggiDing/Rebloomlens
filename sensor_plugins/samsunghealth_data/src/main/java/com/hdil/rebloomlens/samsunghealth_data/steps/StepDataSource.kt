package com.hdil.rebloomlens.samsunghealth_data.steps

import com.hdil.rebloomlens.common.model.StepData
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.LocalTimeGroup
import com.samsung.android.sdk.health.data.request.LocalTimeGroupUnit
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Instant
import java.time.LocalDateTime

class StepDataSource(
    private val healthDataStore: HealthDataStore
) {
    suspend fun readStep(): List<StepData> {
        val lastDay = java.time.ZonedDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<StepData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), LocalDateTime.ofInstant(Instant.now(), firstDay.zone))

        val localTimeGroup = LocalTimeGroup.of(LocalTimeGroupUnit.HOURLY, 1)
        val aggregateRequest = DataType.StepsType.TOTAL.requestBuilder
            .setLocalTimeFilterWithGroup(localTimeFilter, localTimeGroup)
            .setOrdering(Ordering.DESC)
            .build()

        val stepList = healthDataStore.aggregateData(aggregateRequest).dataList

        stepList.forEach { session ->
            val stepCount = session.value as Long
            val uniqueId = "samsung_health_${session.startTime.toEpochMilli()}"

            sessions.add(
                StepData(
                    uid = uniqueId,
                    startTime = session.startTime,
                    endTime = session.endTime!!,
                    stepCount = stepCount
                )
            )
        }

        return sessions
    }
}