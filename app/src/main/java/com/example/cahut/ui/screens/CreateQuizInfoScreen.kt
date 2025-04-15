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

@Composable
fun CreateQuizInfoScreen(navController: NavController) {
    var quizName by remember { mutableStateOf("") }
    var quizDescription by remember { mutableStateOf("") }
    // TODO: Add image handling state

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
                            .padding(top =19.dp),
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
                // TODO: Save quiz info and navigate to slide creation
                navController.navigate(Screen.CreateQuizSlide.route)
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