package com.hdil.rebloomlens.samsunghealth_data.sleep

import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.SleepStage
import com.hdil.rebloomlens.common.model.SleepStageType
import com.hdil.rebloomlens.common.utils.Logger
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


class SleepDataSource(
    private val healthDataStore: HealthDataStore
) {
    suspend fun readSleep(): List<SleepSessionData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<SleepSessionData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), LocalDateTime.ofInstant(Instant.now(), firstDay.zone))
        val readRequest = DataTypes.SLEEP.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()
        val sleepList = healthDataStore.readData(readRequest).dataList

        sleepList.forEach { session ->
            val duration : Duration = session.getValue(DataType.SleepType.DURATION) ?: return@forEach
            val sessionData = session.getValue(DataType.SleepType.SESSIONS) ?: return@forEach
            val sleepScore: Int? = session.getValue(DataType.SleepType.SLEEP_SCORE)?.toInt()

            val stages = mutableListOf<SleepStage>()
            if (sessionData is List<*>) {
                sessionData.forEach { stageData ->
                    stageData?.stages?.forEach { stage ->
                        stages.add(
                            SleepStage(
                                startTime = stage.startTime,
                                endTime = stage.endTime,
                                stage = mapSleepStageType(stage.stage)
                            )
                        )
                    }
                }
            } else {
                Logger.e("수면 세션 ID: ${session.uid} - 예상치 못한 세션 데이터 형식: ${sessionData.javaClass.name}")
            }
            sessions.add(
                SleepSessionData(
                    uid = session.uid,
                    startTime = session.startTime,
                    endTime = session.endTime!!,
                    duration = duration,
                    score = sleepScore,
                    stages = stages
                )
            )
        }
        return sessions
    }

    private fun mapSleepStageType(stage: DataType.SleepType.StageType): SleepStageType {
        return when (stage) {
            DataType.SleepType.StageType.AWAKE -> SleepStageType.AWAKE
            DataType.SleepType.StageType.LIGHT -> SleepStageType.LIGHT
            DataType.SleepType.StageType.DEEP -> SleepStageType.DEEP
            DataType.SleepType.StageType.REM -> SleepStageType.REM
            DataType.SleepType.StageType.UNDEFINED -> SleepStageType.UNKNOWN
            else -> SleepStageType.UNKNOWN
        }
    }
}