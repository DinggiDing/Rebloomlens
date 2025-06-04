package com.hdil.rebloomlens.sensor_plugins.health_connect.sleep

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
import com.hdil.rebloomlens.common.model.SleepSessionData
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ROLE : This file is responsible for displaying a list of sleep sessions.
/**
 * Displays a list of sleep sessions.
 *
 * @param sessions The list of sleep sessions to display.
 */

@Composable
fun SleepSessionsList(sessions: List<SleepSessionData>) {
    // 일자별 수면 정보 그룹화
    val dailySleeps = remember(sessions) {
        sessions.groupBy {
            Instant.ofEpochMilli(it.startTime.toEpochMilli()).atZone(ZoneId.systemDefault()).toLocalDate()
        }.toList().sortedByDescending { it.first }
    }

    // 통계
    val avgSleepMinutes = if (sessions.isNotEmpty()) {
        sessions.mapNotNull { it.duration }.map { it.toMinutes() }.average().toLong()
    } else 0L

    val maxSleepMinutes = sessions.mapNotNull { it.duration }.maxOfOrNull { it.toMinutes() } ?: 0L
    val minSleepMinutes = if (sessions.isNotEmpty()) {
        sessions.mapNotNull { it.duration }.minOfOrNull { it.toMinutes() } ?: 0L
    } else 0L
    val lastSynced = sessions.maxByOrNull { it.endTime }?.endTime ?: Instant.now()

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
                    text = "수면 데이터",
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
                    text = "수면 요약",
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
                SleepStatItem(minutes = avgSleepMinutes, label = "평균")
                SleepStatItem(minutes = maxSleepMinutes, label = "최대")
                SleepStatItem(minutes = minSleepMinutes, label = "최소")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 일별 데이터
            dailySleeps.forEach { (date, sessionsForDay) ->
                // 하루의 총 수면 시간 계산
                val totalDuration = sessionsForDay.mapNotNull { it.duration }
                    .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }

                MinimalSleepDataItem(
                    date = date,
                    duration = totalDuration,
                    sessionsCount = sessionsForDay.size
                )
            }
        }
    }
}

@Composable
fun SleepStatItem(minutes: Long, label: String) {
    val hours = minutes / 60
    val mins = minutes % 60

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${hours}h ${mins}m",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF2196F3)  // 파란색 테마
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MinimalSleepDataItem(date: LocalDate, duration: Duration, sessionsCount: Int) {
    val hours = duration.toHours()
    val minutes = (duration.toMinutes() % 60)

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
                text = "😴",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            if (sessionsCount > 1) {
                Text(
                    text = " (${sessionsCount}회)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${hours}h ${minutes}m",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )
        }
    }
    Divider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = (duration.toMinutes() % 60)
    return "${hours}시간 ${minutes}분"
}