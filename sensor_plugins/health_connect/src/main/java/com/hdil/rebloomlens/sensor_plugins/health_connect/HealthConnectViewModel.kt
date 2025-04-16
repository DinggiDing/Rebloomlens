package com.hdil.rebloomlens.sensor_plugins.health_connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdil.rebloomlens.common.model.BloodGlucoseData
import com.hdil.rebloomlens.common.model.BloodPressureData
import com.hdil.rebloomlens.common.model.BodyFatData
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.model.WeightData
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

    fun loadWeightData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val weight = healthConnectManager.readWeightData()
                _uiState.update {
                    it.copy(
                        weight = weight,
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

    fun loadBloodGlucoseData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bloodGlucose = healthConnectManager.readBloodGlucoseData()
                _uiState.update {
                    it.copy(
                        bloodGlucose = bloodGlucose,
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

    fun loadBloodPressureData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bloodPressure = healthConnectManager.readBloodPressureData()
                _uiState.update {
                    it.copy(
                        bloodPressure = bloodPressure,
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

    fun loadBodyFatData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bodyFat = healthConnectManager.readBodyFatData()
                _uiState.update {
                    it.copy(
                        bodyFat = bodyFat,
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

    fun loadHeartRateData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val heartRate = healthConnectManager.readHeartRateData()
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

data class HealthConnectUiState(
    val sleepSessions: List<SleepSessionData> = emptyList(),
    val steps: List<StepData> = emptyList(),
    val weight: List<WeightData> = emptyList(),
    val bloodGlucose: List<BloodGlucoseData> = emptyList(),
    val bloodPressure: List<BloodPressureData> = emptyList(),
    val heartRate: List<HeartRateData> = emptyList(),
    val bodyFat: List<BodyFatData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)