package com.hdil.rebloomlens.likert_scale

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import org.json.JSONObject
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
        val scale = config.getJSONArray("scale")
        val responses = remember { mutableStateOf(mutableMapOf<String, Int>()) }

        Column(modifier = Modifier.padding(16.dp)) {
            for (i in 0 until questions.length()) {
                val question = questions.getString(i)
                var selectedValue by remember { mutableStateOf(scale.getInt(0)) }

                Text(text = question, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (j in 0 until scale.length()) {
                        val scaleValue = scale.getInt(j)
                        RadioButton(
                            selected = (selectedValue == scaleValue),
                            onClick = {
                                selectedValue = scaleValue
                                responses.value[question] = selectedValue
                                Logger.i("[$pluginId] $question -> $selectedValue")
                            }
                        )
                        Text(text = "$scaleValue")
                    }
                }
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