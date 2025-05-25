package com.example.cahut.config

import android.content.Context
import java.util.*

object AppConfig {
    private var properties: Properties? = null

    fun load(context: Context) {
        properties = Properties()
        val inputStream = context.assets.open("config.properties")
        properties?.load(inputStream)
    }

    fun get(key: String): String? {
        return properties?.getProperty(key)
    }

    fun getBaseUrl(): String {
        return get("BASE_URL") ?: "http://10.0.2.21:5000"
    }
}
