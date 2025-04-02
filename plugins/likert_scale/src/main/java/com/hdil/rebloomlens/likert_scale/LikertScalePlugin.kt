package com.hdil.rebloomlens.likert_scale

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import org.json.JSONObject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class LikertScalePlugin(
    override val pluginId: String,
    override val config: JSONObject
): Plugin {
    override fun initialize(context: Context) {
        Logger.i("[$pluginId] Initialized with config: ${config.toString()}")
    }

    @Composable
    override fun renderUI() {
        val questions = config.getJSONArray("questions")
        val responses = remember { mutableStateOf(mutableMapOf<String, String>()) }

        Column(modifier = Modifier.padding(16.dp)) {
            for (i in 0 until questions.length()) {
                val questionObj = questions.getJSONObject(i)
                val questionText = questionObj.getString("text")
                val scale = questionObj.getJSONArray("scale")
                var selectedValue by remember { mutableStateOf(scale.getString(0)) }

                Text(text = questionText, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically  // 중앙 정렬 추가
                ) {
                    for (j in 0 until scale.length()) {
                        val scaleValue = scale.getString(j)

                        Row(
                            modifier = Modifier.padding(end=8.dp)
                                .wrapContentWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedValue == scaleValue),
                                onClick = {
                                    selectedValue = scaleValue
                                    responses.value[questionText] = selectedValue
                                    Logger.i("[$pluginId] $questionText -> $selectedValue")
                                }
                            )
                            Text(
                                text = "$scaleValue",
                                modifier = Modifier.padding(top = 4.dp)
                                    .wrapContentWidth()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    Logger.i("[$pluginId] Responses: ${responses.value}")
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("제출")
            }
        }
    }

}