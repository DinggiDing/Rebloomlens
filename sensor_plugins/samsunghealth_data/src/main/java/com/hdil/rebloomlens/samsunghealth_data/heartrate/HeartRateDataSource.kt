package com.hdil.rebloomlens.samsunghealth_data.heartrate

import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.HeartRateSample
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HeartRateDataSource(
    private val healthDataStore: HealthDataStore,
) {
    suspend fun readHeartRate(): List<HeartRateData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<HeartRateData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), lastDay.toLocalDateTime())
        val readRequest = DataTypes.HEART_RATE.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()
        val heartRateList = healthDataStore.readData(readRequest).dataList

        heartRateList.forEach { session ->
            val bpm = session.getValue(DataType.HeartRateType.HEART_RATE)?.toLong() ?: return@forEach
            val time = session.startTime
            sessions.add(
                HeartRateData(
                    uid = session.uid,
                    startTime = session.startTime,
                    endTime = session.endTime!!,
                    samples = listOf(
                        HeartRateSample(time, bpm)
                    )
                )
            )
        }

        return sessions
    }
}