package com.hdil.rebloomlens.sensor_plugins.health_connect.bodyfat

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
import com.hdil.rebloomlens.common.model.BodyFatData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun BodyFatList(bodyFats: List<BodyFatData>) {
    val dailyBodyFats = remember(bodyFats) {
        bodyFats.groupBy {
            Instant.ofEpochMilli(it.time.toEpochMilli()).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry ->
            entry.value.map { it.bodyFatPercentage }
        }.toList().sortedByDescending { it.first }
    }

    val avgBodyFat = if (dailyBodyFats.isNotEmpty()) dailyBodyFats.flatMap { it.second }.average().toDouble() else 0.0
    val maxBodyFat = if (dailyBodyFats.isNotEmpty()) dailyBodyFats.flatMap { it.second }.maxOrNull() ?: 0.0 else 0.0
    val minBodyFat = if (dailyBodyFats.isNotEmpty()) dailyBodyFats.flatMap { it.second}.minOrNull() ?: 0.0 else 0.0
    val lastSynced = bodyFats.maxByOrNull { it.time }?.time ?: Instant.now()

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "체지방 데이터",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 동기화 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "체지방 요약",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "마지막 동기화: ${
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(lastSynced)
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 통계
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BodyFatStatItem(value = avgBodyFat, label = "평균")
                BodyFatStatItem(value = maxBodyFat, label = "최대")
                BodyFatStatItem(value = minBodyFat, label = "최소")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 일별 데이터
            dailyBodyFats.forEach { (date, bodyFatAvg) ->
                MinimalBodyFatDataItem(
                    date = date,
                    bodyFat = bodyFatAvg.firstOrNull()?.toDouble() ?: 0.0
                )
            }
        }
    }
}

@Composable
fun BodyFatStatItem(value: Double, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%.1f".format(value),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF009688)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MinimalBodyFatDataItem(date: LocalDate, bodyFat: Double) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊️",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "%.2f".format(bodyFat),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF009688)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF009688).copy(alpha = 0.7f),
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