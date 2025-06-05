package com.hdil.rebloomlens.sensor_plugins.health_connect.bloodglucose

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
import com.hdil.rebloomlens.common.model.BloodGlucoseData
import com.hdil.rebloomlens.common.model.toIntValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ROLE : This file is responsible for displaying a list of blood glucose data.
/**
 * Displays a list of blood glucose data.
 *
 * @param bloodglucoses The list of blood glucose data to display.
 */

@Composable
fun BloodGlucoseList(bloodGlucoses: List<BloodGlucoseData>) {
    val dailyBloodGlucoses = remember(bloodGlucoses) {
        bloodGlucoses.groupBy {
            Instant.ofEpochMilli(it.time.toEpochMilli()).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry ->
            entry.value.map { it.level.toIntValue() }.average().toInt()
        }.toList().sortedByDescending { it.first }
    }

    val avgBloodGlucose = if (dailyBloodGlucoses.isNotEmpty()) {
        dailyBloodGlucoses.map { it.second }.average()
    } else 0
    val maxBloodGlucose = if (dailyBloodGlucoses.isNotEmpty()) {
        dailyBloodGlucoses.map { it.second }.maxOrNull() ?: 0
    } else 0
    val minBloodGlucose = if (dailyBloodGlucoses.isNotEmpty()) {
        dailyBloodGlucoses.map { it.second }.minOrNull() ?: 0
    } else 0
    val lastSynced = bloodGlucoses.maxByOrNull { it.time }?.time ?: Instant.now()

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
                    text = "혈당 데이터",
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
                    text = "혈당 요약",
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
                BloodGlucoseStatItem(value = avgBloodGlucose.toInt(), label = "평균")
                BloodGlucoseStatItem(value = maxBloodGlucose.toInt(), label = "최대")
                BloodGlucoseStatItem(value = minBloodGlucose.toInt(), label = "최소")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 일별 데이터
            dailyBloodGlucoses.forEach { (date, bloodGlucose) ->
                MinimalBloodGlucoseDataItem(
                    date = date,
                    bloodGlucose = bloodGlucose
                )
            }
        }
    }
}

@Composable
fun BloodGlucoseStatItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%,d".format(value),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF9C27B0)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MinimalBloodGlucoseDataItem(date: LocalDate, bloodGlucose: Int) {
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
                text = "🩸",
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
                text = "%,d".format(bloodGlucose),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "mg/dL",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9C27B0).copy(alpha = 0.7f),
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