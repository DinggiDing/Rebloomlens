package com.hdil.rebloomlens.samsunghealth_data.bloodpressure

import com.hdil.rebloomlens.common.model.BloodPressureData
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class BloodPressureDataSource(
    private val healthDataStore: HealthDataStore
) {
    suspend fun readBloodPressure(): List<BloodPressureData> {
        val lastDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val firstDay = lastDay.minusDays(30)

        val sessions = mutableListOf<BloodPressureData>()

        val localTimeFilter = LocalTimeFilter.of(firstDay.toLocalDateTime(), LocalDateTime.ofInstant(Instant.now(), firstDay.zone))
        val readRequest = DataTypes.BLOOD_PRESSURE.readDataRequestBuilder
            .setLocalTimeFilter(localTimeFilter)
            .setOrdering(Ordering.DESC)
            .build()
        val bloodPressureList = healthDataStore.readData(readRequest).dataList
        bloodPressureList.forEach { session ->
            val systolic = session.getValue(DataType.BloodPressureType.SYSTOLIC)?.toDouble() ?: return@forEach
            val diastolic = session.getValue(DataType.BloodPressureType.DIASTOLIC)?.toDouble() ?: return@forEach

            sessions.add(
                BloodPressureData(
                    uid = session.uid,
                    time = session.startTime,
                    systolic = systolic,
                    diastolic = diastolic,
                )
            )
        }
        return sessions

    }
}