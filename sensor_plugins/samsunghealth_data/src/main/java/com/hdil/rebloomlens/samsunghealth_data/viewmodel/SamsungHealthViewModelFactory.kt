package com.hdil.rebloomlens.samsunghealth_data.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hdil.rebloomlens.samsunghealth_data.SamsungHealthManager
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore

class SamsungHealthViewModelFactory(
    private val context: Context,
    private val healthDataStore: HealthDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SamsungHealthMainViewModel::class.java)) {
            // SamsungHealthManager 인스턴스 생성 후 ViewModel에 주입
            val samsungHealthManager = SamsungHealthManager(healthDataStore)
            return SamsungHealthMainViewModel(samsungHealthManager) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}