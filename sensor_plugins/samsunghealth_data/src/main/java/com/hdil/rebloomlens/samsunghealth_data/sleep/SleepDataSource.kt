package com.hdil.rebloomlens.samsunghealth_data.sleep

import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.SleepStage
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.toKotlinDuration

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
            val duration : Duration = session.getValue(DataType.SleepType.DURATION) ?: return@forEach

            val stagesList = mutableListOf<SleepStage>()
            val stagesData = session.getValue(DataType.SleepType.StageType) as? List<*>
            val stages = session.getValue(DataType.SleepType.StageType.) ?: listOf()

            sessions.add(
                SleepSessionData(
                    uid = session.uid,
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration,
                    stages = stages,
                    score = session.getValue(DataType.SleepType.SLEEP_SCORE),
                )
            )
        }
    }

}