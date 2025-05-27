package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.units.Mass
import java.time.Instant

data class WeightData(
    val uid: String,
    val time: Instant,
    val weight: Mass,
)

// 3) Samsung Health SDK → WeightData 매퍼
//    (com.samsung.android.sdk.healthdata.HealthData, androidx.health.connect.client.units.Mass)
class SamsungWeightDataAdapter: HealthDataAdapter<HealthData, WeightData> {
    override fun toDomain(source: HealthData): WeightData {
        // “weight” 필드는 kg 단위 double
        val kgValue = source.getDouble("weight")
        val massValue = Mass.kilograms(kgValue)

        // “measurement_time” 필드는 epoch millis
        val timeMs = source.getLong("measurement_time")
        val instant = Instant.ofEpochMilli(timeMs)

        // 데이터 고유 식별자(_ID 상수는 SDK마다 다를 수 있음)
        val uid = source.getString(HealthData._ID)

        return WeightData(
            uid = uid,
            time = instant,
            weight = massValue
        )
    }
}