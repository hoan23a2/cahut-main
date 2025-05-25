package com.example.cahut.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cahut.data.api.ApiService
import com.example.cahut.data.api.LoginRequest
import com.example.cahut.data.api.LoginResponse
import com.example.cahut.data.api.RegisterRequest
import com.example.cahut.data.api.RetrofitClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.example.cahut.config.AppConfig
import android.util.Log

class AuthRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val apiService: ApiService = RetrofitClient.createService(context)
    
    private val apiServiceLogin: ApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val baseUrl = AppConfig.getBaseUrl() // ✅ Lấy từ config

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiServiceLogin.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Save token and userId
                    sharedPreferences.edit().apply {
                        putString("auth_token", loginResponse.token)
                        putInt("user_image", loginResponse.userImage)
                        apply()
                    }
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Login failed with error: $errorBody")
                Result.failure(Exception(errorBody ?: "Đăng nhập thất bại"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Result.failure(e)
        }
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString("userId", null)
    }
    
    fun getUserImage(): Int {
        return sharedPreferences.getInt("user_image", 1)
    }
    
    fun clearToken() {
        sharedPreferences.edit().apply {
            remove("auth_token")
            remove("userId")
            remove("user_image")
            apply()
        }
    }
    
    suspend fun register(username: String, email: String, password: String) {
        val request = RegisterRequest(username, email, password)
        val response = apiService.register(request)
        
        if (!response.isSuccessful) {
            throw Exception(response.errorBody()?.string() ?: "Đăng ký thất bại")
        }
    }
} 