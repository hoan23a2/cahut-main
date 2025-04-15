package com.example.cahut.ui.screens

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
import androidx.navigation.NavController
import com.example.cahut.navigation.Screen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme

@Composable
fun QuizScoreScreen(navController: NavController) {
    // Mock data for demonstration
    val players = listOf(
        "Người chơi 1" to 100,
        "Người chơi 2" to 85,
        "Người chơi 3" to 75,
        "Người chơi 4" to 60,
        "Người chơi 5" to 45
    )

    // Current question number (for demonstration)
    var currentQuestion by remember { mutableStateOf(10) } // Set to 10 for testing final screen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Bảng Điểm",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Score Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Câu hỏi 1/10",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Đã trả lời đúng: 3/5 người chơi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Players Score List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(players) { (player, score) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "$score",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " điểm",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next Question or Show Result Button
        Button(
            onClick = { 
                if (currentQuestion >= 10) {
                    // Navigate to final result screen
                    navController.navigate(Screen.QuizResult.route) {
                        popUpTo(Screen.PlayQuiz.route) { inclusive = true }
                    }
                } else {
                    // Navigate to next question
                    currentQuestion++
                    navController.navigate(Screen.PlayQuiz.route) {
                        popUpTo(Screen.QuizScore.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (currentQuestion >= 10) "Xem Kết Quả" else "Câu Hỏi Tiếp Theo",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(
    name = "Quiz Score Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun QuizScoreScreenPreview() {
    GameLobbyTheme {
        QuizScoreScreen(rememberNavController())
    }
} 