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

/*
- mealType : breakfast, launch, dinner, snack, unknown
- level : Blood glucose level or concentration. Required field. Valid range: 0-50 mmol/L.
 */