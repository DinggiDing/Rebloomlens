package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.units.Mass
import java.time.Instant

data class WeightData(
    val uid: String,
    val time: Instant,
    val weight: Mass,
) {
    // 1) 보조 생성자: epoch 밀리초 + kg(double) → Instant + Mass 변환
    constructor(
        uid: String,
        time: Instant,
        weightKg: Double
    ) : this(
        uid = uid,
        time = time,
        weight = Mass.kilograms(weightKg)
    )

    companion object {
        /**
         * 삼성헬스에서 읽어온 Float(kg) + epoch millis → WeightData 생성
         */
        fun fromSamsung(
            uid: String,
            time: Instant,
            weightKgFloat: Double
        ): WeightData = WeightData(
            uid = uid,
            time = time,
            weightKg = weightKgFloat
        )
    }
}