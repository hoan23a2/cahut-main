package com.example.cahut.util

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun decodeJwt(token: String): JSONObject? {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = parts[1]
            val decodedPayload = String(Base64.decode(payload, Base64.URL_SAFE))
            return JSONObject(decodedPayload)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getUserIdFromToken(token: String): String? {
        return decodeJwt(token)?.optString("id")
    }

    fun getUsernameFromToken(token: String): String? {
        return decodeJwt(token)?.optString("username")
    }
}
