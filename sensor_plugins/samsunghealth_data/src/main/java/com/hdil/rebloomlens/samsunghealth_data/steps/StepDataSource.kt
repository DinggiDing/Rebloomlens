package com.hdil.rebloomlens.samsunghealth_data.steps

import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.utils.Logger
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.LocalTimeGroup
import com.samsung.android.sdk.health.data.request.LocalTimeGroupUnit
import com.samsung.android.sdk.health.data.request.Ordering

class StepDataSource(
    private val healthDataStore: HealthDataStore
) {
    suspend fun readStep(): List<StepData> {
        val lastDay = java.time.ZonedDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<StepData>()

        val localtimeFilter = LocalTimeFilter.of(
            firstDay.toLocalDateTime(), lastDay.toLocalDateTime()
        )
        val localTimeGroup = LocalTimeGroup.of(LocalTimeGroupUnit.HOURLY, 1)
        val aggregateRequest = DataType.StepsType.TOTAL.requestBuilder
            .setLocalTimeFilterWithGroup(localtimeFilter, localTimeGroup)
            .setOrdering(Ordering.ASC)
            .build()

        val stepList = healthDataStore.aggregateData(aggregateRequest).dataList

        stepList.forEach { session ->
            val stepCount = session.value as Long
            val uniqueId = "samsung_health_${session.startTime.toEpochMilli()}"
            Logger.e("${session.startTime} - step 데이터: ${stepCount}걸음 수")

            sessions.add(
                StepData(
                    uid = uniqueId,
                    startTime = session.startTime,
                    endTime = session.endTime!!,
                    stepCount = stepCount
                )
            )
        }


//        val localTimeFilter = com.samsung.android.sdk.health.data.request.LocalTimeFilter.of(
//            firstDay.toLocalDateTime(), lastDay.toLocalDateTime()
//        )
//        val readRequest = DataTypes.SLEEP.readDataRequestBuilder
//            .setLocalTimeFilter(localTimeFilter)
//            .setOrdering(Ordering.DESC)
//            .build()
//        val stepList = healthDataStore.readData(readRequest).dataList
//
//        stepList.forEach { session ->
//            val stepCount = session.getValue(DataType.StepsType.)?.toLong() ?: return@forEach
//            val time = session.startTime
//            sessions.add(
//                StepData(
//                    uid = session.uid,
//                    startTime = session.startTime,
//                    endTime = session.endTime!!,
//                    stepCount = stepCount
//                )
//            )
//        }

        return sessions
    }
}