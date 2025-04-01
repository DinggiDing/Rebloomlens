package com.hdil.rebloomlens.rebloomlens.core

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

//ROLE  parsing json file

object ConfigLoader {
    fun load(context: Context, fileName: String): JSONObject {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            JSONObject(jsonString)
        } catch (e: Exception) {
            JSONObject()
        }
    }
}