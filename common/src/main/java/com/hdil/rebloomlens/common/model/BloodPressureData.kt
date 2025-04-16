package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.units.Pressure
import java.time.Instant

data class BloodPressureData(
    val uid: String,
    val time: Instant,
    val systolic: Pressure,
    val diastolic: Pressure,
    val bodyPosition: Int,
    val measurementLocation: Int
)

/*
- bodyPosition : unknown, standing up, sitting down, lying down, reclining
- measurementLocation : unknown, left writs, right wrist, left upper arm, right upper arm
 */