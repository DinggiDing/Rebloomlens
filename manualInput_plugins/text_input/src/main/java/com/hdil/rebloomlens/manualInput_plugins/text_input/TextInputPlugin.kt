package com.hdil.rebloomlens.manualInput_plugins.text_input

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.test.services.storage.file.PropertyFile.Column
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import org.json.JSONObject

class TextInputPlugin(
    override val pluginId: String,
    override val config: JSONObject
): Plugin {
    override fun initialize(context: Context) {
        Logger.i("[$pluginId] Initialized with config: ${config.toString()}")
    }

    @Composable
    override fun renderUI() {
        val placeholder = config.optString("placeholder", "입력하세요...")
        var textState by remember { mutableStateOf("") }
        var hours by remember { mutableStateOf("") }
        var minutes by remember { mutableStateOf("") }

        val inputType = config.optString("inputType", "text")
        val min = config.optInt("min", Int.MIN_VALUE)
        val max = config.optInt("max", Int.MAX_VALUE)
        val mode = config.optString("mode", "single")
        val context = LocalContext.current

        val isNumberOnly = inputType == "number"
        val keyboardType = if (isNumberOnly) KeyboardType.Number else KeyboardType.Text
        val isDurationMode = mode == "duration"

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = config.optString("title", "텍스트 입력"), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (isDurationMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = hours,
                        onValueChange = {
                            hours = it.filter { c -> c.isDigit() }
                        },
                        label = { Text("시간") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = minutes,
                        onValueChange = {
                            minutes = it.filter { c -> c.isDigit() }
                        },
                        label = { Text("분") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            } else {
                TextField(
                    value = textState,
                    onValueChange = {
                        textState = if (isNumberOnly) {
                            it.filter { char -> char.isDigit() }
                        } else {
                            it
                        }
                    },
                    placeholder = { Text(text = placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = isNumberOnly
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                if (isDurationMode) {
                    val h = hours.toIntOrNull()
                    val m = minutes.toIntOrNull()

                    if (h == null || h !in 0..23 || m == null || m !in 0..59) {
                        Toast.makeText(context, "0~23시간 / 0~59분 사이로 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    Logger.i("[$pluginId] Submitted duration: ${h}시간 ${m}분")
                } else {
                    val value = textState.toIntOrNull()
                    if (isNumberOnly && (value == null || value < min || value > max)) {
                        Toast.makeText(context, "${min}부터 $max 사이의 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    Logger.i("[$pluginId] Submitted: $textState")
                }
            }) {
                Text("제출")
            }

        }
    }


}