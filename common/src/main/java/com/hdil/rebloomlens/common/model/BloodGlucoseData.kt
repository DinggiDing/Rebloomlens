package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.units.BloodGlucose
import java.time.Instant

data class BloodGlucoseData(
    val uid: String,
    val time: Instant,
    val level: BloodGlucose,
    val specimenSource: Int,
    val mealType: Int,
    val relationToMeal: Int
)

fun BloodGlucose.toIntValue(): Int {
    // mmol/L 단위로 값을 가져와 정수로 변환
    return this.inMillimolesPerLiter.toInt()
}

/*
- mealType : breakfast, launch, dinner, snack, unknown
- level : Blood glucose level or concentration. Required field. Valid range: 0-50 mmol/L.
 */