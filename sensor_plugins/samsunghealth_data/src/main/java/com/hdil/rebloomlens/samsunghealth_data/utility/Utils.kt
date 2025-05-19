package com.hdil.rebloomlens.samsunghealth_data.utility

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler

/** Common Exception Handler (Resolve ResolvablePlatformException) **/
//fun getExceptionHandler(
//    activity: Activity,
//    exceptionResponse: MutableLiveData<String>
//): CoroutineExceptionHandler {
//    return CoroutineExceptionHandler { _, exception ->
//        if ((exception is ResolvablePlatformException) && exception.hasResolution) {
//            exception.resolve(activity)
//        }
//        exceptionResponse.postValue(exception.message!!)
//    }
//}

/** Common Exception Handler **/
fun getExceptionHandler(
    exceptionResponse: MutableLiveData<String>
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, exception ->
        // ResolvablePlatformException 처리 로직은 제거됨
        // 에러 메시지가 null일 수 있으므로 안전하게 처리
        exceptionResponse.postValue(exception.message ?: "Unknown error")
    }
}