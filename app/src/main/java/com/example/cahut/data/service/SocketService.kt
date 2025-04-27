package com.example.cahut.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cahut.data.model.Player
import com.example.cahut.util.JwtUtils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService(private val context: Context) {
    private var socket: Socket? = null
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players
    
    private val _isCreator = MutableStateFlow(false)
    val isCreator: StateFlow<Boolean> = _isCreator
    
    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted
    
    fun connect(roomId: String) {
        try {
            val token = sharedPreferences.getString("auth_token", "") ?: return
            val options = IO.Options().apply {
                query = "token=$token"
            }
            socket = IO.socket("http://10.0.2.2:5000", options)
            
            socket?.on(Socket.EVENT_CONNECT) { _ ->
                Log.d("SocketService", "Socket connected")
                socket?.emit("join-room", JSONObject().apply {
                    put("roomId", roomId)
                    put("token", token)
                })
            }
            
            socket?.on("room-update") { args ->
                Log.d("SocketService", "Received room-update event: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    val users = data.getJSONArray("users")
                    Log.d("SocketService", "users: $users")
                    val playersList = mutableListOf<Player>()
                    
                    for (i in 0 until users.length()) {
                        val user = users.getJSONObject(i)
                        playersList.add(Player(
                            userId = user.getString("_id"),
                            username = user.getString("username")
                        ))
                    }
                    
                    _players.value = playersList
                    Log.d("SocketService", "Updated players list: ${playersList.size} players")
                    
                    val creatorId = data.getString("creatorId")
                    val currentUserId = JwtUtils.getUserIdFromToken(token)
                    Log.d("SocketService", "creatorId: $creatorId")
                    Log.d("SocketService", "userId: $currentUserId")
                    _isCreator.value = currentUserId == creatorId
                    Log.d("SocketService", "Is creator: ${currentUserId == creatorId}")
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing room-update: ${e.message}")
                }
            }
            
            socket?.on("game-started") { _ ->
                Log.d("SocketService", "Game started event received")
                _gameStarted.value = true
            }
            
            socket?.on("room-deleted") { _ ->
                Log.d("SocketService", "Room deleted event received")
            }
            
            socket?.on("error") { args ->
                Log.e("SocketService", "Error event received: ${args[0]}")
            }
            
            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e("SocketService", "Socket connection error: ${e.message}")
        }
    }
    
    fun leaveRoom(roomId: String) {
        val token = sharedPreferences.getString("auth_token", "") ?: return
        Log.d("SocketService", "Leaving room: $roomId")
        socket?.emit("leave-room", JSONObject().apply {
            put("roomId", roomId)
            put("token", token)
        })
    }
    
    fun deleteRoom(roomId: String) {
        val token = sharedPreferences.getString("auth_token", "") ?: return
        Log.d("SocketService", "Deleting room: $roomId")
        socket?.emit("delete-room", JSONObject().apply {
            put("roomId", roomId)
            put("token", token)
        })
    }
    
    fun startGame(roomId: String) {
        val token = sharedPreferences.getString("auth_token", "") ?: return
        Log.d("SocketService", "Starting game in room: $roomId")
        socket?.emit("start-game", JSONObject().apply {
            put("roomId", roomId)
            put("token", token)
        })
    }
    
    fun disconnect() {
        Log.d("SocketService", "Disconnecting socket")
        socket?.disconnect()
        socket = null
    }
} 