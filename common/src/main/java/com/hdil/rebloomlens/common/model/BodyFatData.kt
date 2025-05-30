package com.hdil.rebloomlens.common.model

import java.time.Instant

data class BodyFatData(
    val uid: String,
    val time: Instant,
    val bodyFatPercentage: Double
)