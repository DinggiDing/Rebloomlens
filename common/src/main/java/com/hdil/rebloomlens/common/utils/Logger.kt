package com.hdil.rebloomlens.common.utils

import android.util.Log

object Logger {
    private const val TAG = "Rebloomlens"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String) {
        Log.e(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }
}