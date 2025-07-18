package com.hdil.rebloomlens.manualInput_plugins.likert_scale

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import org.json.JSONObject

class LikertScalePlugin(
    override val pluginId: String,
    override val config: JSONObject
) : Plugin {

    override fun initialize(context: Context) {
        Logger.i("[$pluginId] Initialized with config: ${config}")
    }

    @Composable
    override fun renderUI() {
        val context = LocalContext.current

        // Mode switch: questionnaire or single-question
        if (config.has("questions")) {
            RenderQuestionnaireUI()
        } else {
            RenderSimpleLikertUI()
        }
    }

    @Composable
    fun RenderSimpleLikertUI() {
        val title = config.optString("title", "Untitled")
        val scaleArray = config.optJSONArray("scale")
        val scale: List<String> = if (scaleArray != null) List(scaleArray.length()) { scaleArray.getString(it) } else emptyList()

        var selectedValue by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // ✅ Use the reusable animated Likert scale options
            AnimatedLikertScaleOptions(
                scaleValues = scale,
                selectedValue = selectedValue,
                onSelect = {
                    selectedValue = it
                    Logger.i("[$pluginId] $title -> $selectedValue")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Logger.i("[$pluginId] Final response: $selectedValue")
                },
                enabled = selectedValue.isNotEmpty()
            ) {
                Text("제출")
            }
        }
    }

    @Composable
    fun RenderQuestionnaireUI() {
        val explanation: String? = config.optString("explanation", null.toString())
        val questionsJson = config.optJSONArray("questions")

        val prefix = config.optString("question_prefix", null.toString())
        val prefixRange = config.optJSONArray("question_prefix_range")?.let {
            if (it.length() == 2) it.getInt(0)..it.getInt(1) else null
        }

        if (questionsJson == null || questionsJson.length() == 0) {
            Text("No questions found.")
            return
        }

        var currentQuestionIndex by remember { mutableStateOf(-1) } // -1 = intro screen
        val responses = remember { mutableStateMapOf<Int, String>() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Show explanation if present
            if (currentQuestionIndex == -1 && explanation != null) {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = { currentQuestionIndex = 0 }) {
                    Text("Start")
                }
                return@Column
            }

            // Load current question
            val questionObj = questionsJson.optJSONObject(currentQuestionIndex)
            if (questionObj != null) {
                val rawQuestionText = questionObj.optString("text", "Untitled Question")
                Logger.i("[$pluginId] Prefix: $prefix | Range: $prefixRange | Index: $currentQuestionIndex")
                val questionText = if (prefixRange?.contains(currentQuestionIndex) == true) {
                    "$prefix $rawQuestionText"
                } else {
                    rawQuestionText
                }

                val questionScale: List<String> = when {
                    questionObj.has("scale") -> {
                        val arr = questionObj.getJSONArray("scale")
                        List(arr.length()) { arr.getString(it) }
                    }
                    questionObj.has("scale_id") && config.has("global_scales") -> {
                        val scaleId = questionObj.getString("scale_id")
                        val globalScales = config.getJSONObject("global_scales")
                        val arr = globalScales.optJSONArray(scaleId)
                        if (arr != null) List(arr.length()) { arr.getString(it) } else emptyList()
                    }
                    else -> emptyList()
                }

                val selectedValue = responses[currentQuestionIndex] ?: ""

                Text(
                    text = "Q${currentQuestionIndex + 1}: $questionText",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AnimatedLikertScaleOptions(
                    scaleValues = questionScale,
                    selectedValue = selectedValue,
                    onSelect = { selected ->
                        responses[currentQuestionIndex] = selected
                        Logger.i("[$pluginId] ${questionText} -> $selected")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    if (currentQuestionIndex > 0 || explanation != null) {
                        Button(onClick = {
                            if (currentQuestionIndex == 0 && explanation != null) {
                                currentQuestionIndex = -1
                            } else {
                                currentQuestionIndex--
                            }
                        }) {
                            Text("Back")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val isLast = currentQuestionIndex == questionsJson.length() - 1
                    val context = LocalContext.current

                    Button(
                        onClick = {
                            if (!responses.containsKey(currentQuestionIndex)) {
                                responses[currentQuestionIndex] = selectedValue
                            }
                            if (isLast) {
                                Logger.i("[$pluginId] Final Responses: $responses")
                                Toast.makeText(context, "Completed!", Toast.LENGTH_SHORT).show()
                            } else {
                                currentQuestionIndex++
                            }
                        },
                        enabled = selectedValue.isNotEmpty()
                    ) {
                        Text(if (isLast) "Finish" else "Next")
                    }
                }
            }
        }
    }

    @Composable
    fun AnimatedLikertScaleOptions(
        scaleValues: List<String>,
        selectedValue: String,
        onSelect: (String) -> Unit
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            scaleValues.forEach { value ->
                val isSelected = (selectedValue == value)

                // Animation for background color and scale
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        Color.Transparent,
                    animationSpec = tween(durationMillis = 300)
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.03f else 1f,
                    animationSpec = tween(durationMillis = 300)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundColor)
                        .clickable { onSelect(value) }
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(value) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }

}