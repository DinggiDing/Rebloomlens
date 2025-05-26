package com.hdil.rebloomlens.samsunghealth_data.sleep

import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.SleepStage
import com.hdil.rebloomlens.common.utils.Logger
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


// TODO: 수면 stage 데이터 처리 개선

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
        Logger.e("수면 데이터 조회 결과: ${sleepList.size}개의 세션 발견")

        sleepList.forEach { session ->
            // TODO: Handle null values for startTime and endTime
            val startTime = session.startTime
            val endTime = session.endTime!!
            val duration : Duration = session.getValue(DataType.SleepType.DURATION) ?: return@forEach

            val stagesList = mutableListOf<SleepStage>()
//            val stagesData = session.getValue(DataType.SleepType.SESSIONS) as? List<*>
//            Logger.e("Stages Data: $stagesData")


            // SESSIONS 키로 시도
            val sessionData = session.getValue(DataType.SleepType.SESSIONS)
            Logger.e("SESSIONS 데이터: $sessionData, 타입: ${sessionData?.javaClass?.name}")

            sessionData.let { stagesData ->
                if (stagesData is List<*>) {
                    stagesData.forEach { stage ->
                        if (stage is SleepStage) {
                            stagesList.add(stage)
                            Logger.e("수면 단계: ${stage.stage}, 시작: ${stage.startTime}, 종료: ${stage.endTime}")
                        } else {
                            Logger.e("알 수 없는 단계 데이터: $stage")
                        }
                    }
                } else {
                    Logger.e("SESSIONS 데이터가 예상한 형식이 아닙니다: $stagesData")
                }
            }

        }
        return sessions

    }
}