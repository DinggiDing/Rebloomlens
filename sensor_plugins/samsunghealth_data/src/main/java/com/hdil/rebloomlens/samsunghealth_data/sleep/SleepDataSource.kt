package com.hdil.rebloomlens.samsunghealth_data.sleep

import com.hdil.rebloomlens.common.model.SleepSessionData
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class SleepDataSource(
    private val healthDataStore: HealthDataStore
) {
    suspend fun readSleep(): List<SleepSessionData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<SleepSessionData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), lastDay.toLocalDateTime())
        val readRequest = DataTypes.SLEEP.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()
        val sleepList = healthDataStore.readData(readRequest).dataList

        sleepList.forEach { session ->
            // TODO: Handle null values for startTime and endTime
            val startTime = session.startTime
            val endTime = session.endTime!!
            val sleepType = session.getValue(DataTypes.SLEEP_TYPE)?.toString() ?: return@forEach
            val sleepDuration = session.getValue(DataTypes.SLEEP_DURATION)?.toLong() ?: return@forEach

            sessions.add(
                SleepSessionData(
                    uid = session.uid,
                    startTime = startTime,
                    endTime = endTime,
                    sleepType = sleepType,
                    sleepDuration = sleepDuration
                )
            )
        }
    }

}