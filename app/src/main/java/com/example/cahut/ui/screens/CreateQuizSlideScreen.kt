package com.example.cahut.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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

@Preview(
    name = "Create Quiz Slide Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CreateQuizSlideScreenPreview() {
    GameLobbyTheme {
        CreateQuizSlideScreen(rememberNavController())
    }
}

@Composable
fun CreateQuizSlideScreen(navController: NavController) {
    var isMultipleChoice by remember { mutableStateOf(true) }
    var question by remember { mutableStateOf("") }
    var funFact by remember { mutableStateOf("") }
    
    // Multiple Choice specific states
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctOption by remember { mutableStateOf<String?>(null) }
    
    // Text Answer specific states
    var correctAnswer by remember { mutableStateOf("") }
    var additionalAnswers by remember { mutableStateOf(listOf<String>()) }

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
                text = "Tạo Câu Hỏi",
                style = MaterialTheme.typography.headlineMedium,
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
                    onClick = { isMultipleChoice = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMultipleChoice) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Trắc Nghiệm")
                }
                Button(
                    onClick = { isMultipleChoice = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMultipleChoice) 
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
                value = question,
                onValueChange = { question = it },
                label = { Text("Câu Hỏi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Input
            var timeInSeconds by remember { mutableStateOf("30") }
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
                    value = timeInSeconds,
                    onValueChange = { 
                        // Only allow numbers and limit to reasonable values
                        val newValue = it.filter { char -> char.isDigit() }
                        if (newValue.isEmpty()) {
                            timeInSeconds = "0"
                        } else {
                            val seconds = newValue.toIntOrNull() ?: 0
                            timeInSeconds = when {
                                seconds > 300 -> "300" // Max 5 minutes
                                else -> seconds.toString()
                            }
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

            if (isMultipleChoice) {
                // Multiple Choice Options
                listOf(
                    "A" to optionA,
                    "B" to optionB,
                    "C" to optionC,
                    "D" to optionD
                ).forEach { (option, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = correctOption == option,
                            onClick = { correctOption = option }
                        )
                        OutlinedTextField(
                            value = when (option) {
                                "A" -> optionA
                                "B" -> optionB
                                "C" -> optionC
                                else -> optionD
                            },
                            onValueChange = {
                                when (option) {
                                    "A" -> optionA = it
                                    "B" -> optionB = it
                                    "C" -> optionC = it
                                    else -> optionD = it
                                }
                            },
                            label = { Text("Đáp Án $option") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }
                }
            } else {
                // Text Answer Input
                OutlinedTextField(
                    value = correctAnswer,
                    onValueChange = { correctAnswer = it },
                    label = { Text("Đáp Án Đúng (Bắt Buộc)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Additional Answers
                additionalAnswers.forEachIndexed { index, answer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { newValue ->
                                additionalAnswers = additionalAnswers.toMutableList().apply {
                                    set(index, newValue)
                                }
                            },
                            label = { Text("Đáp Án Thay Thế ${index + 1}") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        IconButton(
                            onClick = {
                                additionalAnswers = additionalAnswers.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, "Xóa Đáp Án")
                        }
                    }
                }

                // Add Answer Button
                if (additionalAnswers.size < 3) {
                    TextButton(
                        onClick = {
                            additionalAnswers = additionalAnswers + ""
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Add, "Thêm Đáp Án")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm Đáp Án Thay Thế")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fun Fact Input
            OutlinedTextField(
                value = funFact,
                onValueChange = { funFact = it },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Screen Button
                Button(
                    onClick = { 
                        navController.navigateUp()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Thông Tin",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Add Question Button
                Button(
                    onClick = {
                        // TODO: Save current slide and reset form
                        question = ""
                        funFact = ""
                        if (isMultipleChoice) {
                            optionA = ""
                            optionB = ""
                            optionC = ""
                            optionD = ""
                            correctOption = null
                        } else {
                            correctAnswer = ""
                            additionalAnswers = listOf()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Thêm Câu Hỏi",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Finish Quiz Button
                Button(
                    onClick = { 
                        navController.navigate(Screen.GameLobby.route) {
                            popUpTo(Screen.GameLobby.route) {
                                inclusive = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = "Hoàn Thành",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 