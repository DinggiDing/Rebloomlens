package com.hdil.rebloomlens.sensor_plugins.health_connect.step

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
import com.hdil.rebloomlens.common.model.StepData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ROLE : This file is responsible for displaying a list of step data.
/**
 * Displays a list of step data.
 *
 * @param steps The list of step data to display.
 */

//@Composable
//fun StepsList(steps: List<StepData>) {
//    Column {
//        Text(
//            text = "걸음 수",
//            style = MaterialTheme.typography.titleMedium
//        )
//        LazyColumn {
//            items(steps) { step ->
//                StepDataItem(step = step)
//            }
//        }
//    }
//}
//
//@Composable
//fun StepDataItem(step: StepData) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "${DateTimeUtils.formatDateTime(step.startTime)} 걸음 기록",
//                style = MaterialTheme.typography.titleMedium
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "걸음 수: ${step.stepCount}걸음",
//                style = MaterialTheme.typography.bodyLarge
//            )
//
//            Text(
//                text = "시작: ${DateTimeUtils.formatDateTime(step.startTime)}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Text(
//                text = "종료: ${DateTimeUtils.formatDateTime(step.endTime)}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//    }
//}


@Composable
fun StepsList(steps: List<StepData>) {
    // 일자별 합계
    val dailySteps = remember(steps) {
        steps.groupBy {
            Instant.ofEpochMilli(it.startTime.toEpochMilli()).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { entry ->
            entry.value.sumOf { it.stepCount }
        }.toList().sortedByDescending { it.first }
    }

    // 통계
    val avgSteps = if (dailySteps.isNotEmpty()) dailySteps.map { it.second }.average().toInt() else 0
    val maxSteps = dailySteps.maxOfOrNull { it.second } ?: 0
    val minSteps = if (dailySteps.isNotEmpty()) dailySteps.minOf { it.second } else 0
    val lastSynced = steps.maxByOrNull { it.startTime }?.startTime ?: Instant.now()

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
                    text = "걸음 데이터",
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
                    text = "걸음 요약",
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
                StepStatItem(value = avgSteps, label = "평균")
                StepStatItem(value = maxSteps.toInt(), label = "최대")
                StepStatItem(value = minSteps.toInt(), label = "최소")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 일별 데이터
            dailySteps.forEach { (date, stepCount) ->
                MinimalStepDataItem(
                    date = date,
                    stepCount = stepCount.toInt()
                )
            }
        }
    }
}

@Composable
fun StepStatItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%,d".format(value),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF4CAF50)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MinimalStepDataItem(date: LocalDate, stepCount: Int) {
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
                text = "👣",
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
                text = "%,d".format(stepCount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "steps",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50).copy(alpha = 0.7f),
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