package com.example.cahut.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.cahut.data.api.ApiService
import com.example.cahut.data.api.CreateRoomRequest
import com.example.cahut.data.api.JoinRoomRequest
import com.example.cahut.data.api.RetrofitClient
import com.example.cahut.data.model.Room
import com.example.cahut.data.model.CreateRoomResponse
import com.example.cahut.data.service.SocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

class RoomRepository(private val context: Context) {
    private val apiService: ApiService = RetrofitClient.createService(context)
    private val authRepository = AuthRepository(context)
    private val socketService = SocketService(context)

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun logResponse(response: Response<*>) {
        Log.d("RoomRepository", "Response code: ${response.code()}")
        Log.d("RoomRepository", "Response headers: ${response.headers()}")
        val responseBody = response.body()
        Log.d("RoomRepository", "Response body: $responseBody")
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("RoomRepository", "Error body: $errorBody")
        }
    }

    suspend fun createRoom(examId: String): Room {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val request = CreateRoomRequest(examId)
        
        val logMessage = "Creating room with examId: $examId"
        Log.d("RoomRepository", logMessage)

        val response = apiService.createRoom("Bearer $token", request)
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val errorMessage = "Failed to create room. Error: $errorBody"
            Log.e("RoomRepository", errorMessage)
            showToast(errorMessage)
            throw Exception(errorBody ?: "Failed to create room")
        }
        
        val createResponse = response.body()
        if (createResponse == null) {
            val nullMessage = "Empty response body"
            Log.e("RoomRepository", nullMessage)
            showToast(nullMessage)
            throw Exception(nullMessage)
        }

        Log.d("RoomRepository", "Room created: ${createResponse.room}")
//        showToast("Room created with ID: ${createResponse.roomId}")
        
        return createResponse.room
    }

    suspend fun joinRoom(roomId: String): Room {
        val token = authRepository.getToken() ?: throw Exception("Not authenticated")
        val request = JoinRoomRequest(roomId)
        
        val logMessage = "Joining room with roomId: $roomId"
        Log.d("RoomRepository", logMessage)
        showToast(logMessage)
        
        val response = apiService.joinRoom("Bearer $token", request)
        
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val errorMessage = "Failed to join room. Error: $errorBody"
            Log.e("RoomRepository", errorMessage)
            showToast(errorMessage)
            throw Exception(errorBody ?: "Failed to join room")
        }
        
        val joinResponse = response.body()
        if (joinResponse == null) {
            val nullMessage = "Empty response body"
            Log.e("RoomRepository", nullMessage)
            showToast(nullMessage)
            throw Exception(nullMessage)
        }

        Log.d("RoomRepository", "Successfully joined room: ${joinResponse.room}")
//        showToast("Successfully joined room")

        return joinResponse.room
    }
} 