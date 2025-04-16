package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.units.Mass
import java.time.Instant

data class WeightData(
    val uid: String,
    val time: Instant,
    val weight: Mass,
)