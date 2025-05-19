package com.hdil.rebloomlens.common.model

import java.time.Instant

data class HeartRateSample(
    val time: Instant,
    val beatsPerMinute: Long
)