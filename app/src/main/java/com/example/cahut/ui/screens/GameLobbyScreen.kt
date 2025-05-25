package com.example.cahut.ui.screens

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme
import com.example.cahut.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.cahut.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.cahut.data.model.Exam
import com.example.cahut.data.repository.ExamRepository
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.cahut.data.repository.RoomRepository
import androidx.compose.material.icons.filled.Login
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import com.example.cahut.util.JwtUtils
import com.example.cahut.config.AppConfig
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import android.content.Context

@Composable
fun GameLobbyScreen(navController: NavController) {
    var gameRoomId by remember { mutableStateOf("") }
    var exams by remember { mutableStateOf<List<Exam>>(emptyList()) }
    var showCreateExamDialog by remember { mutableStateOf(false) }
    var showEditExamDialog by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var selectedExam by remember { mutableStateOf<Exam?>(null) }
    var newExamName by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }
    val examRepository = remember { ExamRepository(context) }
    val roomRepository = remember { RoomRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isPinError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            exams = examRepository.getExams()
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Lỗi khi tải danh sách đề: ${e.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                val token = sharedPreferences.getString("auth_token", "") ?: ""
                val username = JwtUtils.getUsernameFromToken(token) ?: ""
                val userImage = JwtUtils.getUserImageFromToken(token)
                val baseUrl = AppConfig.getBaseUrl()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Avatar and Username Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = context.resources.getIdentifier("a${userImage}", "drawable", context.packageName)),
                                contentDescription = "User avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(40.dp)),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = username,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider()

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout Button at bottom
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng Xuất") },
                        label = { Text("Đăng Xuất") },
                        selected = false,
                        onClick = { 
                            scope.launch {
                                drawerState.close()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.GameLobby.route) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Hồ Sơ",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { scope.launch { drawerState.open() } },
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Room Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFA7A0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tham gia phòng?",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Nhập mã PIN:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = gameRoomId,
                            onValueChange = { 
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    gameRoomId = it
                                    isPinError = false
                                    if (it.length == 6) {
                                        scope.launch {
                                            try {
                                                Log.d("GameLobbyScreen", "Joining room with PIN: $gameRoomId")
                                                val room = roomRepository.joinRoom(gameRoomId)
                                                navController.navigate(Screen.WaitingRoom.createRoute(
                                                    roomId = room.roomId,
                                                    examId = room.examId,
                                                    isHost = false
                                                ))
                                            } catch (e: Exception) {
                                                isPinError = true
                                                snackbarHostState.showSnackbar(
                                                    message = "Lỗi khi tham gia phòng: ${e.message}",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .height(50.dp)
                                .width(140.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = if (isPinError) Color.Red else Color.Transparent,
                                unfocusedBorderColor = if (isPinError) Color.Red else Color.Transparent
                            ),
                            placeholder = { 
                                Text(
                                    "123 456", 
                                    color = Color.Gray,
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                ) 
                            },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Host Game Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF19444A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tạo phòng chơi mới",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { showCreateRoomDialog = true },
                        modifier = Modifier
                            .width(160.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B074)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Tạo Phòng",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create Quiz Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF19444A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tạo bộ Quiz riêng cho bạn",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { 
                            showCreateExamDialog = true
                        },
                        modifier = Modifier
                            .width(160.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6FEEFF)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Tạo Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Exams List Section
            Text(
                text = "Danh sách Quiz",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exams List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exams) { exam ->
                    ExamItem(
                        exam = exam,
                        onEditClick = {
                            selectedExam = exam
                            newExamName = exam.examName
                            showEditExamDialog = true
                        },
                        onDeleteClick = {
                            scope.launch {
                                try {
                                    examRepository.deleteExam(exam._id)
                                    exams = examRepository.getExams()
                                    snackbarHostState.showSnackbar(
                                        message = "Xóa đề thành công!",
                                        duration = SnackbarDuration.Short
                                    )
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Lỗi khi xóa đề: ${e.message}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        onAddQuestionClick = {
                            navController.navigate(Screen.EditQuestions.createRoute(exam._id, exam.examName))
                        }
                    )
                }
            }
        }
    }

    // Create Exam Dialog
    if (showCreateExamDialog) {
        AlertDialog(
            onDismissRequest = { showCreateExamDialog = false },
            title = { 
                Text(
                    "Tạo Quiz mới",
                    color = Color(0xFF00AFC6)
                ) 
            },
            text = {
                OutlinedTextField(
                    value = newExamName,
                    onValueChange = { newExamName = it },
                    label = { Text("Tên Quiz") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00AFC6),
                        unfocusedBorderColor = Color(0xFF00AFC6),
                        focusedLabelColor = Color(0xFF00AFC6),
                        unfocusedLabelColor = Color(0xFF00AFC6),
                        cursorColor = Color(0xFF00AFC6)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (newExamName.isBlank()) {
                                snackbarHostState.showSnackbar(
                                    message = "Vui lòng nhập tên quiz!",
                                    duration = SnackbarDuration.Short
                                )
                                return@launch
                            }
                            try {
                                // Kiểm tra tên quiz đã tồn tại chưa
                                val existingExams = examRepository.getExams()
                                if (existingExams.any { it.examName.equals(newExamName, ignoreCase = true) }) {
                                    snackbarHostState.showSnackbar(
                                        message = "Tên quiz này đã tồn tại!",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }
                                examRepository.createExam(newExamName)
                                val exams = examRepository.getExams()
                                val newExam = exams.find { it.examName == newExamName }
                                if (newExam == null || newExam._id.isNullOrBlank()) {
                                    snackbarHostState.showSnackbar(
                                        message = "Không thể lấy ID của quiz mới",
                                        duration = SnackbarDuration.Short
                                    )
                                    return@launch
                                }
                                showCreateExamDialog = false
                                newExamName = ""
                                navController.navigate(Screen.CreateQuizSlide.createRoute(newExam._id, newExam.examName))
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi khi tạo quiz: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00AFC6)
                    )
                ) {
                    Text("Tạo Quiz", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateExamDialog = false }) {
                    Text("Hủy", color = Color(0xFF00AFC6))
                }
            },
            containerColor = Color(0xFFCFFBFC)
        )
    }

    // Edit Exam Dialog
    if (showEditExamDialog) {
        AlertDialog(
            onDismissRequest = { showEditExamDialog = false },
            title = { Text("Sửa đề") },
            text = {
                OutlinedTextField(
                    value = newExamName,
                    onValueChange = { newExamName = it },
                    label = { Text("Tên đề") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                selectedExam?.let {
                                    examRepository.editExam(it._id, newExamName)
                                    exams = examRepository.getExams()
                                    showEditExamDialog = false
                                    newExamName = ""
                                    snackbarHostState.showSnackbar(
                                        message = "Sửa đề thành công!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi khi sửa đề: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditExamDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Create Room Dialog
    if (showCreateRoomDialog) {
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            title = { 
                Text(
                    "Tạo phòng mới",
                    color = Color(0xFF00B074)
                ) 
            },
            text = {
                Column {
                    Text(
                        text = "Chọn đề thi:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF00B074)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column {
                        exams.forEach { exam ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { 
                                        scope.launch {
                                            try {
                                                Log.d("GameLobbyScreen", "Creating room with examId: ${exam._id}")
                                                val room = roomRepository.createRoom(exam._id)
                                                Log.d("GameLobbyScreen", "Room created with examId: ${room.examId}")
                                                selectedExam = exam
                                                showCreateRoomDialog = false
                                                navController.navigate(Screen.WaitingRoom.createRoute(
                                                    roomId = room.roomId,
                                                    examId = room.examId,
                                                    isHost = true
                                                ))
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                    message = "Lỗi khi tạo phòng: ${e.message}",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedExam?._id == exam._id) 
                                        Color(0xFF00B074) else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = if (selectedExam?._id == exam._id) 
                                        Color(0xFF00B074) else Color(0xFF00B074)
                                )
                            ) {
                                Text(
                                    text = exam.examName,
                                    color = if (selectedExam?._id == exam._id) 
                                        Color.White else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Hủy", color = Color(0xFF00B074))
                }
            },
            containerColor = Color(0xFFd1fccf)
        )
    }

    // Add SnackbarHost to show messages
    Box(modifier = Modifier.fillMaxSize()) {
        // ... existing content ...
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ExamItem(
    exam: Exam,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddQuestionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC679)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exam.examName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Gray
            )
            Row {
                IconButton(
                    onClick = onAddQuestionClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm câu hỏi",
                        tint = Color.Gray
                    )
                }
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa",
                        tint = Color.Gray
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun QuizSelectionDialog(
    onDismiss: () -> Unit,
    onQuizSelected: (String) -> Unit
) {
    val quizzes = remember {
        listOf(
            "Quiz Toán Học",
            "Quiz Tiếng Anh",
            "Quiz Lịch Sử",
            "Quiz Địa Lý",
            "Quiz Văn Học",
            "Quiz Vật Lý"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn Quiz cho phòng",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(quizzes) { quiz ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuizSelected(quiz) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quiz,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (quiz != quizzes.last()) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Preview(
    name = "Game Lobby Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun GameLobbyScreenPreview() {
    GameLobbyTheme {
        GameLobbyScreen(rememberNavController())
    }
} 