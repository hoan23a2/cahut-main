@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
import LeaderboardEntry
import com.example.cahut.data.service.SocketService
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.platform.LocalContext
import com.example.cahut.navigation.Screen
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import com.example.cahut.util.JwtUtils
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.material3.LocalTextStyle
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import com.example.cahut.config.AppConfig
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.zIndex
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.shape.CircleShape


@Composable
fun PlayQuizScreen(
    navController: NavController,
    roomId: String,
    isHost: Boolean,
    totalPlayers: Int
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
    var timeLeftMs by remember { mutableStateOf(0L) }
    var countdown by remember { mutableStateOf<Int?>(null) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var scoreWhenAnswered by remember { mutableStateOf<Int?>(null) }
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var showScores by remember { mutableStateOf<List<LeaderboardEntry>?>(null) }
    var showResults by remember { mutableStateOf<QuizResult?>(null) }
    var loading by remember { mutableStateOf(true) }
    var isCreator by remember { mutableStateOf(false) }
    var currentUsername by remember { mutableStateOf("") }
    var currentUserId by remember { mutableStateOf("") }
    var showRest by remember { mutableStateOf(false) }
    var showTop3 by remember { mutableStateOf(false) }

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

    LaunchedEffect(question) {
        val currentQuestion = question
        if (currentQuestion != null) {
            hasAnswered = false
            scoreWhenAnswered = null
            val totalTimeLimit = currentQuestion.timeLimit
            val totalMs = currentQuestion.timeLimit * 1000L
            val startTime = System.currentTimeMillis()
            var running = true
            while (running) {
                val elapsed = System.currentTimeMillis() - startTime
                val left = (totalMs - elapsed).coerceAtLeast(0L)
                timeLeftMs = left
                val newTimeLeft = (left / 1000).toInt()
                if (newTimeLeft != timeLeft) {
                    timeLeft = newTimeLeft
                }
                if (left <= 0L) {
                    running = false
                } else {
                    delay(16)
                }
            }
            timeLeft = 0
            if (!hasAnswered) {
                socketService.submitAnswer(roomId, "", 0)
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
                    timeLeftMs = event.question.timeLimit * 1000L
                    countdown = null
                    selectedAnswer = null
                    showScores = null
                    showResults = null
                    loading = false
                    hasAnswered = false
                }
                is SocketService.QuizEvent.ShowResults -> {
                    showResults = QuizResult(
                        question = event.question,
                        options = event.options,
                        correctAnswer = event.correctAnswer,
                        type = event.type ?: "normal",
                        imageUrl = event.imageUrl
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

    LaunchedEffect(leaderboard) {
        showRest = false
        showTop3 = false
        delay(300)
        if (leaderboard != null && leaderboard!!.isNotEmpty()) {
            val sorted = leaderboard!!.sortedByDescending { it.score }
            val top3 = sorted.take(3)
            val rest = if (sorted.size > 3) sorted.drop(3).take(4) else emptyList()
            if (rest.isNotEmpty()) showRest = true
            delay(1000)
            showTop3 = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF23616A))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0f3a40), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: roomId, icon người, số lượng người
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${roomId}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Số người",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalPlayers",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                // Right: câu hiện tại/tổng số câu, icon skip nếu là host
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalQuestions > 0) {
                        Text(
                            text = "${questionIndex + 1}/$totalQuestions",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    if (isHost && question != null && showResults == null) {
                        IconButton(
                            onClick = { socketService.timeUp(roomId) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward,
                                contentDescription = "Bỏ qua",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (question!!.type == "image" && question!!.imageUrl != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 16.dp, bottom = 16.dp)
                                        .height(200.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF23616A)
                                    )
                                ) {
                                    val baseUrl = AppConfig.getBaseUrl()
                                    Image(
                                        painter = rememberAsyncImagePainter("${baseUrl}${question!!.imageUrl}"),
                                        contentDescription = "Question image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Vẽ viền đen
                                Text(
                                    question!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = (-1).dp, y = 0.dp)
                                )
                                Text(
                                    question!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 1.dp, y = 0.dp)
                                )
                                Text(
                                    question!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 0.dp, y = (-1).dp)
                                )
                                Text(
                                    question!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 0.dp, y = 1.dp)
                                )
                                // Vẽ chữ trắng ở trên
                                Text(
                                    question!!.question,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Progress bar thời gian custom
                            val totalTime = question?.timeLimit ?: 1
                            val progress = if (totalTime > 0) timeLeftMs.toFloat() / (totalTime * 1000f) else 0f
                            val scoreCurrent = (1000 * progress).toInt().coerceAtLeast(0)
                            val displayedScore = scoreWhenAnswered ?: scoreCurrent
                            val rainbowColors = listOf(
                                Color(0xFFFFE49E), // vàng nhạt
                                Color(0xFFFFA7A0), // đỏ nhạt
                                Color(0xFFC6EA84), // xanh lá nhạt
                                Color(0xFFABFDD7), // xanh ngọc nhạt
                                Color(0xFFFFC679)  // cam nhạt
                            )
                            val barHeight = 15.dp
                            val barRadius = 12.dp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(Color.Transparent),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeight)
                                ) {
                                    val width = size.width
                                    val height = size.height
                                    // Draw background bar
                                    drawRoundRect(
                                        color = Color(0xFF2e3d3d),
                                        size = size,
                                        cornerRadius = CornerRadius(barRadius.toPx(), barRadius.toPx())
                                    )
                                    // Draw progress bar with rainbow gradient
                                    if (progress > 0f) {
                                        drawRoundRect(
                                            brush = Brush.linearGradient(
                                                colors = rainbowColors,
                                                start = Offset(0f, 0f),
                                                end = Offset(width, 0f)
                                            ),
                                            size = Size(width * progress.coerceIn(0f, 1f), height),
                                            cornerRadius = CornerRadius(barRadius.toPx(), barRadius.toPx())
                                        )
                                    }
                                }
                                Text(
                                    text = "$displayedScore",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 8.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            question!!.options.forEachIndexed { index, option ->
                                val optionLetter = ('A' + index).toString()
                                val isSelected = selectedAnswer == option
                                val optionColors = listOf(
                                    Color(0xFFabfdd7), // A
                                    Color(0xFFc6ea84), // B
                                    Color(0xFFffc679), // C
                                    Color(0xFFffa7a0)  // D
                                )
                                val bgColor = optionColors.getOrElse(index) { Color(0xFFabfdd7) }
                                val faded = selectedAnswer != null && !isSelected
                                val answerAlpha = if (faded) 0.4f else 1f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 6.dp)
                                        .alpha(answerAlpha)
                                ) {
                                    Card(
                                        onClick = {
                                            if (selectedAnswer == null && !hasAnswered) {
                                                selectedAnswer = option
                                                hasAnswered = true
                                                val totalTimeMsForScore = (question?.timeLimit ?: 1) * 1000L
                                                val scoreAtAnswer = (1000 * (timeLeftMs.toFloat() / totalTimeMsForScore.toFloat())).toInt().coerceAtLeast(0)
                                                scoreWhenAnswered = scoreAtAnswer
                                                socketService.submitAnswer(roomId, option, scoreAtAnswer)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = bgColor
                                        ),
                                        shape = RoundedCornerShape(28.dp),
                                        border = androidx.compose.foundation.BorderStroke(3.dp, Color.Black.copy(alpha = answerAlpha))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                modifier = Modifier.align(Alignment.Center),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$optionLetter. $option",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
                                                    .background(
                                                        color = Color.Black.copy(alpha = 0.12f)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                showResults != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (showResults!!.type == "image" && showResults!!.imageUrl != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 16.dp, bottom = 16.dp)
                                        .height(200.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF23616A)
                                    )
                                ) {
                                    val baseUrl = AppConfig.getBaseUrl()
                                    Image(
                                        painter = rememberAsyncImagePainter("${baseUrl}${showResults!!.imageUrl}"),
                                        contentDescription = "Question image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Vẽ viền đen
                                Text(
                                    showResults!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = (-1).dp, y = 0.dp)
                                )
                                Text(
                                    showResults!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 1.dp, y = 0.dp)
                                )
                                Text(
                                    showResults!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 0.dp, y = (-1).dp)
                                )
                                Text(
                                    showResults!!.question,
                                    color = Color.Black,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(x = 0.dp, y = 1.dp)
                                )
                                // Vẽ chữ trắng ở trên
                                Text(
                                    showResults!!.question,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            showResults!!.options.forEachIndexed { index, option ->
                                val optionLetter = ('A' + index).toString()
                                val isCorrect = option == showResults!!.correctAnswer
                                val isUserAnswer = option == selectedAnswer

                                val optionColors = listOf(
                                    Color(0xFFabfdd7), // A
                                    Color(0xFFc6ea84), // B
                                    Color(0xFFffc679), // C
                                    Color(0xFFffa7a0)  // D
                                )
                                val bgColor = optionColors.getOrElse(index) { Color(0xFFabfdd7) }

                                val faded = selectedAnswer != null && !isUserAnswer
                                val answerAlpha = if (faded) 0.4f else 1f

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 6.dp)
                                        .alpha(answerAlpha)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = bgColor
                                        ),
                                        shape = RoundedCornerShape(28.dp),
                                        border = androidx.compose.foundation.BorderStroke(3.dp, Color.Black.copy(alpha = answerAlpha))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                modifier = Modifier.align(Alignment.Center),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    "$optionLetter. $option",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (isCorrect) {
                                                    Text("✓", color = Color(0xFF28A745), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                                } else if (isUserAnswer && !isCorrect) {
                                                    Text("✗", color = Color(0xFFDC3545), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
                                                    .background(
                                                        color = Color.Black.copy(alpha = 0.12f)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                showScores != null -> {
                    val topScores = showScores!!.sortedByDescending { it.score }.take(7)
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Confetti cho quán quân (đặt là con trực tiếp của Box chính)
                        if (showTop3 && topScores.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .align(Alignment.TopCenter), // Sử dụng align trong Box chính
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    "🎉🎉🎉",
                                    fontSize = 36.sp,
                                    modifier = Modifier.padding(top = 0.dp)
                                )
                            }
                        }
                        // Column chứa Top 3 và danh sách 4-7 (căn giữa dọc tổng thể)
                        Column(
                            modifier = Modifier
                                .fillMaxSize() // Column này fill Box chính
                                .padding(16.dp), // Thêm padding cho column chứa nội dung
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center // Căn giữa dọc
                        ) {
                            // Top 3
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showTop3,
                                enter = fadeIn() + scaleIn()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    TopWinner(entry = topScores.getOrNull(1), rank = 2)
                                    TopWinner(entry = topScores.getOrNull(0), rank = 1, isChampion = true)
                                    TopWinner(entry = topScores.getOrNull(2), rank = 3)
                                }
                            }
                            // Thêm Spacer giữa Top 3 và danh sách 4-7
                            if (showTop3 && topScores.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            // Danh sách 4-7 (không còn dòng 1-3 chỉ số thứ tự)
                            if (topScores.isNotEmpty()) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showRest,
                                    enter = fadeIn() + scaleIn()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(), // Column này fill Column cha
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        topScores.forEach { entry ->
                                            Row(
                                                modifier = Modifier
                                                    .widthIn(max = 420.dp)
                                                    .padding(vertical = 6.dp)
                                                    .background(Color(0xFFffc679), RoundedCornerShape(24.dp))
                                                    .border(2.dp, Color.Black, RoundedCornerShape(24.dp)),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${entry.rank}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier.padding(start = 24.dp, end = 16.dp)
                                                )
                                                // Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(Color(0xFFFFE49E))
                                                        .border(2.dp, Color.Black, RoundedCornerShape(20.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = LocalContext.current.resources.getIdentifier("a${entry.userImage}", "drawable", LocalContext.current.packageName)),
                                                        contentDescription = "Player avatar",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = entry.username,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(
                                                    text = "${entry.score}",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier.padding(end = 24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                leaderboard != null -> {
                    val sorted = leaderboard!!.sortedByDescending { it.score }
                    val top3 = sorted.take(3)
                    val rest = if (sorted.size > 3) sorted.drop(3).take(4) else emptyList()
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Confetti cho quán quân (đặt là con trực tiếp của Box chính)
                        if (showTop3 && top3.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .align(Alignment.TopCenter), // Sử dụng align trong Box chính
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    "🎉🎉🎉",
                                    fontSize = 36.sp,
                                    modifier = Modifier.padding(top = 0.dp)
                                )
                            }
                        }
                        // Column chứa Top 3 và danh sách 4-7 (căn giữa dọc tổng thể)
                        Column(
                            modifier = Modifier
                                .fillMaxSize() // Column này fill Box chính
                                .padding(16.dp), // Thêm padding cho column chứa nội dung
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center // Căn giữa dọc
                        ) {
                            // Top 3
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showTop3,
                                enter = fadeIn() + scaleIn()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    TopWinner(entry = top3.getOrNull(1), rank = 2)
                                    TopWinner(entry = top3.getOrNull(0), rank = 1, isChampion = true)
                                    TopWinner(entry = top3.getOrNull(2), rank = 3)
                                }
                            }
                            // Thêm Spacer giữa Top 3 và danh sách 4-7
                            if (showTop3 && rest.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            // Danh sách 4-7 (không còn dòng 1-3 chỉ số thứ tự)
                            if (rest.isNotEmpty()) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showRest,
                                    enter = fadeIn() + scaleIn()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(), // Column này fill Column cha
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        rest.forEach { entry ->
                                            Row(
                                                modifier = Modifier
                                                    .widthIn(max = 420.dp)
                                                    .padding(vertical = 6.dp)
                                                    .background(Color(0xFFffc679), RoundedCornerShape(24.dp))
                                                    .border(2.dp, Color.Black, RoundedCornerShape(24.dp)),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${entry.rank}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier.padding(start = 24.dp, end = 16.dp)
                                                )
                                                // Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(Color(0xFFFFE49E))
                                                        .border(2.dp, Color.Black, RoundedCornerShape(20.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = LocalContext.current.resources.getIdentifier("a${entry.userImage}", "drawable", LocalContext.current.packageName)),
                                                        contentDescription = "Player avatar",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = entry.username,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(
                                                    text = "${entry.score}",
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    modifier = Modifier.padding(end = 24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
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
}

@Composable
fun TopWinner(entry: LeaderboardEntry?, rank: Int, isChampion: Boolean = false) {
    if (entry == null) return
    val medalIcons = mapOf(
        1 to "\uD83E\uDD47", // 🥇
        2 to "\uD83E\uDD48", // 🥈
        3 to "\uD83E\uDD49"  // 🥉
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Căn giữa nội dung bên trong cột
        modifier = Modifier.width(IntrinsicSize.Min) // Giúp cột chỉ chiếm đủ chiều rộng cần thiết
    ) {
        // Tên (trắng, viền đen, trên avatar)
        Box(modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { // FillMaxWidth và căn giữa ngang
            Text(
                text = entry.username,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = if (isChampion) 22.sp else 18.sp,
                style = LocalTextStyle.current.copy(
                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = entry.username,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isChampion) 22.sp else 18.sp,
                textAlign = TextAlign.Center
            )
        }
        // Container chứa Avatar và Huy chương
        Box { // Box này chứa cả avatar và huy chương để định vị tương đối
             // Avatar hình tròn đầy đủ
            Box(
                modifier = Modifier
                    .size(if (isChampion) 90.dp else 70.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = LocalContext.current.resources.getIdentifier("a${entry.userImage}", "drawable", LocalContext.current.packageName)),
                    contentDescription = "Player avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Huy chương ở góc dưới trái, nằm ngoài vòng tròn avatar
            Box(
                modifier = Modifier
                    .size(32.dp) // Kích thước huy chương
                    .align(Alignment.BottomStart) // Căn góc dưới trái của Box container
                    .offset(x = (-12).dp, y = 12.dp) // Offset ra ngoài và xuống dưới
                    .background(Color.White, CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
                    .zIndex(1f), // Đảm bảo nổi lên trên
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = medalIcons[rank] ?: "",
                    fontSize = 20.sp // Kích thước font huy chương
                )
            }
        }
        // Điểm (trắng, viền đen, dưới avatar)
        Box(modifier = Modifier.padding(top = 4.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { // FillMaxWidth và căn giữa ngang
            Text(
                text = "${entry.score}",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = if (isChampion) 22.sp else 18.sp,
                style = LocalTextStyle.current.copy(
                    drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${entry.score}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (isChampion) 22.sp else 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}