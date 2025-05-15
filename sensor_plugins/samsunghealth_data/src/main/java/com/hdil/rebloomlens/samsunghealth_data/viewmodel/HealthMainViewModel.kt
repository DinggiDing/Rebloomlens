package com.hdil.rebloomlens.samsunghealth_data.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
import com.hdil.rebloomlens.samsunghealth_data.utility.getExceptionHandler
import com.samsung.android.sdk.health.data.HealthDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HealthMainViewModel(
    private val healthDataStore: HealthDataStore
    activity: Activity
) : ViewModel() {

    private val _permissionResponse = MutableStateFlow(Pair(AppConstants.WAITING, -1))
    private val _exceptionResponse: MutableLiveData<String> = MutableLiveData<String>()
    private val exceptionHandler = getExceptionHandler(activity, _exceptionResponse)
    val permissionResponse: StateFlow<Pair<String, Int>> = _permissionResponse
    val exceptionResponse: LiveData<String> = _exceptionResponse


}