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
        CreateQuizSlideScreen(rememberNavController(), "")
    }
}

@Composable
fun CreateQuizSlideScreen(
    navController: NavController,
    examId: String
) {
    var questions by remember { mutableStateOf(listOf(QuizQuestion(1))) }
    var currentSlide by remember { mutableStateOf(1) }
    val context = LocalContext.current
    val questionRepository = remember { QuestionRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Get current question
    val currentQuestion = questions.find { it.id == currentSlide } ?: questions.first()

    // Function to update a question
    fun updateQuestion(updatedQuestion: QuizQuestion) {
        questions = questions.map { 
            if (it.id == updatedQuestion.id) updatedQuestion else it 
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Content that can scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tạo câu hỏi cho đề: $examId",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Câu hỏi ${currentQuestion.id}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Question Type Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { 
                        updateQuestion(currentQuestion.copy(isMultipleChoice = true))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentQuestion.isMultipleChoice) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Trắc Nghiệm")
                }
                Button(
                    onClick = { 
                        updateQuestion(currentQuestion.copy(isMultipleChoice = false))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!currentQuestion.isMultipleChoice) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Nhập văn bản")
                }
            }

            // Image Upload Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Thêm Hình Ảnh (Tùy Chọn)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Input
            OutlinedTextField(
                value = currentQuestion.question,
                onValueChange = { 
                    updateQuestion(currentQuestion.copy(question = it))
                },
                label = { Text("Câu Hỏi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

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
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
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
                    label = { Text("Giây") },
                    modifier = Modifier.width(120.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    trailingIcon = {
                        Text(
                            text = "giây",
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (currentQuestion.isMultipleChoice) {
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
                            }
                        )
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                val newOptions = currentQuestion.options.toMutableList().apply {
                                    set(index, newValue)
                                }
                                updateQuestion(currentQuestion.copy(options = newOptions))
                            },
                            label = { Text("Đáp Án ${('A' + index)}") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            } else {
                // Text Answer Input
                OutlinedTextField(
                    value = currentQuestion.correctAnswer,
                    onValueChange = { 
                        updateQuestion(currentQuestion.copy(correctAnswer = it))
                    },
                    label = { Text("Đáp Án Đúng (Bắt Buộc)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fun Fact Input
            OutlinedTextField(
                value = currentQuestion.funFact,
                onValueChange = { 
                    updateQuestion(currentQuestion.copy(funFact = it))
                },
                label = { Text("Thông Tin Thêm (Tùy Chọn)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Navigation Buttons - Outside of scrollable area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                    // Home Button
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { 
                        navController.navigateUp()
                    },
                        colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    }

                    // Question Slides
                    questions.forEach { question ->
                        Card(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { 
                                    currentSlide = question.id
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (question.id == currentSlide)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
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

                    // Add Question Button
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { 
                                val newQuestion = QuizQuestion(questions.size + 1)
                                questions = questions + newQuestion
                                currentSlide = newQuestion.id
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm câu hỏi",
                                tint = Color.White
                    )
                }
                    }

                    // Finish Button
                    Card(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                scope.launch {
                                    try {
                                        questions.forEach { question ->
                                            if (question.question.isNotBlank() && question.correctAnswer.isNotBlank()) {
                                                questionRepository.createQuestion(
                                                    examId = examId,
                                                    question = question.question,
                                                    options = if (question.isMultipleChoice) question.options else listOf(question.correctAnswer),
                                                    correctAnswer = question.correctAnswer,
                                                    timeLimit = question.timeLimit
                                                )
                                            }
                                        }
                        navController.navigate(Screen.GameLobby.route) {
                            popUpTo(Screen.GameLobby.route) {
                                inclusive = false
                            }
                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = "Lỗi khi lưu câu hỏi: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Hoàn thành",
                                tint = Color.White
                            )
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
} 