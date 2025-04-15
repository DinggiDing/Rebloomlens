package com.hdil.rebloomlens.sensor_plugins.health_connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HealthConnectViewModelFactory(
    private val healthConnectManager: HealthConnectManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthConnectViewModel::class.java)) {
            return HealthConnectViewModel(healthConnectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}