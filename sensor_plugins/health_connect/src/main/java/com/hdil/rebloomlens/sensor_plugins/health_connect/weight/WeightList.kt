package com.hdil.rebloomlens.sensor_plugins.health_connect.weight

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.model.WeightData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items
import com.hdil.rebloomlens.common.utils.DateTimeUtils

// ROLE : This file is responsible for displaying a list of weight data.
/**
 * Displays a list of weight data.
 *
 * @param weight The list of weight data to display.
 */

@Composable
fun WeightList(weights: List<WeightData>) {
    Column {
        Text(
            text = "체중",
            style = MaterialTheme.typography.titleMedium
        )
        LazyColumn {
            items(weights) { weight ->
                WeightItem(weight = weight)
            }
        }
    }
}

@Composable
fun WeightItem(weight: WeightData) {
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
                text = "${DateTimeUtils.formatDateTime(weight.time)} 체중 기록",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "체중: ${weight.weight}kg",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}