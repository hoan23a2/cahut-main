package com.example.cahut.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.cahut.navigation.Screen
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.cahut.data.repository.QuestionRepository
import com.example.cahut.data.repository.ExamRepository
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

// Data class to represent a question
data class QuizQuestion(
    val id: Int,
    var question: String = "",
    var isMultipleChoice: Boolean = true,
    var options: List<String> = listOf("", "", "", ""),
    var correctAnswer: String = "",
    var timeLimit: Int = 30,
    var type: String = "normal",
    var imageUri: Uri? = null
)

@Preview(
    name = "Create Quiz Slide Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CreateQuizSlideScreenPreview() {
    GameLobbyTheme {
        CreateQuizSlideScreen(rememberNavController(), "", "")
    }
}

@Composable
fun CreateQuizSlideScreen(
    navController: NavController,
    examId: String,
    examName: String
) {
    var questions by remember { mutableStateOf(listOf(QuizQuestion(1))) }
    var currentSlide by remember { mutableStateOf(1) }
    val context = LocalContext.current
    val questionRepository = remember { QuestionRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val examRepository = remember { ExamRepository(context) }
    
    // Get current question
    val currentQuestion = questions.find { it.id == currentSlide } ?: questions.first()

    // Function to check if a question is valid
    fun isQuestionValid(question: QuizQuestion): Boolean {
        return question.question.isNotBlank() && 
               question.correctAnswer.isNotBlank() &&
               question.options.all { it.isNotBlank() }
    }

    // Function to check if quiz can be submitted
    fun canSubmitQuiz(): Boolean {
        return questions.all { isQuestionValid(it) }
    }

    // Function to update a question
    fun updateQuestion(updatedQuestion: QuizQuestion) {
        questions = questions.map { 
            if (it.id == updatedQuestion.id) updatedQuestion else it 
        }
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            updateQuestion(currentQuestion.copy(
                type = "image",
                imageUri = uri
            ))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF19444A))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quiz Name Display Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF23616A)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    examRepository.deleteExam(examId)
                                    navController.navigate(Screen.GameLobby.route) {
                                        popUpTo(Screen.GameLobby.route) {
                                            inclusive = false
                                        }
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Lỗi khi xóa quiz: ${e.message}",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF00AFC6), RoundedCornerShape(8.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = examName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    if (!canSubmitQuiz()) {
                                        snackbarHostState.showSnackbar(
                                            message = "Vui lòng điền đầy đủ thông tin quiz và ít nhất một câu hỏi",
                                            duration = SnackbarDuration.Short
                                        )
                                        return@launch
                                    }
                                    val validQuestions = questions.filter { isQuestionValid(it) }
                                    
                                    // Chuyển về GameLobby trước
                                    navController.navigate(Screen.GameLobby.route) {
                                        popUpTo(Screen.GameLobby.route) {
                                            inclusive = false
                                        }
                                    }

                                    // Sau đó mới gửi câu hỏi lên API
                                    var successCount = 0
                                    for (question in validQuestions) {
                                        try {
                                            questionRepository.createQuestion(
                                                examId = examId,
                                                question = question.question,
                                                options = if (question.isMultipleChoice) question.options else listOf(question.correctAnswer),
                                                correctAnswer = question.correctAnswer,
                                                timeLimit = question.timeLimit,
                                                type = question.type,
                                                imageUri = question.imageUri
                                            )
                                            successCount++
                                        } catch (e: Exception) {
                                            Log.e("CreateQuizSlideScreen", "Lỗi khi tạo câu hỏi: ${e.message}")
                                        }
                                    }
                                    Log.d("CreateQuizSlideScreen", "Đã tạo thành công $successCount câu hỏi")
                                } catch (e: Exception) {
                                    Log.e("CreateQuizSlideScreen", "Lỗi khi xử lý: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier
                            .background(
                                if (canSubmitQuiz()) Color(0xFF00AFC6) else Color.Gray,
                                RoundedCornerShape(8.dp)
                            )
                            .size(40.dp),
                        enabled = canSubmitQuiz()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Hoàn thành",
                            tint = Color.White
                        )
                    }
                }
            }

            // Content that can scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Câu hỏi ${currentQuestion.id}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF23616A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Image Upload Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF00AFC6),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { imagePicker.launch("image/*") }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentQuestion.imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(currentQuestion.imageUri),
                                    contentDescription = "Selected image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Thêm Hình Ảnh",
                                        modifier = Modifier.size(36.dp),
                                        tint = Color(0xFF00AFC6)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Thêm Hình Ảnh (Tùy Chọn)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF00AFC6)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Question Input
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFFBE6)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(4.dp, Color(0xFF00AFC6))
                        ) {
                            TextField(
                                value = currentQuestion.question,
                                onValueChange = { 
                                    updateQuestion(currentQuestion.copy(question = it))
                                },
                                label = { Text("Câu Hỏi", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF23616A),
                                    unfocusedTextColor = Color(0xFF23616A),
                                    focusedLabelColor = Color.Gray,
                                    unfocusedLabelColor = Color.Gray,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Time Input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thời gian trả lời:",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Card(
                                modifier = Modifier
                                    .width(120.dp)
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFFBE6)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(4.dp, Color(0xFF00AFC6))
                            ) {
                                TextField(
                                    value = currentQuestion.timeLimit.toString(),
                                    onValueChange = { 
                                        val newValue = it.filter { char -> char.isDigit() }
                                        if (newValue.isEmpty()) {
                                            updateQuestion(currentQuestion.copy(timeLimit = 0))
                                        } else {
                                            val seconds = newValue.toIntOrNull() ?: 0
                                            val timeLimit = when {
                                                seconds > 300 -> 300 // Max 5 minutes
                                                else -> seconds
                                            }
                                            updateQuestion(currentQuestion.copy(timeLimit = timeLimit))
                                        }
                                    },
                                    label = { Text("Giây", color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF23616A),
                                        unfocusedTextColor = Color(0xFF23616A),
                                        focusedLabelColor = Color.Gray,
                                        unfocusedLabelColor = Color.Gray,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent
                                    ),
                                    trailingIcon = {
                                        Text(
                                            text = "giây",
                                            modifier = Modifier.padding(end = 16.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }
                                )
                            }
                        }

                        // Multiple Choice Options
                        currentQuestion.options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentQuestion.correctAnswer == option,
                                    onClick = { 
                                        updateQuestion(currentQuestion.copy(correctAnswer = option))
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF00AFC6),
                                        unselectedColor = Color(0xFF00AFC6)
                                    )
                                )
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFFBE6)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(4.dp, Color(0xFF00AFC6))
                                ) {
                                    TextField(
                                        value = option,
                                        onValueChange = { newValue ->
                                            val newOptions = currentQuestion.options.toMutableList().apply {
                                                set(index, newValue)
                                            }
                                            updateQuestion(currentQuestion.copy(options = newOptions))
                                        },
                                        label = { Text("Đáp Án ${('A' + index)}", color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF23616A),
                                            unfocusedTextColor = Color(0xFF23616A),
                                            focusedLabelColor = Color.Gray,
                                            unfocusedLabelColor = Color.Gray,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Delete Slide Button
                if (questions.size > 1) {
                    Button(
                        onClick = {
                            // Xóa slide hiện tại
                            questions = questions.filter { it.id != currentSlide }
                            // Cập nhật lại ID cho các slide còn lại
                            questions = questions.mapIndexed { index, question ->
                                question.copy(id = index + 1)
                            }
                            // Chuyển về slide trước đó hoặc slide đầu tiên
                            currentSlide = if (currentSlide > 1) currentSlide - 1 else 1
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA7A0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa slide",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Xóa slide",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF23616A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Slides Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 75.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(questions.size) {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            questions.forEach { question ->
                                Card(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable { 
                                            currentSlide = question.id
                                        }
                                        .then(
                                            if (question.id == currentSlide) Modifier.border(2.dp, Color(0xFF00AFC6), RoundedCornerShape(8.dp))
                                            else Modifier
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (question.id == currentSlide)
                                            Color(0xFF23616A)
                                        else
                                            Color(0xFF00AFC6)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = question.id.toString(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            if (!isQuestionValid(question)) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Chưa hoàn thành",
                                                    tint = Color(0xFFFFA7A0),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // FloatingActionButton
        FloatingActionButton(
            onClick = {
                val newQuestion = QuizQuestion(questions.size + 1)
                questions = questions + newQuestion
                currentSlide = newQuestion.id
            },
            containerColor = Color(0xFF00AFC6),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 46.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm câu hỏi")
        }
    }
} 