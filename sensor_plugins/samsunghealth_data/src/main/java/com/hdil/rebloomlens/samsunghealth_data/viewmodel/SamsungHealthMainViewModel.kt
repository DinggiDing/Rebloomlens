package com.hdil.rebloomlens.samsunghealth_data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdil.rebloomlens.common.model.BloodPressureData
import com.hdil.rebloomlens.common.model.BodyFatData
import com.hdil.rebloomlens.common.model.ExerciseData
import com.hdil.rebloomlens.common.model.HeartRateData
import com.hdil.rebloomlens.common.model.SkeletalMuscleMassData
import com.hdil.rebloomlens.common.model.SleepSessionData
import com.hdil.rebloomlens.common.model.StepData
import com.hdil.rebloomlens.common.model.WeightData
import com.hdil.rebloomlens.common.utils.Logger
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
        Logger.e("loadHeartRateData called")
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

    fun loadSleepData() {
        viewModelScope.launch {
            Logger.e("loadSleepData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sleep = samsungHealthManager.readSleepData()
                _uiState.update {
                    it.copy(
                        sleep = sleep,
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
            Logger.e("loadStepData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val step = samsungHealthManager.readStepData()
                _uiState.update {
                    it.copy(
                        step = step,
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
            Logger.e("loadBloodPressureData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bloodPressure = samsungHealthManager.readBloodPressureData()
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
            Logger.e("loadBodyFatData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bodyFat = samsungHealthManager.readBodyFatPercentageData()
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

    fun loadSkeletalMuscleMassData() {
        viewModelScope.launch {
            Logger.e("loadSkeletalMuscleMassData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val skeletalMuscleMass = samsungHealthManager.readSkeletalMuscleMassData()
                _uiState.update {
                    it.copy(
                        skeletalMuscleMass = skeletalMuscleMass,
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
            Logger.e("loadWeightData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val weight = samsungHealthManager.readWeightData()
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

    fun loadExerciseData() {
        viewModelScope.launch {
            Logger.e("loadExerciseData called")
            _uiState.update { it.copy(isLoading = true) }
            try {
                val exercise = samsungHealthManager.readExerciseData()
                _uiState.update {
                    it.copy(
                        exercise = exercise,
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
    val sleep: List<SleepSessionData> = emptyList(),
    val step: List<StepData> = emptyList(),
    val bloodPressure: List<BloodPressureData> = emptyList(),
    val bodyFat: List<BodyFatData> = emptyList(),
    val skeletalMuscleMass: List<SkeletalMuscleMassData> = emptyList(),
    val weight: List<WeightData> = emptyList(),
    val exercise: List<ExerciseData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)