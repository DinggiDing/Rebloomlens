package com.hdil.rebloomlens.rebloomlens.core.utils

import org.json.JSONObject

//ROLE  JSON → JSONObject

object JsonParser {
    fun parse(jsonString: String): JSONObject {
        return try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            JSONObject()
        }
    }
}