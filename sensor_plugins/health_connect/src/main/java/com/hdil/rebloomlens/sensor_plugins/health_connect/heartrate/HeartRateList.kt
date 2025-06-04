package com.hdil.rebloomlens.sensor_plugins.health_connect.heartrate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.rebloomlens.common.model.HeartRateData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HeartRateList(heartRates: List<HeartRateData>) {
    val dailyHeartRates = remember(heartRates) {
        heartRates.groupBy {
            Instant.ofEpochMilli(it.startTime.toEpochMilli()).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry ->
            entry.value.map { it.samples.first().beatsPerMinute }.average().toInt()
        }.toList().sortedByDescending { it.first }
    }

    // 통계
    val avgHeartRates = if (dailyHeartRates.isNotEmpty()) dailyHeartRates.map { it.second }.average().toInt() else 0
    val maxHeartRates = dailyHeartRates.maxOfOrNull { it.second } ?: 0
    val minHeartRates = if (dailyHeartRates.isNotEmpty()) dailyHeartRates.minOf { it.second } else 0
    val lasySynced = heartRates.maxByOrNull { it.startTime }?.startTime ?: Instant.now()

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "심박수 데이터",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "심박수 요약",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "마지막 동기화: ${
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(lasySynced)
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                HeartRateStatItem(value = avgHeartRates, label = "평균")
                HeartRateStatItem(value = maxHeartRates.toInt(), label = "최대")
                HeartRateStatItem(value = minHeartRates.toInt(), label = "최소")
            }

            Spacer(modifier = Modifier.height(20.dp))

            dailyHeartRates.forEach { (date, heartRateCount) ->
                MinimalHeartRateDataItem(
                    date = date,
                    heartRateCount = heartRateCount.toInt()
                )
            }
        }
    }
}

@Composable
fun HeartRateStatItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%d".format(value),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFFE91E63)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MinimalHeartRateDataItem(date: LocalDate, heartRateCount: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "💓",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "%d".format(heartRateCount),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE91E63)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "bpm",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE91E63).copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
    }
    Divider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}