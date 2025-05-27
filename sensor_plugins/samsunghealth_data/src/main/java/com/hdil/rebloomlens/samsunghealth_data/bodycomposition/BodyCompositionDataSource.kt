package com.hdil.rebloomlens.samsunghealth_data.bodycomposition

import com.hdil.rebloomlens.common.model.BodyFatData
import com.hdil.rebloomlens.common.model.SkeletalMuscleMassData
import com.hdil.rebloomlens.common.model.WeightData
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class BodyCompositionDataSource(
    private val healthDataStore: HealthDataStore,
) {
    suspend fun readBodyFatPercentage(): List<BodyFatData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<BodyFatData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), LocalDateTime.ofInstant(Instant.now(), firstDay.zone))
        val readRequest = DataTypes.BODY_COMPOSITION.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()

        val bodyfatList = healthDataStore.readData(readRequest).dataList
        bodyfatList.forEach { session ->
            val bodyFatPercentage = session.getValue(DataType.BodyCompositionType.BODY_FAT)?.toDouble() ?: return@forEach

            sessions.add(
                BodyFatData(
                    uid = session.uid,
                    time = session.startTime,
                    bodyFatPercentage = bodyFatPercentage
                )
            )
        }
        return sessions
    }

    suspend fun readSkeletalMuscleMass(): List<SkeletalMuscleMassData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<SkeletalMuscleMassData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), lastDay.toLocalDateTime())
        val readRequest = DataTypes.BODY_COMPOSITION.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()

        val bodyCompositionList = healthDataStore.readData(readRequest).dataList
        bodyCompositionList.forEach { session ->
            val skeletalMuscleMass = session.getValue(DataType.BodyCompositionType.SKELETAL_MUSCLE_MASS)?.toDouble() ?: return@forEach

            sessions.add(
                SkeletalMuscleMassData(
                    uid = session.uid,
                    time = session.startTime,
                    skeletalMuscleMass = skeletalMuscleMass
                )
            )
        }
        return sessions
    }

    suspend fun readWeight(): List<WeightData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<WeightData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), lastDay.toLocalDateTime())
        val readRequest = DataTypes.BODY_COMPOSITION.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()

        val bodyCompositionList = healthDataStore.readData(readRequest).dataList
        bodyCompositionList.forEach { session ->
            val weight = session.getValue(DataType.BodyCompositionType.WEIGHT)?.toDouble() ?: return@forEach
            sessions.add(
                WeightData.fromSamsung(
                    uid = session.uid,
                    time = session.startTime,
                    weightKgFloat = weight
                )
            )
        }
        return sessions
    }
}