package com.hdil.rebloomlens.manualInput_plugins.likert_scale

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import org.json.JSONObject

class LikertScalePlugin(
    override val pluginId: String,
    override val config: JSONObject
): Plugin {
    override fun initialize(context: Context) {
        Logger.i("[$pluginId] Initialized with config: ${config.toString()}")
    }

    @Composable
    override fun renderUI() {
        val title = config.getString("title")
        var selectedValue by remember { mutableStateOf("") }
        val response = remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold)

            // config에 scale이 있는지 확인
            if (config.has("scale")) {
                val scale = config.getJSONArray("scale")

                // 기본값 설정 (아무것도 선택되지 않은 상태)
                if (selectedValue.isEmpty() && scale.length() > 0) {
                    selectedValue = scale.getString(0)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (j in 0 until scale.length()) {
                        val scaleValue = scale.getString(j)

                        Row(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .wrapContentWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedValue == scaleValue),
                                onClick = {
                                    selectedValue = scaleValue
                                    response.value = selectedValue
                                    Logger.i("[$pluginId] $title -> $selectedValue")
                                }
                            )
                            Text(
                                text = scaleValue,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .wrapContentWidth()
                            )
                        }
                    }
                }
            } else {
                Text("설정된 리커트 스케일이 없습니다.")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Logger.i("[$pluginId] Response for $title: $selectedValue")
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("제출")
            }
        }
    }

}