package com.hdil.rebloomlens.sensor_plugins.health_connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HealthConnectViewModel(
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState = _uiState.asStateFlow()

    fun loadSleepData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sessions = healthConnectManager.readSleepSessions()
                _uiState.update {
                    it.copy(
                        sleepSessions = sessions,
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

    fun loadStepData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val steps = healthConnectManager.readStepData()
                _uiState.update {
                    it.copy(
                        steps = steps,
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

data class HealthConnectUiState(
    val sleepSessions: List<SleepSessionData> = emptyList(),
    val steps: List<StepData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)