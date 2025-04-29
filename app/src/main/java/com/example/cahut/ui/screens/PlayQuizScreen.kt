package com.example.cahut.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cahut.data.model.PlayingQuestion
import com.example.cahut.data.model.QuizResult
import com.example.cahut.data.model.LeaderboardEntry
import com.example.cahut.data.service.SocketService
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.platform.LocalContext
import com.example.cahut.navigation.Screen
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import com.example.cahut.util.JwtUtils

@Composable
fun PlayQuizScreen(
    navController: NavController,
    roomId: String,
    isHost: Boolean
) {
    val context = LocalContext.current
    val socketService = remember { SocketService(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val sharedPreferences = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }

    var question by remember { mutableStateOf<PlayingQuestion?>(null) }
    var questionIndex by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var showScores by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var showResults by remember { mutableStateOf<QuizResult?>(null) }
    var loading by remember { mutableStateOf(true) }
    var isCreator by remember { mutableStateOf(false) }
    var currentUsername by remember { mutableStateOf("") }
    var currentUserId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val token = sharedPreferences.getString("auth_token", "") ?: return@LaunchedEffect
        currentUsername = JwtUtils.getUsernameFromToken(token) ?: ""
        currentUserId = JwtUtils.getUserIdFromToken(token) ?: ""
        socketService.connect(roomId)
    }

    DisposableEffect(Unit) {
        onDispose {
            socketService.disconnect()
        }
    }

    LaunchedEffect(countdown) {
        countdown?.let {
            if (it > 0) {
                delay(1000)
                countdown = it - 1
            }
        }
    }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0 && question != null) {
            delay(1000)
            timeLeft--
        } else if (timeLeft == 0 && question != null) {
            if (selectedAnswer == null) {
                socketService.submitAnswer(roomId, "")
            }
            socketService.timeUp(roomId)
        }
    }

    LaunchedEffect(socketService.quizEvents) {
        socketService.quizEvents.collect { event ->
            when (event) {
                is SocketService.QuizEvent.Countdown -> {
                    countdown = event.countdown
                    question = null
                    showScores = null
                    showResults = null
                    selectedAnswer = null
                    loading = false
                }
                is SocketService.QuizEvent.NextQuestion -> {
                    question = event.question
                    questionIndex = event.questionIndex
                    totalQuestions = event.totalQuestions
                    timeLeft = event.question.timeLimit
                    countdown = null
                    selectedAnswer = null
                    showScores = null
                    showResults = null
                    loading = false
                }
                is SocketService.QuizEvent.ShowResults -> {
                    showResults = QuizResult(
                        event.question,
                        event.options,
                        event.correctAnswer
                    )
                    question = null
                    countdown = null
                    showScores = null
                }
                is SocketService.QuizEvent.ShowScores -> {
                    showScores = event.leaderboard
                    question = null
                    countdown = null
                    showResults = null
                }
                is SocketService.QuizEvent.GameEnded -> {
                    question = null
                    showScores = null
                    showResults = null
                    countdown = null
                    leaderboard = event.leaderboard
                    loading = false
                }
                is SocketService.QuizEvent.RoomDeleted -> {
                    scope.launch {
                        navController.navigate(Screen.GameLobby.route) {
                            popUpTo(Screen.PlayQuiz.route) { inclusive = true }
                        }
                        snackbarHostState.showSnackbar(
                            message = "Phòng đã bị xóa!",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is SocketService.QuizEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                        navController.navigate(Screen.GameLobby.route)
                    }
                }
                null -> {
                    loading = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f3a40))
            .padding(16.dp)
    ) {
        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang tải...", color = Color.White)
                }
            }
            countdown != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF23616A)
                    )
                ) {
                    Column(
            modifier = Modifier
                .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Chuẩn bị",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
        )
                        Spacer(modifier = Modifier.height(16.dp))
        Text(
                            "Bắt đầu sau: $countdown giây",
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    }
                }
            }
            question != null -> {
                Column(
                modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
        Text(
                        question!!.question,
                        color = Color.White,
                        fontSize = 20.sp,
            textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
        )

                    Text(
                        "⏳ Thời gian còn lại: $timeLeft giây",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    question!!.options.forEachIndexed { index, option ->
                        val optionLetter = ('A' + index).toString()
            val isSelected = selectedAnswer == option
                        val isDisabled = selectedAnswer != null

                        Button(
                            onClick = {
                                if (selectedAnswer == null) {
                                    selectedAnswer = option
                                    socketService.submitAnswer(roomId, option)
                                }
                            },
                modifier = Modifier
                    .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected)
                                    Color(0xFF00B074) else Color(0xFF23616A)
                ),
                            enabled = !isDisabled
            ) {
                Text(
                                "$optionLetter. $option",
                                color = Color.White
                            )
                        }
                    }

                    if (isHost) {
                        Button(
                            onClick = { socketService.timeUp(roomId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA726)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward,
                                contentDescription = "Bỏ qua",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bỏ qua câu hỏi", color = Color.White)
                        }
                    }
                }
            }
            showResults != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        showResults!!.question,
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    showResults!!.options.forEachIndexed { index, option ->
                        val optionLetter = ('A' + index).toString()
                        val isCorrect = option == showResults!!.correctAnswer
                        val isUserAnswer = option == selectedAnswer

        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isCorrect -> Color(0xFF28A745)
                                    isUserAnswer && !isCorrect -> Color(0xFFDC3545)
                                    else -> Color(0xFF23616A)
                                }
                            ),
                            enabled = false
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    "$optionLetter. $option",
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (isCorrect) {
                                    Text("✓", color = Color.White)
                                } else if (isUserAnswer && !isCorrect) {
                                    Text("✗", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            showScores != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF23616A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Bảng xếp hạng",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Điểm số hiện tại",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Người chơi $currentUsername đang có ${showScores?.find { it.id == currentUserId }?.score ?: 0} điểm!!!",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        showScores?.let { scores ->
                            if (scores.isNotEmpty()) {
                                LazyColumn {
                                    items(scores) { entry ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "Hạng ${entry.rank}",
                                                color = Color.White
                                            )
                                            Text(
                                                entry.username,
                                                color = Color.White
                                            )
                                            Text(
                                                "${entry.score} điểm",
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    "Chưa có dữ liệu xếp hạng",
                                    color = Color.White
                                )
                            }
                        } ?: run {
                            Text(
                                "Chưa có dữ liệu xếp hạng",
                                color = Color.White
                            )
                        }
                    }
                }
            }
            leaderboard != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF23616A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Kết quả cuối cùng",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Chúc mừng!",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Người chơi $currentUsername đã đạt được: ${leaderboard!!.find { it.id == currentUserId }?.score ?: 0} điểm!!!",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn {
                            items(leaderboard!!) { entry ->
                                Row(
            modifier = Modifier
                .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Hạng ${entry.rank}",
                                        color = Color.White
                                    )
                                    Text(
                                        entry.username,
                                        color = Color.White
                                    )
                                    Text(
                                        "${entry.score} điểm",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (isHost) {
                                    socketService.deleteRoom(roomId)
                                } else {
                                    navController.navigate(Screen.GameLobby.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHost)
                                    Color(0xFFDC3545) else Color(0xFF00B074)
                            )
                        ) {
                            Icon(
                                imageVector = if (isHost)
                                    Icons.Default.Close else Icons.Default.Home,
                                contentDescription = if (isHost) "Xóa phòng" else "Về trang chủ",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
            Text(
                                if (isHost) "Xóa phòng" else "Về trang chủ",
                                color = Color.White
            )
        }
    }
}
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Đang chờ bắt đầu...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }

    // Add SnackbarHost to show messages
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}