package com.example.cahut.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

// Data class to represent a question
data class QuizQuestion(
    val id: Int,
    var question: String = "",
    var isMultipleChoice: Boolean = true,
    var options: List<String> = listOf("", "", "", ""),
    var correctAnswer: String = "",
    var timeLimit: Int = 30,
    var funFact: String = ""
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
        return questions.any { isQuestionValid(it) }
    }

    // Function to update a question
    fun updateQuestion(updatedQuestion: QuizQuestion) {
        questions = questions.map { 
            if (it.id == updatedQuestion.id) updatedQuestion else it 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF19444A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quiz Name Display Section - Không cho sửa
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
                    Text(
                        text = examName,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = Color(0xFF23616A),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
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
                                // Gửi API tạo câu hỏi cho examId đã có, tuần tự từng câu
                                val validQuestions = questions.filter { isQuestionValid(it) }
                                var successCount = 0
                                for (question in validQuestions) {
                                    try {
                                        questionRepository.createQuestion(
                                            examId = examId,
                                            question = question.question,
                                            options = if (question.isMultipleChoice) question.options else listOf(question.correctAnswer),
                                            correctAnswer = question.correctAnswer,
                                            timeLimit = question.timeLimit
                                        )
                                        successCount++
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = "Lỗi khi tạo câu hỏi: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                                snackbarHostState.showSnackbar(
                                    message = "Tạo quiz thành công với $successCount câu hỏi!",
                                    duration = SnackbarDuration.Short
                                )
                                navController.navigate(Screen.GameLobby.route) {
                                    popUpTo(Screen.GameLobby.route) {
                                        inclusive = false
                                    }
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi khi tạo câu hỏi: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmitQuiz()) Color(0xFF00AFC6) else Color.Gray
                    ),
                    enabled = canSubmitQuiz()
                ) {
                    Text(
                        text = "Hoàn thành",
                        color = Color.White
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
                    .height(120.dp)
                    .border(
                        width = 2.dp,
                                color = Color(0xFF00AFC6),
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable { /* TODO: Handle image selection */ }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
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
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Gray
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

            Spacer(modifier = Modifier.height(16.dp))

            // Fun Fact Input
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
                value = currentQuestion.funFact,
                onValueChange = { 
                    updateQuestion(currentQuestion.copy(funFact = it))
                },
                            label = { Text("Thông Tin Thêm (Tùy Chọn)", color = Color.Gray) },
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
                }
            }
        }

        // Bottom Navigation Buttons - Outside of scrollable area
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    // Question Slides
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
                    Text(
                                    text = question.id.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
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

    // Thêm FloatingActionButton bên ngoài navigation bar, luôn ở sát phải
    Box(modifier = Modifier.fillMaxSize()) {
        // ... existing content ...
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
                .padding(end = 32.dp, bottom = 32.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm câu hỏi")
        }
    }
} 