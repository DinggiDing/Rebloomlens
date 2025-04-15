package com.hdil.rebloomlens.sensor_plugins.health_connect.sleep

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.utils.Logger
import java.time.Duration
import java.time.Instant
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
    LazyColumn {
        items(sessions) { session ->
            SleepSessionItem(session = session)
        }
    }
}

@Composable
fun SleepSessionItem(session: SleepSessionData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.title ?: "수면 기록",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "시작: ${formatDateTime(session.startTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "종료: ${formatDateTime(session.endTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
            session.duration?.let { duration ->
                Text(
                    text = "총 수면 시간: ${formatDuration(duration)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (session.stages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "수면 단계",
                    style = MaterialTheme.typography.titleSmall
                )
                session.stages.forEach { stage ->
                    val stageDuration = Duration.between(stage.startTime, stage.endTime)
                    Text(
                        text = "${stage.stage}: ${formatDuration(stageDuration)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun formatDateTime(instant: Instant): String {
    return DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = (duration.toMinutes() % 60)
    return "${hours}시간 ${minutes}분"
}