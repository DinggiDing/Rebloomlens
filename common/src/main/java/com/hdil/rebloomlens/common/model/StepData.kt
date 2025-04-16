package com.hdil.rebloomlens.common.model

import java.time.Instant

data class StepData(
    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val stepCount: Long
)