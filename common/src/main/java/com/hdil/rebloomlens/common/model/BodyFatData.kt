package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.units.Percentage
import java.time.Instant

data class BodyFatData(
    val uid: String,
    val time: Instant,
    val bodyFatPercentage: Percentage
)