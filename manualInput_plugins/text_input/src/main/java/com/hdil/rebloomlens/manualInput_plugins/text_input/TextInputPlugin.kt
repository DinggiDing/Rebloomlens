package com.hdil.rebloomlens.manualInput_plugins.text_input

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
        var textState by remember { mutableStateOf(TextFieldValue("")) }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = config.optString("title", "텍스트 입력"), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = textState,
                onValueChange = { textState = it },
                placeholder = { Text(text = placeholder) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                Logger.i("[$pluginId] Submitted: ${textState.text}")
            }) {
                Text("제출")
            }
        }
    }


}