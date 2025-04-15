package com.example.cahut.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cahut.navigation.Screen
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.cahut.ui.theme.GameLobbyTheme

// Data class for Question structure
data class Question(
    val id: Int,
    val text: String,
    val imageUrl: String?,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String? = null
)

@Composable
fun PlayQuizScreen(navController: NavController) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer and Progress
        LinearProgressIndicator(
            progress = 0.7f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question Counter
        Text(
            text = "Câu hỏi 1/10",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Question Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://example.com/question-image.jpg") // Replace with actual image URL
                    .crossfade(true)
                    .build(),
                contentDescription = "Question Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Question Text
        Text(
            text = "Thủ đô của Việt Nam là gì?",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Answer Options
        val options = listOf(
            "A" to "Hà Nội",
            "B" to "Hồ Chí Minh",
            "C" to "Đà Nẵng",
            "D" to "Hải Phòng"
        )

        options.forEach { (option, answer) ->
            val isSelected = selectedAnswer == option

            OutlinedButton(
                onClick = { selectedAnswer = option },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$option. $answer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit Button
        Button(
            onClick = { navController.navigate(Screen.QuizScore.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = selectedAnswer != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = "Trả lời",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(
    name = "Play Quiz Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PlayQuizScreenPreview() {
    GameLobbyTheme {
        PlayQuizScreen(rememberNavController())
    }
} 