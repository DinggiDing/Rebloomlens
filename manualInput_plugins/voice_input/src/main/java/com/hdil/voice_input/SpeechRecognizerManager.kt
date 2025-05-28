package com.hdil.voice_input

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.hdil.rebloomlens.common.utils.Logger

//class SpeechRecognizerManager(private val context: Context) {
//    private var speechRecognizer: SpeechRecognizer? = null
//    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")  // 한국어로 설정
//        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
//        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
//        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
//    }
//
//    init {
//        initializeSpeechRecognizer()
//    }
//
//    private fun initializeSpeechRecognizer() {
//        try {
//            // 이전 인스턴스가 있으면 정리
//            speechRecognizer?.destroy()
//
//            // 새 인스턴스 생성
//            if (SpeechRecognizer.isRecognitionAvailable(context)) {
//                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
//                Logger.i("[VoiceInput] SpeechRecognizer 초기화 성공")
//            } else {
//                Logger.e("[VoiceInput] 음성 인식이 기기에서 지원되지 않습니다")
//            }
//        } catch (e: Exception) {
//            Logger.e("[VoiceInput] SpeechRecognizer 초기화 실패: ${e.message}")
//            speechRecognizer = null
//        }
//    }
//
//    fun startListening(listener: RecognitionListener) {
//        try {
//            if (speechRecognizer == null) {
//                initializeSpeechRecognizer()
//            }
//
//            speechRecognizer?.let {
//                it.setRecognitionListener(listener)
//                it.startListening(recognizerIntent)
//                Logger.e("[VoiceInput] 음성 인식 시작")
//            } ?: run {
//                Logger.e("[VoiceInput] 음성 인식 시작 실패: SpeechRecognizer가 null입니다")
//                // 콜백을 통해 오류 전달
//                val bundle = Bundle()
//                listener.onError(SpeechRecognizer.ERROR_CLIENT)
//            }
//        } catch (e: Exception) {
//            Logger.e("[VoiceInput] 음성 인식 시작 중 오류 발생: ${e.message}")
//            // 콜백을 통해 오류 전달
//            listener.onError(SpeechRecognizer.ERROR_CLIENT)
//        }
//    }
//
//    fun stopListening() {
//        try {
//            speechRecognizer?.stopListening()
//            Logger.e("[VoiceInput] 음성 인식 중지")
//        } catch (e: Exception) {
//            Logger.e("[VoiceInput] 음성 인식 중지 중 오류 발생: ${e.message}")
//        }
//    }
//
//    fun destroy() {
//        try {
//            speechRecognizer?.destroy()
//            speechRecognizer = null
//            Logger.e("[VoiceInput] SpeechRecognizer 해제됨")
//        } catch (e: Exception) {
//            Logger.e("[VoiceInput] SpeechRecognizer 해제 중 오류 발생: ${e.message}")
//        }
//    }
//}

class SpeechRecognizerManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")  // 한국어로 설정
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // 부분 결과 활성화
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }

    init {
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        try {
            // 이전 인스턴스가 있으면 정리
            speechRecognizer?.destroy()

            // 새 인스턴스 생성
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                Logger.i("[VoiceInput] SpeechRecognizer 초기화 성공")
            } else {
                Logger.e("[VoiceInput] 음성 인식이 기기에서 지원되지 않습니다")
            }
        } catch (e: Exception) {
            Logger.e("[VoiceInput] SpeechRecognizer 초기화 실패: ${e.message}")
            speechRecognizer = null
        }
    }

    fun startListening(listener: RecognitionListener) {
        try {
            if (speechRecognizer == null) {
                initializeSpeechRecognizer()
            }

            speechRecognizer?.let {
                it.setRecognitionListener(listener)
                it.startListening(recognizerIntent)
                Logger.i("[VoiceInput] 음성 인식 시작")
            } ?: run {
                Logger.e("[VoiceInput] 음성 인식 시작 실패: SpeechRecognizer가 null입니다")
                // 콜백을 통해 오류 전달
                listener.onError(SpeechRecognizer.ERROR_CLIENT)
            }
        } catch (e: Exception) {
            Logger.e("[VoiceInput] 음성 인식 시작 중 오류 발생: ${e.message}")
            // 콜백을 통해 오류 전달
            listener.onError(SpeechRecognizer.ERROR_CLIENT)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            Logger.i("[VoiceInput] 음성 인식 중지")
        } catch (e: Exception) {
            Logger.e("[VoiceInput] 음성 인식 중지 중 오류 발생: ${e.message}")
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            Logger.i("[VoiceInput] SpeechRecognizer 해제됨")
        } catch (e: Exception) {
            Logger.e("[VoiceInput] SpeechRecognizer 해제 중 오류 발생: ${e.message}")
        }
    }
}