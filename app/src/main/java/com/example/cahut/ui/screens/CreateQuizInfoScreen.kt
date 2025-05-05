package com.example.cahut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.cahut.R
import com.example.cahut.navigation.Screen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.cahut.data.repository.ExamRepository
import kotlinx.coroutines.launch

@Composable
fun CreateQuizInfoScreen(navController: NavController) {
    var quizName by remember { mutableStateOf("") }
    var quizDescription by remember { mutableStateOf("") }
    val context = LocalContext.current
    val examRepository = remember { ExamRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var existingExams by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load existing exam names
    LaunchedEffect(Unit) {
        try {
            val exams = examRepository.getExams()
            existingExams = exams.map { it.examName }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Lỗi khi tải danh sách đề: ${e.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF19444A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image Selection Card
        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(top = 24.dp)
                .clickable { /* TODO: Handle image selection */ }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm Hình Ảnh",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Input Fields Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF23616A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Quiz Name Section
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .wrapContentWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF00AFC6)
                        )
                    ) {
                        Text(
                            text = "Quiz name",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 20.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 19.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF00AFC6)
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFFBE6)
                            )
                        ) {
                            OutlinedTextField(
                                value = quizName,
                                onValueChange = { quizName = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description Section
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .wrapContentWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF00AFC6)
                        )
                    ) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 20.dp)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 19.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF00AFC6)
                        )
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(5.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFFBE6)
                            )
                        ) {
                            OutlinedTextField(
                                value = quizDescription,
                                onValueChange = { quizDescription = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                minLines = 3
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next Button
        Button(
            onClick = { 
                scope.launch {
                    try {
                        val trimmedQuizName = quizName.trim()
                        if (trimmedQuizName.isBlank()) {
                            snackbarHostState.showSnackbar(
                                message = "Vui lòng nhập tên đề",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        if (existingExams.any { it.equals(trimmedQuizName, ignoreCase = true) }) {
                            snackbarHostState.showSnackbar(
                                message = "Tên đề đã tồn tại, vui lòng chọn tên khác",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        // Gọi createExam để quiz có trên server và lấy luôn object trả về
                        val createdExam = examRepository.createExam(trimmedQuizName)
                        if (createdExam == null || createdExam._id.isBlank()) {
                            snackbarHostState.showSnackbar(
                                message = "Không thể lấy ID của đề mới",
                                duration = SnackbarDuration.Short
                            )
                            return@launch
                        }
                        // Truyền _id (examId) và examName sang màn tiếp theo
                        navController.navigate(Screen.CreateQuizSlide.createRoute(createdExam._id, createdExam.examName))
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            message = "Lỗi khi tạo đề: ${e.message}",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00AFC6)
            )
        ) {
            Text("Thêm câu hỏi")
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

@Preview(
    name = "Create Quiz Info Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CreateQuizInfoScreenPreview() {
    GameLobbyTheme {
        CreateQuizInfoScreen(rememberNavController())
    }
} 