package com.hdil.rebloomlens.samsunghealth_data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.samsunghealth_data.SamsungHealthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SamsungHealthMainViewModel(
    private val samsungHealthManager: SamsungHealthManager
) : ViewModel() {

//    private val _permissionResponse = MutableStateFlow(Pair(AppConstants.WAITING, -1))
//    private val _exceptionResponse: MutableLiveData<String> = MutableLiveData<String>()
//    private val exceptionHandler = getExceptionHandler(exceptionResponse = _exceptionResponse)
//    val permissionResponse: StateFlow<Pair<String, Int>> = _permissionResponse
//    val exceptionResponse: LiveData<String> = _exceptionResponse
//
//    private val _uiState = MutableStateFlow(SamsungHealthUiState(isLoading = true))
//    val uiState = _uiState.asStateFlow()
//
//    fun initializeHealthConnection(context: Context) {
//        viewModelScope.launch(exceptionHandler) {
//            samsungHealthManager.initSamsungHealthConnection(context)
//        }
//    }
//
//    fun checkForPermission(
//        context: Context,
//        permSet: MutableSet<Permission>,
//        activityId: Int
//    ) {
//        viewModelScope.launch(exceptionHandler) {
//            val result = samsungHealthManager.checkAndRequestPermissions(context, permSet, activityId)
//            _permissionResponse.emit(result)
//        }
//    }
//
//    fun resetPermissionResponse() {
//        viewModelScope.launch {
//            _permissionResponse.emit(Pair(AppConstants.WAITING, -1))
//        }
//    }
//
//    // 데이터 로드 함수들
//    fun loadHeartRateData() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                val heartRate = samsungHealthManager.readHeartRateData()
//                _uiState.update {
//                    it.copy(
//                        heartRate = heartRate,
//                        isLoading = false
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(
//                        error = e.message,
//                        isLoading = false
//                    )
//                }
//            }
//        }
//    }
    private val _uiState = MutableStateFlow(SamsungHealthUiState(isLoading = false))
    val uiState = _uiState.asStateFlow()

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

    // 삼성 헬스 초기화를 위한 함수
    fun initializeHealthConnection(context: Context) {
        viewModelScope.launch {
            try {
                samsungHealthManager.initSamsungHealthConnection(context)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "권한 요청 중 오류: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // 권한이 필요한 작업을 실행하기 위한 함수
    fun runWithPermissions(
        context: Context,
        activityId: Int,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                samsungHealthManager.checkPermissionsAndRun(
                    context,
                    activityId
                ) {
                    action()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "권한 요청 중 오류: ${e.message}",
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