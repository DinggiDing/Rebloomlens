package com.hdil.rebloomlens.sensor_plugins.health_connect.bloodglucose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.hdil.rebloomlens.common.model.BloodGlucoseData
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.utils.DateTimeUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ROLE : This file is responsible for displaying a list of blood glucose data.
/**
 * Displays a list of blood glucose data.
 *
 * @param bloodglucoses The list of blood glucose data to display.
 */

@Composable
fun BloodGlucoseList(bloodglucoses: List<BloodGlucoseData>) {
    Column {
        Text(
            text = "혈당 수치",
            style = MaterialTheme.typography.titleMedium
        )
        LazyColumn {
            items(bloodglucoses) { bloodglucose ->
                BloodGlucoseItem(bloodglucose = bloodglucose)
            }
        }
    }
}

@Composable
fun BloodGlucoseItem(bloodglucose: BloodGlucoseData) {
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
                text = "${DateTimeUtils.formatDateTime(bloodglucose.time)} 혈당 기록",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "혈당 수치: ${bloodglucose.level} mg/dL",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}