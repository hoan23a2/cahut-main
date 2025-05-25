package com.example.cahut.data.service

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.cahut.config.AppConfig
import com.example.cahut.data.api.ApiService
import com.example.cahut.data.api.UpdateProfileRequest
import com.example.cahut.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthService(private val context: Context) {
    private val TAG = "AuthService"
    private val apiService: ApiService = RetrofitClient.createService(context)
    private val sharedPreferences = context.getSharedPreferences("auth_prefs", MODE_PRIVATE)

    private fun getToken(): String {
        return sharedPreferences.getString("auth_token", "") ?: ""
    }

    suspend fun updateProfile(
        username: String,
        userImage: Int,
        newPassword: String?,
        currentPassword: String
    ): String {
        try {
            Log.d(TAG, "Starting profile update...")
            Log.d(TAG, "Username: $username")
            Log.d(TAG, "UserImage: $userImage")
            Log.d(TAG, "Has new password: ${!newPassword.isNullOrBlank()}")

            val request = UpdateProfileRequest(
                username = username,
                userImage = userImage,
                newPassword = newPassword,
                currentPassword = currentPassword
            )

            return withContext(Dispatchers.IO) {
                Log.d(TAG, "Executing network request...")
                val response = apiService.updateProfile(
                    token = "Bearer ${getToken()}",
                    request = request
                )
                
                Log.d(TAG, "Response Code: ${response.code()}")
                Log.d(TAG, "Response Body: ${response.body()}")

                if (!response.isSuccessful) {
                    val errorMessage = response.errorBody()?.string() ?: "Lỗi khi cập nhật thông tin"
                    Log.e(TAG, "Error response: $errorMessage")
                    throw Exception(errorMessage)
                }

                try {
                    val updateResponse = response.body()
                    if (updateResponse == null) {
                        throw Exception("Không nhận được phản hồi từ server")
                    }
                    
                    Log.d(TAG, "Successfully got new token")
                    
                    // Update token in SharedPreferences
                    sharedPreferences.edit().putString("auth_token", updateResponse.token).apply()
                    Log.d(TAG, "Token updated in SharedPreferences")
                    
                    updateResponse.token
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response: ${e.message}")
                    throw Exception("Lỗi khi xử lý phản hồi từ server: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateProfile: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
} 