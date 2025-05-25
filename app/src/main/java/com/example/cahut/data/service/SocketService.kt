package com.example.cahut.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cahut.data.model.Player
import com.example.cahut.data.model.PlayingQuestion
import LeaderboardEntry
import com.example.cahut.util.JwtUtils
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URISyntaxException
import com.example.cahut.config.AppConfig


class SocketService(private val context: Context) {
    private var socket: Socket? = null
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()
    
    private val _isCreator = MutableStateFlow(false)
    val isCreator: StateFlow<Boolean> = _isCreator.asStateFlow()
    
    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted.asStateFlow()

    private val _roomDeleted = MutableStateFlow(false)
    val roomDeleted: StateFlow<Boolean> = _roomDeleted.asStateFlow()

    private val _quizEvents = MutableStateFlow<QuizEvent?>(null)
    val quizEvents: StateFlow<QuizEvent?> = _quizEvents.asStateFlow()

    sealed class QuizEvent {
        data class Countdown(val countdown: Int) : QuizEvent()
        data class NextQuestion(
            val question: PlayingQuestion,
            val questionIndex: Int,
            val totalQuestions: Int
        ) : QuizEvent()
        data class ShowResults(
            val question: String,
            val options: List<String>,
            val correctAnswer: String,
            val type: String?,
            val imageUrl: String?
        ) : QuizEvent()
        data class ShowScores(val leaderboard: List<LeaderboardEntry>) : QuizEvent()
        data class GameEnded(val leaderboard: List<LeaderboardEntry>) : QuizEvent()
        object RoomDeleted : QuizEvent()
        data class Error(val message: String) : QuizEvent()
    }

    fun connect(roomId: String) {
        try {
            val token = sharedPreferences.getString("auth_token", "") ?: return
            val options = IO.Options().apply {
                query = "token=$token"
            }
            val baseUrl = AppConfig.getBaseUrl()
            socket = IO.socket("${baseUrl}", options)
            
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
                            username = user.getString("username"),
                            userImage = user.optInt("userImage", 1)
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
            
            socket?.on("countdown") { args ->
                Log.d("SocketService", "Countdown event received: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    val countdown = data.getInt("countdown")
                    _quizEvents.value = QuizEvent.Countdown(countdown)
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing countdown: ${e.message}")
                }
            }

            socket?.on("next-question") { args ->
                Log.d("SocketService", "Next question event received: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    if (!data.has("question")) {
                        Log.d("SocketService", "No question field in next-question event, skipping")
                        return@on
                    }
                    val questionObj = data.getJSONObject("question")
                    val question = PlayingQuestion(
                        question = questionObj.getString("question"),
                        options = (0 until questionObj.getJSONArray("options").length())
                            .map { questionObj.getJSONArray("options").getString(it) },
                        correctAnswer = questionObj.getString("correctAnswer"),
                        timeLimit = questionObj.getInt("timeLimit"),
                        type = if (questionObj.has("type")) questionObj.getString("type") else "normal",
                        imageUrl = if (questionObj.has("imageUrl")) questionObj.getString("imageUrl") else null
                    )
                    _quizEvents.value = QuizEvent.NextQuestion(
                        question = question,
                        questionIndex = data.getInt("questionIndex"),
                        totalQuestions = data.getInt("totalQuestions")
                    )
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing next-question: ${e.message}")
                }
            }

            socket?.on("show-results") { args ->
                Log.d("SocketService", "Show results event received: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    _quizEvents.value = QuizEvent.ShowResults(
                        question = data.getString("question"),
                        options = (0 until data.getJSONArray("options").length())
                            .map { data.getJSONArray("options").getString(it) },
                        correctAnswer = data.getString("correctAnswer"),
                        type = if (data.has("type")) data.getString("type") else null,
                        imageUrl = if (data.has("imageUrl")) data.getString("imageUrl") else null
                    )
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing show-results: ${e.message}")
                }
            }

            socket?.on("show-scores") { args ->
                Log.d("SocketService", "Show scores event received: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    val leaderboard = (0 until data.getJSONArray("leaderboard").length())
                        .map { index ->
                            val entry = data.getJSONArray("leaderboard").getJSONObject(index)
                            LeaderboardEntry(
                                id = entry.getString("id"),
                                rank = entry.getInt("rank"),
                                username = entry.getString("username"),
                                score = entry.getInt("score"),
                                userImage = entry.optInt("userImage", 1),
                                isCorrectForLastQuestion = if (entry.has("isCorrectForLastQuestion")) entry.getBoolean("isCorrectForLastQuestion") else null
                            )
                        }
                    _quizEvents.value = QuizEvent.ShowScores(leaderboard)
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing show-scores: ${e.message}")
                }
            }

            socket?.on("game-ended") { args ->
                Log.d("SocketService", "Game ended event received: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    val leaderboard = (0 until data.getJSONArray("leaderboard").length())
                        .map { index ->
                            val entry = data.getJSONArray("leaderboard").getJSONObject(index)
                            LeaderboardEntry(
                                id = entry.getString("id"),
                                rank = entry.getInt("rank"),
                                username = entry.getString("username"),
                                score = entry.getInt("score"),
                                userImage = entry.optInt("userImage", 1),
                                isCorrectForLastQuestion = if (entry.has("isCorrectForLastQuestion")) entry.getBoolean("isCorrectForLastQuestion") else null
                            )
                        }
                    _quizEvents.value = QuizEvent.GameEnded(leaderboard)
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing game-ended: ${e.message}")
                }
            }
            
            socket?.on("room-deleted") { _ ->
                Log.d("SocketService", "Room deleted event received")
                _roomDeleted.value = true
                _quizEvents.value = QuizEvent.RoomDeleted
            }
            
            socket?.on("error") { args ->
                Log.e("SocketService", "Error event received: ${args[0]}")
                try {
                    val data = args[0] as JSONObject
                    _quizEvents.value = QuizEvent.Error(data.getString("message"))
                } catch (e: Exception) {
                    Log.e("SocketService", "Error processing error event: ${e.message}")
                }
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

    fun submitAnswer(roomId: String, answer: String, score: Int) {
        val token = sharedPreferences.getString("auth_token", "") ?: return
        Log.d("SocketService", "Submitting answer: $answer with score: $score for room: $roomId")
        socket?.emit("submit-answer", JSONObject().apply {
            put("roomId", roomId)
            put("answer", answer)
            put("score", score)
            put("token", token)
        })
    }

    fun timeUp(roomId: String) {
        Log.d("SocketService", "Time up for room: $roomId")
        socket?.emit("time-up", JSONObject().apply {
            put("roomId", roomId)
        })
    }
    
    fun disconnect() {
        Log.d("SocketService", "Disconnecting socket")
        socket?.disconnect()
        socket = null
        _players.value = emptyList()
        _isCreator.value = false
        _gameStarted.value = false
        _roomDeleted.value = false
        _quizEvents.value = null
    }
} 