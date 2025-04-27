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
            
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // Use 10.0.2.2 for Android emulator to access localhost
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
                        apply()
                    }
                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString("userId", null)
    }
    
    fun clearToken() {
        sharedPreferences.edit().apply {
            remove("auth_token")
            remove("userId")
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