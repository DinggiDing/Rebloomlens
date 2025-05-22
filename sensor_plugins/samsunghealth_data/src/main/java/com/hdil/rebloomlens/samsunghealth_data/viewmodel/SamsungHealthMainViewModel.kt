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

/**
 * ROLE : Samsung Health DataManagement and Business Logic Handler
 *
 * Manages UI state for Samsung Health data,
 * loads data through SamsungHealthManager, coordinates permission handling logic and data loading states.
 * Acts as a mediator between UI layer and data layer.
 */

class SamsungHealthMainViewModel(
    private val samsungHealthManager: SamsungHealthManager
) : ViewModel() {

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