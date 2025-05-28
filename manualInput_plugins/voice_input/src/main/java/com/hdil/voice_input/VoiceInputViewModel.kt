package com.hdil.voice_input

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.hdil.rebloomlens.common.utils.Logger

class VoiceInputViewModel : ViewModel() {
    var recognizedText = mutableStateOf("")
        private set

    private var manager: SpeechRecognizerManager? = null

    fun initialize(context: Context) {
        if (manager == null) {
            manager = SpeechRecognizerManager(context)
            Logger.i("[VoiceInput] SpeechRecognizerManager initialized")
        }
    }

    fun startListening() {
        manager?.let {
            it.startListening(RecognitionListenerImpl(
                onResult = { text ->
                    recognizedText.value = text
                    Logger.i("[VoiceInput] 인식된 텍스트: $text")
                },
                onPartialResult = { partialText ->
                    // 부분 결과는 UI에 표시하지 않고 로그만 남김
                    Logger.e("[VoiceInput] 인식 중: $partialText")
                },
                onError = { errorCode ->
                    // 오류 발생 시 사용자에게 알림
                    recognizedText.value = "음성 인식 오류가 발생했습니다. 다시 시도해주세요."
                }
            ))
        } ?: run {
            Logger.e("[VoiceInput] SpeechRecognizerManager가 초기화되지 않았습니다")
            recognizedText.value = "음성 인식을 사용할 수 없습니다"
        }
    }

    fun stopListening() {
        manager?.stopListening()
        Logger.i("[VoiceInput] 음성 인식 중지")
    }

    override fun onCleared() {
        super.onCleared()
        manager?.destroy()
        manager = null
        Logger.i("[VoiceInput] 리소스 정리 완료")
    }
}