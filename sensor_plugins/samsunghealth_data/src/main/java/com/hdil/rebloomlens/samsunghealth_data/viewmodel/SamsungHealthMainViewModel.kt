package com.hdil.rebloomlens.samsunghealth_data.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.samsunghealth_data.SamsungHealthManager
import com.hdil.rebloomlens.samsunghealth_data.utility.AppConstants
import com.hdil.rebloomlens.samsunghealth_data.utility.getExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SamsungHealthMainViewModel(
    private val samsungHealthManager: SamsungHealthManager
) : ViewModel() {

    private val _permissionResponse = MutableStateFlow(Pair(AppConstants.WAITING, -1))
    private val _exceptionResponse: MutableLiveData<String> = MutableLiveData<String>()
    private val exceptionHandler = getExceptionHandler(exceptionResponse = _exceptionResponse)
    val permissionResponse: StateFlow<Pair<String, Int>> = _permissionResponse
    val exceptionResponse: LiveData<String> = _exceptionResponse

    private val _uiState = MutableStateFlow(SamsungHealthUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    fun checkForPermission(
        context: Context,
        permSet: MutableSet<com.samsung.android.sdk.health.data.permission.Permission>,
        activityId: Int
    ) {
        viewModelScope.launch(exceptionHandler) {
            val result = samsungHealthManager.checkAndRequestPermissions(context, permSet, activityId)
            _permissionResponse.emit(result)
        }
    }

    fun connectToSamsungHealth(context: Context) {
        viewModelScope.launch(exceptionHandler) {
            samsungHealthManager.connectToSamsungHealth(context)
        }
    }

    fun resetPermissionResponse() {
        viewModelScope.launch {
            _permissionResponse.emit(Pair(AppConstants.WAITING, -1))
        }
    }

    // 데이터 로드 함수들
    fun loadHeartRateData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val heartRate = samsungHealthManager.readHeartRateData()
                _uiState.update {
                    it.copy(
                        heartRate = heartRate,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class SamsungHealthUiState(
    val heartRate: List<HeartRateData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)