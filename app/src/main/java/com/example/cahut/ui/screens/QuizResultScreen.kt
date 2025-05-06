package com.example.cahut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cahut.navigation.Screen
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.cahut.ui.theme.GameLobbyTheme

@Composable
fun QuizResultScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF23616A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Kết Quả Cuối Cùng",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Top 3 Winners
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Second Place
            TopWinner(
                rank = 2,
                name = "Người chơi 2",
                score = 85,
                modifier = Modifier.weight(1f)
            )
            
            // First Place
            TopWinner(
                rank = 1,
                name = "Người chơi 1",
                score = 100,
                modifier = Modifier.weight(1f),
                isWinner = true
            )
            
            // Third Place
            TopWinner(
                rank = 3,
                name = "Người chơi 3",
                score = 75,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Other Players List
        Text(
            text = "Bảng Xếp Hạng",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items((4..8).toList()) { rank ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$rank.",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.width(40.dp)
                            )
                            Text(
                                text = "Người chơi $rank",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            text = "${100 - (rank * 10)} điểm",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Return to Lobby Button
        Button(
            onClick = { 
                navController.navigate(Screen.GameLobby.route) {
                    popUpTo(Screen.GameLobby.route) {
                        inclusive = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Trở Về Sảnh")
        }
    }
}

@Composable
private fun TopWinner(
    rank: Int,
    name: String,
    score: Int,
    modifier: Modifier = Modifier,
    isWinner: Boolean = false
) {
    val podiumColors = mapOf(
        1 to Color(0xFFFFD700), // Gold
        2 to Color(0xFFC0C0C0), // Silver
        3 to Color(0xFFCD7F32)  // Bronze
    )

    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isWinner) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Winner Trophy",
                tint = podiumColors[rank] ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(if (isWinner) 80.dp else 60.dp)
                .clip(CircleShape)
                .background(podiumColors[rank] ?: MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = Color.White,
                fontSize = if (isWinner) 32.sp else 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Text(
            text = "$score điểm",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(
    name = "Quiz Result Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun QuizResultScreenPreview() {
    GameLobbyTheme {
        QuizResultScreen(rememberNavController())
    }
} 