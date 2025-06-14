package com.hdil.voice_input

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdil.rebloomlens.common.plugin_interfaces.Plugin
import com.hdil.rebloomlens.common.utils.Logger
import com.hdil.voice_input.llm.OpenAIParser
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

//class VoiceInputPlugin(
//    override val pluginId: String,
//    override val config: JSONObject
//) : Plugin {
//    private lateinit var context: Context
//    private lateinit var recognizerManager: SpeechRecognizerManager
//    private val viewModel = VoiceInputViewModel()
//    private val hasPermission = mutableStateOf(false)
//
//    override fun initialize(context: Context) {
//        this.context = context
//        recognizerManager = SpeechRecognizerManager(context)
//        viewModel.initialize(context)
//        hasPermission.value = checkAudioPermission()
//        Logger.i("[$pluginId] Initialized with config: ${config.toString()}, 권한 상태: ${hasPermission.value}")
//    }
//
//    private fun checkAudioPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.RECORD_AUDIO
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    fun onDestroy() {
//        recognizerManager.destroy()
//    }
//
//    @Composable
//    override fun renderUI() {
//        val vm = remember { viewModel }
//        val currentContext = LocalContext.current
//        val permissionState = remember { hasPermission }
//
//        LaunchedEffect(Unit) {
//            permissionState.value = checkAudioPermission()
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            if (!permissionState.value) {
//                Text("음성 인식을 사용하려면 마이크 권한이 필요합니다.")
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = {
//                    if (currentContext is Activity) {
//                        PermissionHandler.requestAudioPermission(currentContext, 1001)
//                    } else {
//                        Logger.e("[$pluginId] 권한 요청을 위한 Activity 컨텍스트가 필요합니다.")
//                    }
//                }) {
//                    Text("권한 요청")
//                }
//            } else {
//                VoiceInputScreen(vm)
//            }
//        }
//    }
//
//    inner class VoiceInputViewModel : ViewModel() {
//        var recognizedText = mutableStateOf("")
//            private set
//        var isListening = mutableStateOf(false)
//            private set
//        var parsedResult = mutableStateOf<JSONObject?>(null)
//            private set
//        var isProcessing = mutableStateOf(false)
//            private set
//
//        private var openAIParser = OpenAIParser()
//
//        fun initialize(context: Context) {
//            Logger.e("[$pluginId] VoiceInputViewModel 초기화")
//            openAIParser.initialize()
//        }
//
//        fun startListening() {
//            if (hasPermission.value) {
//                recognizedText.value = "듣는 중..."
//                isListening.value = true
//
//                recognizerManager.startListening(RecognitionListenerImpl(
//                    onResult = { result ->
//                        recognizedText.value = result
//                        isListening.value = false
//                        Logger.i("[$pluginId] 최종 인식 결과: $result")
//                    },
//                    onPartialResult = { partialText ->
//                        recognizedText.value = "[$partialText]"
//                        Logger.e("[$pluginId] 인식 중: $partialText")
//                    },
//                    onError = { errorCode ->
//                        isListening.value = false
//                        when (errorCode) {
//                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
//                                recognizedText.value = "마이크 권한이 필요합니다"
//                                hasPermission.value = false
//                            }
//                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
//                                recognizedText.value = "인식 엔진이 사용 중입니다. 잠시 후 다시 시도해주세요"
//                                recognizerManager.destroy()
//                                recognizerManager = SpeechRecognizerManager(context)
//                            }
//                            SpeechRecognizer.ERROR_CLIENT -> {
//                                recognizedText.value = "음성 인식 초기화에 문제가 있습니다. 앱을 재시작해 주세요"
//                                recognizerManager.destroy()
//                                recognizerManager = SpeechRecognizerManager(context)
//                            }
//                            else -> {
//                                recognizedText.value = "음성 인식 오류가 발생했습니다. 다시 시도해주세요"
//                            }
//                        }
//                        Logger.e("[$pluginId] 음성 인식 오류: $errorCode")
//                    },
//                    onReadyForSpeech = {
//                        recognizedText.value = "말씀해 주세요..."
//                    }
//                ))
//            } else {
//                Logger.e("[$pluginId] 권한 없음: 음성 인식을 시작할 수 없습니다.")
//                recognizedText.value = "마이크 권한이 필요합니다."
//            }
//        }
//
//        fun stopListening() {
//            recognizerManager.stopListening()
//            isListening.value = false
//        }
//
//        fun parseRecognizedText() {
//            val text = recognizedText.value
////            if (text.isNotEmpty() && text != "듣는 중..." && text != "말씀해 주세요...") {
//                viewModelScope.launch {
//                    isProcessing.value = true
//                    val result = openAIParser.parseFoodIntake(text)
//                    parsedResult.value = result
//                    isProcessing.value = false
//                }
////            }
//        }
//    }
//}
//
//@Composable
//fun VoiceInputScreen(viewModel: VoiceInputPlugin.VoiceInputViewModel) {
//    val recognizedText = viewModel.recognizedText.value
//    val isListening = viewModel.isListening.value
//    val parsedResult = viewModel.parsedResult.value
//    val isProcessing = viewModel.isProcessing.value
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = if (recognizedText.isEmpty()) "여기에 인식된 텍스트가 표시됩니다" else recognizedText,
//            style = TextStyle(
//                fontSize = 18.sp,
//                textAlign = TextAlign.Center
//            )
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        if (isProcessing) {
//            CircularProgressIndicator()
//            Text(
//                text = "OpenAI로 분석 중...",
//                style = TextStyle(fontSize = 14.sp),
//                modifier = Modifier.padding(top = 8.dp)
//            )
//        } else if (parsedResult != null) {
//            Surface(
//                modifier = Modifier.padding(16.dp),
//                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
//                color = MaterialTheme.colorScheme.surfaceVariant,
//                shadowElevation = 2.dp
//            ) {
//                LazyColumn(modifier = Modifier.padding(16.dp)) {
//                    item {
//                        Text(
//                            text = "분석 결과",
//                            style = TextStyle(
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(text = "날짜: ${parsedResult.optString("date", "날짜 정보 없음")}")
//                        Spacer(modifier = Modifier.height(8.dp))
//                    }
//
//                    // meals 배열 처리
//                    val meals = parsedResult.optJSONArray("meals") ?: JSONArray()
//                    items(meals.length()) { index ->
//                        val meal = meals.optJSONObject(index) ?: return@items
//                        val mealType = meal.optString("mealType", "알 수 없음")
//
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                        ) {
//                            Text(
//                                text = "식사: $mealType",
//                                style = TextStyle(
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            )
//
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Divider()
//                            Spacer(modifier = Modifier.height(4.dp))
//
//                            // 식사 항목 표시
//                            val items = meal.optJSONArray("items") ?: JSONArray()
//                            for (i in 0 until items.length()) {
//                                val item = items.optJSONObject(i) ?: continue
//                                val food = item.optString("food", "알 수 없음")
//                                val quantity = item.optDouble("quantity", 0.0)
//                                val unit = item.optString("unit", "개")
//
//                                Text("• $food ${quantity}$unit")
//                            }
//                        }
//
//                        if (index < meals.length() - 1) {
//                            Divider(
//                                modifier = Modifier.padding(vertical = 8.dp),
//                                thickness = 1.dp
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // 버튼 영역
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.fillMaxWidth(0.8f)
//        ) {
//            if (isListening) {
//                Button(
//                    onClick = { viewModel.stopListening() },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = androidx.compose.ui.graphics.Color.Red
//                    ),
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("음성 인식 중지")
//                }
//            } else {
//                Button(
//                    onClick = { viewModel.startListening() },
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text("음성 인식 시작")
//                }
//
////                if (recognizedText.isNotEmpty() && recognizedText != "듣는 중..." && recognizedText != "말씀해 주세요...") {
//                    Button(
//                        onClick = { viewModel.parseRecognizedText() },
//                        enabled = !isProcessing,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("분석하기")
//                    }
////                }
//            }
//        }
//    }
//}

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

    inner class VoiceInputViewModel : ViewModel() {
        var recognizedText = mutableStateOf("")
            private set
        var isListening = mutableStateOf(false)
            private set
        var parsedResult = mutableStateOf<JSONObject?>(null)
            private set
        var isProcessing = mutableStateOf(false)
            private set
        var parseError = mutableStateOf<String?>(null)
            private set

        private var openAIParser = OpenAIParser()

        fun initialize(context: Context) {
            Logger.e("[$pluginId] VoiceInputViewModel 초기화")
            openAIParser.initialize()
        }

        fun startListening() {
            if (hasPermission.value) {
                recognizedText.value = "듣는 중..."
                isListening.value = true
                parseError.value = null

                recognizerManager.startListening(RecognitionListenerImpl(
                    onResult = { result ->
                        recognizedText.value = result
                        isListening.value = false
                        Logger.i("[$pluginId] 최종 인식 결과: $result")
                    },
                    onPartialResult = { partialText ->
                        recognizedText.value = "[$partialText]"
                        Logger.e("[$pluginId] 인식 중: $partialText")
                    },
                    onError = { errorCode ->
                        isListening.value = false
                        when (errorCode) {
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                recognizedText.value = "마이크 권한이 필요합니다"
                                hasPermission.value = false
                            }
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                recognizedText.value = "인식 엔진이 사용 중입니다. 잠시 후 다시 시도해주세요"
                                recognizerManager.destroy()
                                recognizerManager = SpeechRecognizerManager(context)
                            }
                            SpeechRecognizer.ERROR_CLIENT -> {
                                recognizedText.value = "음성 인식 초기화에 문제가 있습니다. 앱을 재시작해 주세요"
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

        fun parseRecognizedText() {
            val text = recognizedText.value
//            if (text.isNotEmpty() && text != "듣는 중..." && text != "말씀해 주세요...") {
                viewModelScope.launch {
                    isProcessing.value = true
                    parseError.value = null
                    try {
                        val result = openAIParser.parseFoodIntake(text)
                        parsedResult.value = result
                        if (result == null) {
                            parseError.value = "분석 결과를 얻지 못했습니다. 다시 시도해주세요."
                        }
                    } catch (e: Exception) {
                        parseError.value = "분석 중 오류: ${e.message}"
                        Logger.e("[$pluginId] 분석 오류: ${e.message}")
                    } finally {
                        isProcessing.value = false
                    }
                }
//            } else {
//                parseError.value = "분석할 텍스트가 없습니다. 먼저 음성 인식을 진행해주세요."
//            }
        }
    }
}

@Composable
fun VoiceInputScreen(viewModel: VoiceInputPlugin.VoiceInputViewModel) {
    val recognizedText = viewModel.recognizedText.value
    val isListening = viewModel.isListening.value
    val parsedResult = viewModel.parsedResult.value
    val isProcessing = viewModel.isProcessing.value
    val parseError = viewModel.parseError.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (recognizedText.isEmpty()) "여기에 인식된 텍스트가 표시됩니다" else recognizedText,
            style = TextStyle(
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isProcessing) {
            CircularProgressIndicator()
            Text(
                text = "OpenAI로 분석 중...",
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (parseError != null) {
            Text(
                text = parseError,
                style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(16.dp)
            )
        } else if (parsedResult != null) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = "분석 결과",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "날짜: ${parsedResult.optString("date", "날짜 정보 없음")}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                    }

                    // meals 배열 처리
                    val meals = parsedResult.optJSONArray("meals") ?: JSONArray()
                    items(meals.length()) { index ->
                        val meal = meals.optJSONObject(index) ?: return@items
                        val mealType = meal.optString("mealType", "알 수 없음")
                        val translatedType = when(mealType) {
                            "breakfast" -> "아침"
                            "lunch" -> "점심"
                            "dinner" -> "저녁"
                            "snack" -> "간식"
                            else -> mealType
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "식사: $translatedType",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(4.dp))

                            // 식사 항목 표시
                            val items = meal.optJSONArray("items") ?: JSONArray()
                            if (items.length() == 0) {
                                Text("항목 없음", style = TextStyle(fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                            }

                            for (i in 0 until items.length()) {
                                val item = items.optJSONObject(i) ?: continue
                                val food = item.optString("food", "알 수 없음")
                                val quantity = item.optDouble("quantity", 0.0)
                                val unit = item.optString("unit", "개")

                                Text("• $food ${quantity}$unit")
                            }
                        }

                        if (index < meals.length() - 1) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 버튼 영역
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (isListening) {
                Button(
                    onClick = { viewModel.stopListening() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color.Red
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("음성 인식 중지")
                }
            } else {
                Button(
                    onClick = { viewModel.startListening() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("음성 인식 시작")
                }

//                if (recognizedText.isNotEmpty() && recognizedText != "듣는 중..." && recognizedText != "말씀해 주세요...") {
                    Button(
                        onClick = { viewModel.parseRecognizedText() },
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("분석하기")
                    }
//                }
            }
        }
    }
}