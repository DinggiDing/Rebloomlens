package com.hdil.rebloomlens.sensor_plugins.health_connect.step

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.model.StepData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items
import com.hdil.rebloomlens.common.utils.Logger


@Composable
fun StepsList(steps: List<StepData>) {
    Column {
        Text(
            text = "걸음 수",
            style = MaterialTheme.typography.titleMedium
        )
        LazyColumn {
            items(steps) { step ->
                StepDataItem(step = step)
            }
        }
    }
}

@Composable
fun StepDataItem(step: StepData) {
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
                text = "${formatDateTime(step.startTime)} 걸음 기록",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "걸음 수: ${step.stepCount}걸음",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "시작: ${formatDateTime(step.startTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "종료: ${formatDateTime(step.endTime)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDateTime(instant: Instant): String {
    return DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}