package com.hdil.rebloomlens.samsunghealth_data.utility

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.samsung.android.sdk.health.data.error.ResolvablePlatformException
import kotlinx.coroutines.CoroutineExceptionHandler

/** Common Exception Handler (Resolve ResolvablePlatformException) **/
fun getExceptionHandler(
    activity: Activity,
    exceptionResponse: MutableLiveData<String>
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, exception ->
        if ((exception is ResolvablePlatformException) && exception.hasResolution) {
            exception.resolve(activity)
        }
        exceptionResponse.postValue(exception.message!!)
    }
}