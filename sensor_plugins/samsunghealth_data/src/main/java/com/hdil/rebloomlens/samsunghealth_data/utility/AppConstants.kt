package com.hdil.rebloomlens.samsunghealth_data.utility

import kotlinx.coroutines.Dispatchers
import java.time.LocalDateTime
import java.time.LocalTime

object AppConstants {
    const val SUCCESS = "SUCCESS"
    const val WAITING = "WAITING"
    const val NO_PERMISSION = "NO PERMISSION"
    const val NUTRITION_ACTIVITY = 0
    const val STEP_ACTIVITY = 1
    const val HEART_RATE_ACTIVITY = 2
    const val SLEEP_ACTIVITY = 3
    const val EXERCISE_ACTIVITY = 4
    const val SKIN_TEMP_UNIT = "\u2103"
    const val BLOOD_OXYGEN_UNIT = "\u0025"
    val minimumDate: LocalDateTime = LocalDateTime.of(1900, 1, 1, 0, 0)
    val currentDate: LocalDateTime = LocalDateTime.now().with(LocalTime.MIDNIGHT)
    val SCOPE_IO_DISPATCHERS = Dispatchers.IO
    const val BUNDLE_KEY_MEAL_TYPE = "MEAL_TYPE"
    const val BUNDLE_KEY_INSERT_DATE = "INSERT_DATE"
    const val BUNDLE_KEY_NUTRITION_DATA = "NUTRITION_DATA"
    const val CHOOSE_FOOD_ACTIVITY = 4
    const val UPDATE_FOOD_ACTIVITY = 5
    const val NO_WRITE_PERMISSION = -1
    const val APP_ID = "com.hdil.rebloomlens.samsunghealth_data"
}