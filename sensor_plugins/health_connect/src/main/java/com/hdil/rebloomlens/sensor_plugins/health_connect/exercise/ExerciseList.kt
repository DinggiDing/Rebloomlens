package com.hdil.rebloomlens.sensor_plugins.health_connect.exercise

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
import com.hdil.rebloomlens.common.model.ExerciseData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseList(exercises: List<ExerciseData>) {
    val dailyExercises = remember(exercises) {
        exercises.groupBy {
            Instant.ofEpochMilli(it.startTime.toEpochMilli())
                .atZone(ZoneId.systemDefault()).toLocalDate()
        }.toList().sortedByDescending { it.first }
    }

    val avgDuration = if (exercises.isNotEmpty()) {
        exercises.map { (it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 1000 }
            .average().toLong()
    } else 0L
    val maxDuration = if (exercises.isNotEmpty()) {
        exercises.maxOfOrNull { (it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 1000 } ?: 0L
    } else 0L
    val minDuration = if (exercises.isNotEmpty()) {
        exercises.minOfOrNull { (it.endTime.toEpochMilli() - it.startTime.toEpochMilli()) / 1000 } ?: 0L
    } else 0L
    val lastSynced = exercises.maxByOrNull { it.endTime }?.endTime ?: Instant.now()

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "운동 데이터",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "운동 요약",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExerciseStatItem(value = avgDuration, label = "평균")
                ExerciseStatItem(value = maxDuration, label = "최대")
                ExerciseStatItem(value = minDuration, label = "최소")
            }

            Spacer(modifier = Modifier.height(20.dp))

            dailyExercises.forEach { (data, exerciseAvg) ->
                val totalDuration = exerciseAvg.fold(0L) { acc, exercise ->
                    acc + (exercise.endTime.toEpochMilli() - exercise.startTime.toEpochMilli()) / 1000
                }
                // 가장 많은 운동 타입 찾기
                val mostCommonExerciseType = exerciseAvg
                    .groupBy { it.exerciseType }
                    .maxByOrNull { (_, exercises) -> exercises.size }
                    ?.key ?: "-"

                MinimalExerciseItem(
                    date = data,
                    avgDuration = totalDuration,
                    exercises = mostCommonExerciseType
                )
            }
        }
    }
}

@Composable
fun ExerciseStatItem(value: Long, label: String) {
    val hours = value / 3600
    val min = (value % 3600) / 60

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${hours}h ${min}m",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color(0xFF795548)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MinimalExerciseItem(
    date: LocalDate,
    exercises: String,
    avgDuration: Long
) {
    val hours = avgDuration / 3600
    val minutes = (avgDuration % 3600) / 60

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
                text = "🏃",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.weight(1f))

            Column {
                Text(
                    text = "${hours}h ${minutes}m",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF795548)
                )
                Text(
                    text = " 가장 많은 운동: ${exercises})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
    Divider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}