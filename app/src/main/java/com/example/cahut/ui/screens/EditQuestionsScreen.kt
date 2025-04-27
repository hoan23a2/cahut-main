package com.example.cahut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cahut.data.model.Question
import com.example.cahut.data.repository.QuestionRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration

@Composable
fun EditQuestionsScreen(
    navController: NavController,
    examId: String,
    examName: String
) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var showCreateQuestionDialog by remember { mutableStateOf(false) }
    var showEditQuestionDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<Question?>(null) }
    var newQuestion by remember { mutableStateOf("") }
    var newOptions by remember { mutableStateOf(listOf("", "", "", "")) }
    var newCorrectAnswer by remember { mutableStateOf("") }
    var newTimeLimit by remember { mutableStateOf(30) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val questionRepository = remember { QuestionRepository(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            questions = questionRepository.getQuestions(examId)
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Lỗi khi tải câu hỏi: ${e.message}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Thêm câu hỏi vào đề: $examName",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showCreateQuestionDialog = true },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm câu hỏi"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm câu hỏi")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                QuestionItem(
                    question = question,
                    onEditClick = {
                        selectedQuestion = question
                        newQuestion = question.question
                        newOptions = question.options
                        newCorrectAnswer = question.correctAnswer
                        newTimeLimit = question.timeLimit
                        showEditQuestionDialog = true
                    },
                    onDeleteClick = {
                        scope.launch {
                            try {
                                questionRepository.deleteQuestion(question._id)
                                questions = questionRepository.getQuestions(examId)
                                snackbarHostState.showSnackbar(
                                    message = "Xóa câu hỏi thành công!",
                                    duration = SnackbarDuration.Short
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi khi xóa câu hỏi: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    // Create Question Dialog
    if (showCreateQuestionDialog) {
        AlertDialog(
            onDismissRequest = { showCreateQuestionDialog = false },
            title = { Text("Thêm câu hỏi mới") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newQuestion,
                        onValueChange = { newQuestion = it },
                        label = { Text("Câu hỏi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    newOptions.forEachIndexed { index, option ->
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                newOptions = newOptions.toMutableList().apply {
                                    set(index, newValue)
                                }
                            },
                            label = { Text("Đáp án ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = newCorrectAnswer,
                        onValueChange = { newCorrectAnswer = it },
                        label = { Text("Đáp án đúng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTimeLimit.toString(),
                        onValueChange = { newTimeLimit = it.toIntOrNull() ?: 30 },
                        label = { Text("Thời gian (giây)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                questionRepository.createQuestion(
                                    examId = examId,
                                    question = newQuestion,
                                    options = newOptions,
                                    correctAnswer = newCorrectAnswer,
                                    timeLimit = newTimeLimit
                                )
                                questions = questionRepository.getQuestions(examId)
                                showCreateQuestionDialog = false
                                newQuestion = ""
                                newOptions = listOf("", "", "", "")
                                newCorrectAnswer = ""
                                newTimeLimit = 30
                                snackbarHostState.showSnackbar(
                                    message = "Thêm câu hỏi thành công!",
                                    duration = SnackbarDuration.Short
                                )
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi khi thêm câu hỏi: ${e.message}",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                ) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateQuestionDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Edit Question Dialog
    if (showEditQuestionDialog) {
        AlertDialog(
            onDismissRequest = { showEditQuestionDialog = false },
            title = { Text("Sửa câu hỏi") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newQuestion,
                        onValueChange = { newQuestion = it },
                        label = { Text("Câu hỏi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    newOptions.forEachIndexed { index, option ->
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                newOptions = newOptions.toMutableList().apply {
                                    set(index, newValue)
                                }
                            },
                            label = { Text("Đáp án ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = newCorrectAnswer,
                        onValueChange = { newCorrectAnswer = it },
                        label = { Text("Đáp án đúng") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTimeLimit.toString(),
                        onValueChange = { newTimeLimit = it.toIntOrNull() ?: 30 },
                        label = { Text("Thời gian (giây)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                selectedQuestion?.let {
                                    questionRepository.editQuestion(
                                        questionId = it._id,
                                        examId = examId,
                                        question = newQuestion,
                                        options = newOptions,
                                        correctAnswer = newCorrectAnswer,
                                        timeLimit = newTimeLimit
                                    )
                                    questions = questionRepository.getQuestions(examId)
                                    showEditQuestionDialog = false
                                    newQuestion = ""
                                    newOptions = listOf("", "", "", "")
                                    newCorrectAnswer = ""
                                    newTimeLimit = 30
                                    snackbarHostState.showSnackbar(
                                        message = "Sửa câu hỏi thành công!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = "Lỗi khi sửa câu hỏi: ${e.message}",
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
                TextButton(onClick = { showEditQuestionDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Add SnackbarHost to show messages
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun QuestionItem(
    question: Question,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Đáp án: ${question.options.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Đáp án đúng: ${question.correctAnswer}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Thời gian: ${question.timeLimit} giây",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa"
                    )
                }
            }
        }
    }
} 