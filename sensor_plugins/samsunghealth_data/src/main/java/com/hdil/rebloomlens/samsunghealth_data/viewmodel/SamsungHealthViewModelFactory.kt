package com.hdil.rebloomlens.samsunghealth_data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hdil.rebloomlens.samsunghealth_data.SamsungHealthManager

class SamsungHealthViewModelFactory(
    private val samsungHealthManager: SamsungHealthManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SamsungHealthMainViewModel::class.java)) {
            return SamsungHealthMainViewModel(samsungHealthManager) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}