package com.hdil.rebloomlens.common.model

import androidx.health.connect.client.records.HeartRateRecord
import java.time.Instant

data class HeartRateData(
    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val samples: List<HeartRateRecord.Sample> = listOf(),
)