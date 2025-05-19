package com.hdil.rebloomlens.common.model

import java.time.Instant

data class HeartRateData(
    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val samples: List<HeartRateSample> = listOf(),
)