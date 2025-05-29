package com.hdil.voice_input

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import com.hdil.rebloomlens.common.utils.Logger

//class RecognitionListenerImpl(
//    private val onResult: (String) -> Unit,
//    private val onError: ((Int) -> Unit)? = null,
//    private val onPartialResult: ((String) -> Unit)? = null,
//    private val onReadyForSpeech: (() -> Unit)? = null
//) : RecognitionListener {
//
//    override fun onResults(results: Bundle?) {
//        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//        if (matches != null && matches.isNotEmpty()) {
//            onResult(matches[0])
//        } else {
//            Logger.e("[VoiceInput] 인식 결과가 비어 있습니다")
//            onError?.invoke(SpeechRecognizer.ERROR_NO_MATCH)
//        }
//    }
//
//    override fun onReadyForSpeech(params: Bundle?) {
//        Logger.e("[VoiceInput] 음성 인식 준비됨")
//        onReadyForSpeech?.invoke()
//    }
//
//    override fun onBeginningOfSpeech() {
//        Logger.e("[VoiceInput] 음성 인식 시작")
//    }
//
//    override fun onRmsChanged(rmsdB: Float) {
//        // RMS 변화 로깅 생략
//    }
//
//    override fun onBufferReceived(buffer: ByteArray?) {
//        Logger.e("[VoiceInput] 오디오 버퍼 수신")
//    }
//
//    override fun onEndOfSpeech() {
//        Logger.e("[VoiceInput] 음성 인식 종료")
//    }
//
//    override fun onError(error: Int) {
//        val errorMessage = when (error) {
//            SpeechRecognizer.ERROR_AUDIO -> "오디오 녹음 오류"
//            SpeechRecognizer.ERROR_CLIENT -> "클라이언트 측 오류"
//            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 부족"
//            SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
//            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
//            SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
//            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "인식기가 사용 중"
//            SpeechRecognizer.ERROR_SERVER -> "서버 오류"
//            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
//            else -> "알 수 없는 오류: $error"
//        }
//        Logger.e("[VoiceInput] 오류: $errorMessage")
//        onError?.invoke(error)
//    }
//
//    override fun onPartialResults(partialResults: Bundle?) {
//        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//        if (matches != null && matches.isNotEmpty()) {
//            onPartialResult?.invoke(matches[0])
//        }
//    }
//
//    override fun onEvent(eventType: Int, params: Bundle?) {
//        Logger.e("[VoiceInput] 이벤트 타입: $eventType")
//    }
//}

class RecognitionListenerImpl(
    private val onResult: (String) -> Unit,
    private val onError: ((Int) -> Unit)? = null,
    private val onPartialResult: ((String) -> Unit)? = null,
    private val onReadyForSpeech: (() -> Unit)? = null
) : RecognitionListener {

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            onResult(matches[0])
        } else {
            Logger.e("[VoiceInput] 인식 결과가 비어 있습니다")
            onError?.invoke(SpeechRecognizer.ERROR_NO_MATCH)
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Logger.d("[VoiceInput] 음성 인식 준비됨")
        onReadyForSpeech?.invoke()
    }

    override fun onBeginningOfSpeech() {
        Logger.d("[VoiceInput] 음성 인식 시작")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // 볼륨 레벨 변화는 로깅하지 않음 (너무 빈번)
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Logger.d("[VoiceInput] 오디오 버퍼 수신")
    }

    override fun onEndOfSpeech() {
        Logger.d("[VoiceInput] 음성 인식 종료")
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "오디오 녹음 오류"
            SpeechRecognizer.ERROR_CLIENT -> "클라이언트 측 오류"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 부족"
            SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
            SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "인식기가 사용 중"
            SpeechRecognizer.ERROR_SERVER -> "서버 오류"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
            else -> "알 수 없는 오류: $error"
        }
        Logger.e("[VoiceInput] 오류: $errorMessage")
        onError?.invoke(error)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches != null && matches.isNotEmpty()) {
            // 부분 결과 콜백을 항상 호출
            onPartialResult?.invoke(matches[0])
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Logger.d("[VoiceInput] 이벤트 타입: $eventType")
    }
}

private fun Logger.d(string: kotlin.String) {}
