package com.hdil.voice_input

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import org.json.JSONObject

class VoiceInputPlugin(
    override val pluginId: String,
    override val config: JSONObject
) : Plugin {
    private lateinit var context: Context
    private lateinit var recognizerManager: SpeechRecognizerManager
    private val viewModel = VoiceInputViewModel()
    private val hasPermission = mutableStateOf(false)

    override fun initialize(context: Context) {
        this.context = context
        recognizerManager = SpeechRecognizerManager(context)
        viewModel.initialize(context)
        // 초기화 시점에 권한 체크
        hasPermission.value = checkAudioPermission()
        Logger.i("[$pluginId] Initialized with config: ${config.toString()}, 권한 상태: ${hasPermission.value}")
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onDestroy() {
        recognizerManager.destroy()
    }

    @Composable
    override fun renderUI() {
        val vm = remember { viewModel }
        val currentContext = LocalContext.current
        val permissionState = remember { hasPermission }

        // UI가 표시될 때 권한 상태 업데이트
        LaunchedEffect(Unit) {
            permissionState.value = checkAudioPermission()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!permissionState.value) {
                Text("음성 인식을 사용하려면 마이크 권한이 필요합니다.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (currentContext is Activity) {
                        PermissionHandler.requestAudioPermission(currentContext, 1001)
                    } else {
                        Logger.e("[$pluginId] 권한 요청을 위한 Activity 컨텍스트가 필요합니다.")
                    }
                }) {
                    Text("권한 요청")
                }
            } else {
                VoiceInputScreen(vm)
            }
        }
    }

    inner class VoiceInputViewModel {
        var recognizedText = mutableStateOf("")
            private set
        var isListening = mutableStateOf(false)
            private set

        fun initialize(context: Context) {
            // 뷰모델 초기화
        }

        fun startListening() {
            if (hasPermission.value) {
                recognizedText.value = "듣는 중..."
                isListening.value = true

                recognizerManager.startListening(RecognitionListenerImpl(
                    onResult = { result ->
                        recognizedText.value = result
                        isListening.value = false
                        Logger.i("[$pluginId] 최종 인식 결과: $result")
                    },
                    onPartialResult = { partialText ->
                        // 부분 결과를 UI에 즉시 표시
                        recognizedText.value = "[$partialText]"
                        Logger.e("[$pluginId] 인식 중: $partialText")
                    },
                    onError = { errorCode ->
                        isListening.value = false
                        when (errorCode) {
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                recognizedText.value = "마이크 권한이 필요합니다"
                                // 권한 상태 갱신
                                hasPermission.value = false
                            }
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                recognizedText.value = "인식 엔진이 사용 중입니다. 잠시 후 다시 시도해주세요"
                                // 재초기화 시도
                                recognizerManager.destroy()
                                recognizerManager = SpeechRecognizerManager(context)
                            }
                            SpeechRecognizer.ERROR_CLIENT -> {
                                recognizedText.value = "음성 인식 초기화에 문제가 있습니다. 앱을 재시작해 주세요"
                                // 재초기화 시도
                                recognizerManager.destroy()
                                recognizerManager = SpeechRecognizerManager(context)
                            }
                            else -> {
                                recognizedText.value = "음성 인식 오류가 발생했습니다. 다시 시도해주세요"
                            }
                        }
                        Logger.e("[$pluginId] 음성 인식 오류: $errorCode")
                    },
                    onReadyForSpeech = {
                        // 음성 인식 준비 완료 시 UI 업데이트
                        recognizedText.value = "말씀해 주세요..."
                    }
                ))
            } else {
                Logger.e("[$pluginId] 권한 없음: 음성 인식을 시작할 수 없습니다.")
                recognizedText.value = "마이크 권한이 필요합니다."
            }
        }

        fun stopListening() {
            recognizerManager.stopListening()
            isListening.value = false
        }
    }
}

@Composable
fun VoiceInputScreen(viewModel: VoiceInputPlugin.VoiceInputViewModel) {
    val recognizedText = viewModel.recognizedText.value
    val isListening = viewModel.isListening.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (recognizedText.isEmpty()) "여기에 인식된 텍스트가 표시됩니다" else recognizedText,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isListening) {
            Button(
                onClick = { viewModel.stopListening() },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Red
                )
            ) {
                Text("음성 인식 중지")
            }
        } else {
            Button(onClick = { viewModel.startListening() }) {
                Text("음성 인식 시작")
            }
        }
    }
}